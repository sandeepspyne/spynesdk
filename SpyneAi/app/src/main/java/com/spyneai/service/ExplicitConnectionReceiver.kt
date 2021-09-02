package com.spyneai.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.ListenableWorker
import com.posthog.android.Properties
import com.spyneai.captureEvent
import com.spyneai.isMyServiceRunning
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootLocalRepository

class ExplicitConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {


        val shootLocalRepository = ShootLocalRepository()
        if (shootLocalRepository.getOldestImage().itemId != null
            || shootLocalRepository.getOldestSkippedImage().itemId != null){
                if (context != null){

                    val prperties = Properties()
                        .apply {
                            put("email", Utilities.getPreference(context, AppConstants.EMAIL_ID).toString())
                            put("medium","Explicit Broadcast")
                        }

                    if (!context?.isMyServiceRunning(ImageUploadingService::class.java)){
                        var action = Actions.START
                        if (getServiceState(context) == com.spyneai.service.ServiceState.STOPPED && action == Actions.STOP)
                            return

                        val serviceIntent = Intent(context, ImageUploadingService::class.java)
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