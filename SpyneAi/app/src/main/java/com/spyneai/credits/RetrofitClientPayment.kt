package com.spyneai.credits

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClientPayment {
    private const val BASE_URL = "https://payment.spyne.ai/"
    private const val STAGING_URL = "http://34.87.119.10/api/"

    private val client = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.MINUTES)
        .writeTimeout(10, TimeUnit.MINUTES)
        .connectTimeout(10, TimeUnit.MINUTES)
        .addInterceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader("content-type","application/json")
                .build()

            chain.proceed(newRequest)
        }
        .build()


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

    fun <T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}