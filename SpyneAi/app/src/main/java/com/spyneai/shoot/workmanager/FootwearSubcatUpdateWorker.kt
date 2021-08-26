package com.spyneai.shoot.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.posthog.android.Properties
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ProcessRepository
import com.spyneai.shoot.data.ShootRepository

class FootwearSubcatUpdateWorker (private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    val TAG = "FootwearSubcatUpdateWorker"
    val shootRepository = ShootRepository()

    override suspend fun doWork(): Result {

        var response = shootRepository.updateFootwearSubcategory(
            Utilities.getPreference(appContext,AppConstants.AUTH_KEY).toString(),
            inputData.getString("sku_id")!!,
            inputData.getInt("initial_image_count",0)!!,
            inputData.getString("sub_cat_id")!! )

        when(response) {
            is Resource.Success -> {
                com.spyneai.shoot.utils.log("Frames updated success")
                captureEvent(Events.FOOTWAER_SUBCAT_UPDATED,true,null)

                return Result.success()
            }

            is Resource.Failure -> {
                if(response.errorMessage == null){
                    captureEvent(Events.FOOTWAER_SUBCAT_UPDATE_FAILED,false,response.errorCode.toString()+": Http exception from server")
                }else {
                    captureEvent(Events.FOOTWAER_SUBCAT_UPDATE_FAILED,false,response.errorCode.toString()+": "+response.errorMessage)
                }

                Result.retry()
            }
        }

        return Result.success()
    }


    private fun captureEvent(eventName : String,isSuccess : Boolean, error: String?) {
        val properties = Properties()
        properties.apply {
            this["sku_id"] = inputData.getString("sku_id")
            this["initial_image_count"] = inputData.getInt("initial_image_count",0)
            this["sub_cat_id"] = inputData.getString("sub_cat_id")
        }

        if (isSuccess) {
            appContext.captureEvent(
                eventName,
                properties)
        }else{
            appContext.captureFailureEvent(
                eventName,
                properties, error!!
            )
        }
    }
}