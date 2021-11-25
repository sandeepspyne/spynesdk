package com.spyneai.debug

import android.content.Context
import com.posthog.android.Properties
import com.spyneai.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.FilesRepository
import com.spyneai.shoot.data.ImageLocalRepository
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class SendSkusData(
    val shootRepository: ShootRepository,
                   val imageLocalRepository: ImageLocalRepository
)  {

    suspend fun startWork(){
        //get all sku from local DB
        val images = imageLocalRepository.getAllImages()

        val properties = Properties()
        properties.apply {
            this["size"] = images.size
        }

        BaseApplication.getContext().captureEvent(
            Events.SKU_LOCAL_REAED_FINISHED,
            properties)

        val imageList = JSONArray()

        images.forEach {
            val data = JSONObject()
            data.put("image_id",it.itemId)
            data.put("project_id",it.projectId)
            data.put("sku_name",it.skuName)
            data.put("sku_id",it.skuId)
            data.put("sequence_no",it.sequence)
            data.put("image_name",it.name)
            data.put("upload_status",it.isUploaded)
            data.put("mark_done_status",it.isStatusUpdated)
            data.put("presigned_url",it.preSignedUrl)

            imageList.put(data.toString())
        }

        startManualUploadWorker(imageList)
    }

    private suspend fun startManualUploadWorker(filesPathList : JSONArray) {
        val properties = Properties()
        properties.apply {
            this["email"] = Utilities.getPreference(BaseApplication.getContext(), AppConstants.EMAIL_ID).toString()
        }

        BaseApplication.getContext().captureEvent(
            Events.SKU_REAED_FINISHED,
            properties)


        sendData(0,filesPathList.toString(),properties)

    }

    private suspend fun sendData(count : Int,data : String,properties: Properties) {

        //send all data to server
        var sendDataRes = ShootRepository().sendFilesData(
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString(),
            data
        )

        when(sendDataRes){
            is Resource.Success -> {
                BaseApplication.getContext().captureEvent(
                    Events.SKU_DATA_SENT,
                    properties
                )

                Utilities.saveBool(BaseApplication.getContext(),Events.IS_SKU_DATA_SENT,true)

            }


            is Resource.Failure -> {

                if (count <= 5){
                    sendData(count.plus(1),data,properties)
                }
                properties["error"] = sendDataRes.errorMessage

                BaseApplication.getContext().captureEvent(
                    Events.SKU_DATA_SENT_FAILED,
                    properties
                )
            }
        }
    }

}