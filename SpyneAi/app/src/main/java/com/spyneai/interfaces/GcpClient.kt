package com.spyneai.interfaces

import com.google.gson.GsonBuilder
import com.spyneai.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object GcpClient {

    private const val BASE_URL = "https://storage.googleapis.com"

    private val client = OkHttpClient.Builder()
        .readTimeout(4, TimeUnit.MINUTES)
        .writeTimeout(4, TimeUnit.MINUTES)
        .connectTimeout(4, TimeUnit.MINUTES)
        .retryOnConnectionFailure(true)
        .also { client ->
            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                client.addInterceptor(logging)
            }
        }.build()


    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    .setLenient()
                    .create()
            )
        )
        .build()

    fun <T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }

}


