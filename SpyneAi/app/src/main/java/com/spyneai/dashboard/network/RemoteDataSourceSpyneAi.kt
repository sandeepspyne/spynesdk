package com.spyneai.dashboard.network

import com.spyneai.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.logging.Level

class RemoteDataSourceSpyneAi{
    companion object{
        private const val BASE_URL = "https://api.spyne.ai/"
    }

    fun<Api> buildApi(
        api: Class<Api>
    ): Api{
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().also { client ->
                if (BuildConfig.DEBUG) {
                    val logging = HttpLoggingInterceptor()
                    logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
                    client.addInterceptor(logging)
                }
            }.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }

}