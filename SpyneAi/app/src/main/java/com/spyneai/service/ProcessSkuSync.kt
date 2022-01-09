package com.spyneai.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.spyneai.Resource
import com.spyneai.captureEvent
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.ProcessRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.repository.db.ShootDao
import com.spyneai.shoot.repository.model.project.ProjectBody
import com.spyneai.shoot.repository.model.sku.Sku
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ProcessSkuSync(
    val context: Context,
    val shootDao: ShootDao,
    var lastIdentifier: String = "0",
    var retryCount: Int = 0,
    var connectionLost: Boolean = false
) {

    val TAG = "ProjectSkuSync"

    fun uploadParent(type : String,startedBy : String?) {
//        context.captureEvent("UPLOAD PARENT TRIGGERED",HashMap<String,Any?>().apply {
//            put("type",type)
//            put("service_started_by",startedBy)
//            put("upload_running", Utilities.getBool(context, AppConstants.UPLOADING_RUNNING, false))
//        })

        //update triggered value
        Utilities.saveBool(context, AppConstants.UPLOAD_TRIGGERED, true)

        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            if (Utilities.getBool(context, AppConstants.UPLOAD_TRIGGERED, true)
                &&
                !Utilities.getBool(context, AppConstants.UPLOADING_RUNNING, false)
            ) {
                if (context.isInternetActive())
                    GlobalScope.launch(Dispatchers.Default) {
                        Log.d(TAG, "uploadParent: start")
                        // Utilities.saveBool(context, AppConstants.UPLOADING_RUNNING, true)
                        context.captureEvent("START UPLOADING CALLED",HashMap())
                        startUploading()
                    }
                else {
                    //Utilities.saveBool(context, AppConstants.UPLOADING_RUNNING, false)
                    //listener.onConnectionLost()
                    Log.d(TAG, "uploadParent: connection lost")
                }
            }
        }, getRandomNumberInRange().toLong())
    }

    suspend fun  startUploading(){
        do {
            val sku = shootDao.getProcessAbleSku() ?: break

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

        }while (sku != null)

        Log.d(TAG, "startUploading: all done")
    }

    private suspend fun processSku(sku: Sku) : Boolean {
        val response = ProcessRepository().processSku(
            Utilities.getPreference(context,AppConstants.AUTH_KEY)!!,
            sku.skuId!!,
            sku.backgroundId!!,
            sku.isThreeSixty,
            sku.additionalData?.getBoolean("number_plate_blure")!!,
            sku.additionalData?.getBoolean("window_correction")!!,
            sku.additionalData?.getBoolean("tint_window")!!)

        if (response is com.spyneai.base.network.Resource.Failure)
            return false

        //update sku processed
        sku.isProcessed = true
        shootDao.updateSku(sku)

        return true
    }

    private suspend fun updateTotalFrames(sku: Sku): Boolean {
        val response = ShootRepository().updateTotalFrames(
            Utilities.getPreference(context,AppConstants.AUTH_KEY)!!,
            sku.skuId!!,
            sku.totalFrames.toString())

        if (response is com.spyneai.base.network.Resource.Failure)
            return false

        //update total frames updated
        sku.totalFramesUpdated = true
        shootDao.updateSku(sku)
        return true
    }



    private fun getRandomNumberInRange(): Int {
        val r = Random()
        return r.nextInt(100 - 10 + 1) + 10
    }
}