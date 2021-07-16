package com.spyneai

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.posthog.android.Properties
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events

fun Context.gotoHome(){
    val intent = Intent(this, MainDashboardActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}
fun Context.gotoLogin(){
    this.captureEvent(Events.LOG_OUT, Properties())

    Utilities.savePrefrence(this, AppConstants.TOKEN_ID, "")
    Utilities.savePrefrence(this, AppConstants.AUTH_KEY, "")
    Utilities.savePrefrence(this, AppConstants.PROJECT_ID, "")
    Utilities.savePrefrence(this, AppConstants.SHOOT_ID, "")
    Utilities.savePrefrence(this, AppConstants.SKU_ID, "")
    Intent.FLAG_ACTIVITY_CLEAR_TASK
    val intent = Intent(this, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}


fun Context.isValidGlideContext() = this !is Activity || (!this.isDestroyed && !this.isFinishing)