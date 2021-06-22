package com.spyneai.base.network

import android.util.Log
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException


class ResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val response = chain.proceed(request)

        if (response.body != null){
            val body = response.body
            val bodyString = body!!.string()
            val contentType: MediaType? = body!!.contentType()
            val json = JSONObject(bodyString)
            Log.d("ResponseInterceptor", "intercept: " + json)

            var finalResponse = response.newBuilder().body(bodyString.toResponseBody(contentType)).build()


            if (json.has("status") && json.getInt("status") != 200) {
                throw ServerException(json.getInt("status"),json.getString("message"))
            }

            return finalResponse
        }


        return response

    }
}