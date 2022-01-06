package com.spyneai.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.spyneai.captureEvent
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.ImageLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.repository.db.ShootDao
import com.spyneai.shoot.repository.model.project.ProjectWithSku
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

class ProjectSkuSync(
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
            val projectWithSku = shootDao.getProjectWithSkus()

            projectWithSku?.project?.let {
                Log.d(TAG, "startUploading: "+Gson().toJson(it).toString())
            }

            projectWithSku?.skus?.let {
                Log.d(TAG, "startUploading: "+Gson().toJson(it).toString())
            }

        }while (projectWithSku != null)

        Log.d(TAG, "startUploading: all done")
    }

    private fun getRandomNumberInRange(): Int {
        val r = Random()
        return r.nextInt(100 - 10 + 1) + 10
    }
}