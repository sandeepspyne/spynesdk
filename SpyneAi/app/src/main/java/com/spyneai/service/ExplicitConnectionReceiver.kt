package com.spyneai.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.spyneai.captureEvent
import com.spyneai.isMyServiceRunning
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ImageLocalRepository

class ExplicitConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val shootLocalRepository = ImageLocalRepository()
        if (shootLocalRepository.getOldestImage("0").itemId != null
            || shootLocalRepository.getOldestImage("-1").itemId != null){
                if (context != null){

                    val prperties = HashMap<String,Any?>()
                        .apply {
                            put("email", Utilities.getPreference(context, AppConstants.EMAIL_ID).toString())
                            put("medium","Explicit Broadcast")
                        }

                    if (!context?.isMyServiceRunning(ImageUploadingService::class.java)){
                        Utilities.saveBool(context, AppConstants.UPLOADING_RUNNING, false)

                        var action = Actions.START
                        if (getServiceState(context) == ServiceState.STOPPED && action == Actions.STOP)
                            return

                        val serviceIntent = Intent(context, ImageUploadingService::class.java)
                        serviceIntent.putExtra(AppConstants.SERVICE_STARTED_BY,
                            ExplicitConnectionReceiver::class.simpleName)
                        serviceIntent.action = action.name

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            log("Starting the service in >=26 Mode")
                            ContextCompat.startForegroundService(context, serviceIntent)
                            return
                        } else {
                            log("Starting the service in < 26 Mode")
                            context.startService(serviceIntent)
                        }

                        prperties.put("state","Started")
                        context.captureEvent(Events.SERVICE_STARTED,prperties)
                    }else {
                        prperties.put("state","Running")
                        context.captureEvent(Events.SERVICE_STARTED,prperties)
                    }
                }
        }
    }


}