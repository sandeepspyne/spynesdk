package com.spyneai.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ProcessRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.repository.db.ShootDao
import com.spyneai.shoot.repository.model.project.ProjectBody
import com.spyneai.shoot.repository.model.sku.Sku
import com.spyneai.toDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ProcessSkuSync(
    val context: Context,
    val shootDao: ShootDao,
    val listener: DataSyncListener,
    var retryCount: Int = 0,
    var connectionLost: Boolean = false
) {

    val TAG = "ProcessSkuSync"

    fun processSkuParent(type : String,startedBy : String?) {
        context.captureEvent(Events.PROCESS_SKU_PARENT_TRIGGERED,HashMap<String,Any?>().apply {
            put("type",type)
            put("service_started_by",startedBy)
            put("upload_running", Utilities.getBool(context, AppConstants.PROCESS_SKU_RUNNING, false))
        })

        //update triggered value
        Utilities.saveBool(context, AppConstants.PROCESS_SKU_PARENT_TRIGGERED, true)

        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            if (Utilities.getBool(context, AppConstants.PROCESS_SKU_PARENT_TRIGGERED, true)
                &&
                !Utilities.getBool(context, AppConstants.PROCESS_SKU_RUNNING, false)
            ) {
                if (context.isInternetActive())
                    GlobalScope.launch(Dispatchers.Default) {
                        Log.d(TAG, "uploadParent: start")
                         Utilities.saveBool(context, AppConstants.PROCESS_SKU_RUNNING, true)
                        context.captureEvent(Events.PROCESS_SKU_STARTED,HashMap())
                        processSku()
                    }
                else {
                    Utilities.saveBool(context, AppConstants.PROCESS_SKU_RUNNING, false)
                    listener.onConnectionLost("Process Sku Stopped",SeverSyncTypes.PROCESS)
                    Log.d(TAG, "uploadParent: connection lost")
                }
            }
        }, getRandomNumberInRange().toLong())
    }

    suspend fun  processSku(){
        do {
            val sku = shootDao.getProcessAbleSku() ?: break

            if (connectionLost){
                val count = shootDao.getPendingSku()
                context.captureEvent(
                    Events.PROCESS_SKU_CONNECTION_CONNECTION_BREAK,
                    HashMap<String,Any?>()
                        .apply {
                            put("sku_remaining",count)
                        }
                )
                Utilities.saveBool(context,AppConstants.PROCESS_SKU_RUNNING,false)
                listener.onConnectionLost("Process Sku Stopped",SeverSyncTypes.CREATE)
                break
            }else {
                val properties = HashMap<String,Any?>()
                    .apply {
                        put("project_id",sku.projectId)
                        put("sku_id",sku.skuId)
                        put("data",Gson().toJson(sku))
                    }

                context.captureEvent(
                    Events.SKU_SELECTED,
                    properties
                )

                if (retryCount > 4){
                    //skip project
                    val skip = shootDao.skipSku(
                        sku.uuid,
                        sku.toProcessAt.plus( sku.retryCount * AppConstants.RETRY_DELAY_TIME)
                    )

                    context.captureEvent(
                        Events.SKU_SKIPPED,
                        properties.apply {
                            put("db_count",skip)
                        }
                    )
                    continue
                }

                //in progress listener
                listener.inProgress("Processing Sku ${sku.skuName}",SeverSyncTypes.PROCESS)


                if (sku.totalFramesUpdated){
                    processSku(sku)
                }else {
                    //update total frames
                    val isTotalFramesUpdated = updateTotalFrames(sku)

                    if (!isTotalFramesUpdated)
                        continue

                    processSku(sku)

                    continue
                }
            }
        }while (sku != null)

        if (!connectionLost){
            //get pending projects count
            val count = shootDao.getPendingSku()

            context.captureEvent(
                Events.ALL_SKUS_PROCESSED_BREAKS,
                HashMap<String,Any?>().apply {
                    put("sku_remaining",count)
                }
            )

            if (count > 0){
                val sku = shootDao.getOldestSku()
                val scheduleTime = sku.toProcessAt.minus(System.currentTimeMillis())
                if (scheduleTime > 0){
                    Log.d(TAG, "selectLastImageAndUpload: "+scheduleTime)
                    Log.d(TAG, "selectLastImageAndUpload: "+ TimeUnit.MILLISECONDS.toMinutes(scheduleTime))
                    Log.d(TAG, "selectLastImageAndUpload: "+sku.toProcessAt.toDate())
                    val handler = Handler(Looper.getMainLooper())

                    handler.postDelayed({
                        Log.d(TAG, "selectLastImageAndUpload: "+sku.toProcessAt.toDate())
                        GlobalScope.launch(Dispatchers.IO) {
                            processSku()
                        }
                    },scheduleTime)
                }
                else{
                    processSku()
                }
            }else {
                listener.onCompleted("All Skus Processed",SeverSyncTypes.PROCESS,false)
                Utilities.saveBool(context, AppConstants.PROJECT_SYNC_RUNNING, false)
            }
        }
    }

    private suspend fun updateTotalFrames(sku: Sku): Boolean {
        val properties = HashMap<String,Any?>()
            .apply {
                put("project_id",sku.projectId)
                put("sku_id",sku.skuId)
                put("data",Gson().toJson(sku))
            }

        val response = ShootRepository().updateTotalFrames(
            Utilities.getPreference(context,AppConstants.AUTH_KEY)!!,
            sku.skuId!!,
            sku.totalFrames.toString())

        context.captureEvent(
            Events.UPDATE_TOTAL_FRAMES_INITIATED,
            properties
        )

        if (response is Resource.Failure){
            context.captureEvent(
                Events.UPDATE_TOTAL_FRAMES_FAILED,
                properties.apply {
                    put("response",response)
                    put("throwable",response.throwable)
                }
            )
            retryCount++
            return false
        }

        context.captureEvent(
            Events.SKU_TOTAL_FRAMES_UPDATED,
            properties.apply {
                put("response",response)
            }
        )

        //update total frames updated
        sku.totalFramesUpdated = true
        val updateCount = shootDao.updateSku(sku)

        context.captureEvent(
            Events.SKU_TOTAL_FRAMES_IN_DB_UPDATED,
            properties.apply {
                put("response",response)
                put("db_count",updateCount)
            }
        )

        return true
    }

    private suspend fun processSku(sku: Sku) : Boolean {
        val properties = HashMap<String,Any?>()
            .apply {
                put("project_id",sku.projectId)
                put("sku_id",sku.skuId)
                put("data",Gson().toJson(sku))
            }

        val response = ProcessRepository().processSku(
            Utilities.getPreference(context,AppConstants.AUTH_KEY)!!,
            sku.skuId!!,
            sku.backgroundId!!,
            sku.isThreeSixty,
            sku.additionalData?.getBoolean("number_plate_blure")!!,
            sku.additionalData?.getBoolean("window_correction")!!,
            sku.additionalData?.getBoolean("tint_window")!!)

        context.captureEvent(
            Events.PROCESS_SKU_INTIATED,
            properties
        )

        if (response is Resource.Failure){
            context.captureEvent(
                Events.PROCESS_SKU_FAILED,
                properties.apply {
                    put("response",response)
                    put("throwable",response.throwable)
                }
            )
            retryCount++
            return false
        }

        context.captureEvent(
            Events.SKU_PROCESSED,
            properties.apply {
                put("response",response)
            }
        )

        //update sku processed
        sku.isProcessed = true
        val updateCount = shootDao.updateSku(sku)

        context.captureEvent(
            Events.SKU_PROCESSED_IN_DB_UPDATED,
            properties.apply {
                put("response",response)
                put("db_count",updateCount)
            }
        )
        retryCount = 0
        return true
    }





    private fun getRandomNumberInRange(): Int {
        val r = Random()
        return r.nextInt(100 - 10 + 1) + 10
    }
}