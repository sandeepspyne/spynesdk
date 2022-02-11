package com.spyneai.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.spyneai.checkPendingDataSync

class ExplicitConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            it.checkPendingDataSync()
        }
    }
}