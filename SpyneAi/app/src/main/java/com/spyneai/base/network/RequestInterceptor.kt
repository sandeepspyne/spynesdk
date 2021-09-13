package com.spyneai.base.network

import com.posthog.android.Properties
import com.spyneai.BaseApplication
import com.spyneai.captureEvent
import com.spyneai.getRequestHeaderData
import com.spyneai.posthog.Events
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONException
import org.json.JSONObject

class RequestInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("device_details", getRequestHeaderData().toString())
            .build()

        return chain.proceed(request)
    }
}