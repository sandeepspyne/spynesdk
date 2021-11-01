package com.spyneai

import android.content.Context
import com.posthog.android.PostHog
import com.posthog.android.Properties

import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

fun Context.captureEvent(eventName : String, items: HashMap<String,Any?>) {

    val properties = Properties()
    properties.putValue("app_name",getString(R.string.app_name))

    items.keys.forEach {
        properties.putValue(it,items[it])
    }

    PostHog.with(this)
        .capture(eventName, properties)
}

fun Context.captureFailureEvent(eventName : String,items: HashMap<String,Any?>,message : String) {
    val properties = Properties()
    properties.putValue("message",message)
    properties.putValue("app_name",getString(R.string.app_name))

    items.keys.forEach {
        properties.putValue(it,items[it])
    }

    PostHog.with(this)
        .capture(eventName, properties)
}

fun Context.captureIdentity(userId : String,items: HashMap<String,Any?>) {
    //save email
    Utilities.savePrefrence(this,AppConstants.EMAIL_ID,items.get("email").toString())
    Utilities.savePrefrence(this, AppConstants.TOKEN_ID, items.get("user_id").toString())
    Utilities.savePrefrence(this, AppConstants.USER_NAME, items.get("name").toString())
    Utilities.savePrefrence(this, AppConstants.USER_EMAIL, items.get("email").toString())

    val properties = Properties()
    properties.putValue("app_name",getString(R.string.app_name))

    items.keys.forEach {
        properties.putValue(it,items[it])
    }

    PostHog.with(this)
        .identify(userId,properties)
}