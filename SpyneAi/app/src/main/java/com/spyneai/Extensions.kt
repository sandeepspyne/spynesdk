package com.spyneai

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.spyneai.dashboard.ui.MainDashboardActivity

fun Context.gotoHome(){
    val intent = Intent(this, MainDashboardActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}

fun Context.isValidGlideContext() = this !is Activity || (!this.isDestroyed && !this.isFinishing)