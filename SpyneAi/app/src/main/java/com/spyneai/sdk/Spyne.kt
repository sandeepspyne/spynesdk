package com.spyneai.sdk

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.spyneai.BaseApplication
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.ShootRepository


class Spyne(
    val context: Context,
    val shootRepository: ShootRepository
) {

    companion object {
        var intent: Intent? = null
        var apiKey: String? = null
        var email: String? = null
        var userId: String? = null
        var contactNo: String? = null
        var enterpriseid: String? = null

        fun init(context: Context, apiKey: String, enterpriseid: String) {
            this.apiKey = apiKey
            this.enterpriseid = enterpriseid
            Utilities.savePrefrence(context, AppConstants.API_KEY, apiKey)
            Utilities.savePrefrence(context, AppConstants.ENTERPRISE_ID, enterpriseid)
        }

        fun start(
            applicationContext: Context,
            context: Context,
            email: String,
            contactNo: String,
            userId: String,
            intent: Intent
        ) {
            BaseApplication.setContext(applicationContext)
            this.intent = intent
            this.email = email
            this.userId = userId
            this.contactNo = contactNo

            //go to SignupSDK from here once SDK is integrated with app
            try {
                val signupIntent = Intent(context, SignupSDK::class.java)
                context.startActivity(signupIntent)
            }catch (e : Exception){
                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

    }
}