package com.spyneai.interfaces


import com.google.gson.GsonBuilder
import com.spyneai.BuildConfig
import com.spyneai.needs.AppConstants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClients {


    private const val STAGING_URL = "http://34.87.119.10/api/"

    private val client = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.MINUTES)
        .writeTimeout(10, TimeUnit.MINUTES)
        .connectTimeout(10, TimeUnit.MINUTES)
        .retryOnConnectionFailure(true)
        .also { client ->
            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                client.addInterceptor(logging)
            }
        }.build()


    private val retrofit = Retrofit.Builder()
        .baseUrl(AppConstants.BASE_URL)
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


