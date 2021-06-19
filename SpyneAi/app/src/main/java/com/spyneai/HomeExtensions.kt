package com.spyneai

import android.content.Context
import android.content.Intent
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.loginsignup.activity.LoginActivity

fun Context.gotoHome(){
    val intent = Intent(this, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}