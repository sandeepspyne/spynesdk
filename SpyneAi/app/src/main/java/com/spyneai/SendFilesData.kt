package com.spyneai

import com.google.gson.Gson
import com.spyneai.base.network.Resource
import com.spyneai.base.room.AppDatabase
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SendFilesData {

    public fun start(){
        val properties = HashMap<String,Any?>()
        properties.apply {
            this["email"] = Utilities.getPreference(BaseApplication.getContext(), AppConstants.EMAIL_ID).toString()
        }

        //send all data to server
        GlobalScope.launch(Dispatchers.IO) {
            BaseApplication.getContext().captureEvent(
                Events.SKU_DATA_SEND_STARTED,
                properties)
            sendData()
        }
    }

    private suspend fun sendData() {
        val appDataBase = AppDatabase.getInstance(BaseApplication.getContext())
        val properties = HashMap<String,Any?>()
        properties.apply {
            this["email"] = Utilities.getPreference(BaseApplication.getContext(), AppConstants.EMAIL_ID).toString()
        }

        val data = HashMap<String,String>()

        data["projects"] = Gson().toJson(appDataBase.projectDao().getAllProjects())
        data["skus"] = Gson().toJson(appDataBase.skuDao().getAllSKus())
        data["images"] = Gson().toJson(appDataBase.imageDao().getAllImages())

        var sendDataRes = ShootRepository().sendFilesData(
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString(),
            Gson().toJson(data)
        )

        when(sendDataRes){
            is Resource.Success -> {
                BaseApplication.getContext().captureEvent(
                    Events.SKU_DATA_SENT,
                    properties
                )
                Utilities.saveBool(BaseApplication.getContext(),AppConstants.IS_SKU_DATA_SENT,true)
            }

            is Resource.Failure -> {
                properties["error"] = sendDataRes.errorMessage

                BaseApplication.getContext().captureEvent(
                    Events.SKU_DATA_SENT_FAILED,
                    properties
                )
                sendData()
            }
        }
    }
}