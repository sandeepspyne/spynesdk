package com.spyneai.interfaces

import android.util.Base64
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClientSpyneAis {

    private val AUTH = "Basic "+ Base64.encodeToString("belalkhan:123456".toByteArray(), Base64.NO_WRAP)

    private const val BASE_URL = "https:www.//spyne.ai/"

    private val client = OkHttpClient.Builder()
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .connectTimeout(2, TimeUnit.MINUTES)
        .retryOnConnectionFailure(true)
            .build()

    private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }

}