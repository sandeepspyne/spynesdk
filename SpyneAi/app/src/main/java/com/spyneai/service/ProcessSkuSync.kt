package com.spyneai.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.spyneai.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.base.room.AppDatabase
import com.spyneai.captureEvent
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ProcessRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.repository.db.ShootDao
import com.spyneai.shoot.repository.model.sku.Sku
import io.sentry.protocol.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

class ProcessSkuSync(
    val context: Context,
    val shootDao: ShootDao,
    val listener: DataSyncListener,
    var retryCount: Int = 0,
    var connectionLost: Boolean = false,
    var isActive: Boolean = false
) {

    val TAG = "ProcessSkuSync"
    companion object{
        @Volatile
        private var INSTANCE: ProcessSkuSync? = null

        fun getInstance(context: Context,listener: DataSyncListener): ProcessSkuSync {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = ProcessSkuSync(
                        context,
                        AppDatabase.getInstance(BaseApplication.getContext()).shootDao(),
                        listener
                    )

                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    fun processSkuParent(type : String,startedBy : String?) {
        Log.d(TAG, "processSkuParent: ")
        context.captureEvent(Events.PROCESS_SKU_PARENT_TRIGGERED,HashMap<String,Any?>().apply {
            put("type",type)
            put("service_started_by",startedBy)
            put("upload_running", isActive)
        })

        //update triggered value
        Utilities.saveBool(context, AppConstants.PROCESS_SKU_PARENT_TRIGGERED, true)

        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            if (Utilities.getBool(context, AppConstants.PROCESS_SKU_PARENT_TRIGGERED, true)
                &&
                !isActive
            ) {
                if (context.isInternetActive())
                    GlobalScope.launch(Dispatchers.Default) {
                         isActive = true
                        context.captureEvent(Events.PROCESS_SKU_STARTED,HashMap())
                        processSku()
                    }
                else {
                   isActive = false
                    listener.onConnectionLost("Process Sku Stopped",ServerSyncTypes.PROCESS)
                    Log.d(TAG, "uploadParent: connection lost")
                }
            }else{
                Log.d(TAG, "processSkuParent: running")
            }
        }, getRandomNumberInRange().toLong())
    }

    private suspend fun  processSku(){
        do {
            Log.d(TAG, "processSku: ")
            val sku = shootDao.getProcessAbleSku() ?: break

            Log.d(TAG, "processSku: "+Gson().toJson(sku))
            
            if (connectionLost){
                val count = shootDao.getPendingSku()
                context.captureEvent(
                    Events.PROCESS_SKU_CONNECTION_CONNECTION_BREAK,
                    HashMap<String,Any?>()
                        .apply {
                            put("sku_remaining",count)
                        }
                )
                isActive = false
                listener.onConnectionLost("Process Sku Stopped",ServerSyncTypes.CREATE)
                break
            }
            else {
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
                listener.inProgress("Processing Sku ${sku.skuName}",ServerSyncTypes.PROCESS)
                isActive = true


                if (sku.totalFramesUpdated){
                    processSku(sku)
                }else {
                    //update total frames
                    if (sku.totalFrames!! > sku.initialFrames!!){
                        val isTotalFramesUpdated = updateTotalFrames(sku)

                        if (!isTotalFramesUpdated)
                            continue

                        processSku(sku)

                        continue
                    }else {
                        processSku(sku)

                        continue
                    }
                }
            }
        }while (sku != null)

        if (!connectionLost){
            listener.onCompleted("All Skus Background Id Updated",ServerSyncTypes.PROCESS)
            isActive = false
            //get pending projects count
//            val count = shootDao.getPendingSku()
//
//            Log.d(TAG, "processSku: count $count")
//
//            context.captureEvent(
//                Events.ALL_SKUS_PROCESSED_BREAKS,
//                HashMap<String,Any?>().apply {
//                    put("sku_remaining",count)
//                }
//            )
//
//            if (count > 0){
//                val sku = shootDao.getOldestSku()
//                val scheduleTime = sku.toProcessAt.minus(System.currentTimeMillis())
//                if (scheduleTime > 0){
//                    Log.d(TAG, "selectLastImageAndUpload: "+scheduleTime)
//                    Log.d(TAG, "selectLastImageAndUpload: "+ TimeUnit.MILLISECONDS.toMinutes(scheduleTime))
//                    Log.d(TAG, "selectLastImageAndUpload: "+sku.toProcessAt.toDate())
//                    val handler = Handler(Looper.getMainLooper())
//
//                    handler.postDelayed({
//                        Log.d(TAG, "selectLastImageAndUpload: "+sku.toProcessAt.toDate())
//                        GlobalScope.launch(Dispatchers.IO) {
//                            processSku()
//                        }
//                    },scheduleTime)
//                }
//                else{
//                    processSku()
//                }
//            }else {
//                Log.d(TAG, "processSku: all processed")
//                listener.onCompleted("All Skus Processed",SeverSyncTypes.PROCESS,false)
//                Utilities.saveBool(context, AppConstants.PROJECT_SYNC_RUNNING, false)
//            }
        }
    }

    private suspend fun updateTotalFrames(sku: Sku): Boolean {
        Log.d(TAG, "updateTotalFrames: "+sku.totalFrames)
        val properties = HashMap<String,Any?>()
            .apply {
                put("project_id",sku.projectId)
                put("sku_id",sku.skuId)
                put("data",Gson().toJson(sku))
            }

        val response = ShootRepository().updateTotalFrames(
            sku.skuId!!,
            sku.totalFrames.toString(),
            Utilities.getPreference(context,AppConstants.AUTH_KEY)!!)

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

        //val additionalData = JSONObject(sku.additionalData)

        val response = when(sku.categoryId){
            AppConstants.CARS_CATEGORY_ID,
            AppConstants.BIKES_CATEGORY_ID-> {
                ProcessRepository().processSku(
                    Utilities.getPreference(context,AppConstants.AUTH_KEY)!!,
                    sku.skuId!!,
                    sku.backgroundId!!,
                    sku.isThreeSixty,
                    false,
                    false,
                    false)
            }

            AppConstants.FOOTWEAR_CATEGORY_ID -> {
                ShootRepository().skuProcessState(
                    auth_key = Utilities.getPreference(context,AppConstants.AUTH_KEY).toString(),
                    sku.projectId!!,)
            }

            AppConstants.FOOD_AND_BEV_CATEGORY_ID -> {
                ProcessRepository().skuProcessStateWithBackgroundId(
                    auth_key = Utilities.getPreference(context,AppConstants.AUTH_KEY).toString(),
                    sku.projectId!!,
                    sku.backgroundId.toInt())
            }

            else -> {
                ShootRepository().skuProcessStateWithShadowOption(
                    auth_key = Utilities.getPreference(context,AppConstants.AUTH_KEY).toString(),
                    sku.projectId!!,
                    sku.backgroundId.toInt(),
                    false.toString())
            }
        }



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

        Log.d(TAG, "processSku: ")
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