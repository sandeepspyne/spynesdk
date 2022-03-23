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
        var userId: String? = null
        var foreignSkuId: String? = null
        var enterpriseid: String? = null

        fun init(context: Context, apiKey: String,categoryId: String) {
            BaseApplication.setContext(context)
            this.apiKey = apiKey
            this.enterpriseid = enterpriseid
            Utilities.savePrefrence(context, AppConstants.CATEGORY_ID, categoryId)
            Utilities.savePrefrence(context, AppConstants.API_KEY, apiKey)
            Utilities.saveBool(context, AppConstants.FROM_SDK, true)
        }

        fun start(
            context: Context,
            userId: String,
            foreignSkuId: String,
            intent: Intent
        ) {
            this.userId = userId
            this.foreignSkuId = foreignSkuId
            this.intent = intent

            //go to SignupSDK from here once SDK is integrated with app
            val signupIntent = Intent(context, SignupSDK::class.java)
            context.startActivity(signupIntent)
        }

    }
}