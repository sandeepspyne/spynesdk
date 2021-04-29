package com.spyneai.credits

import com.spyneai.credits.model.CreditPlansRes
import com.spyneai.videorecording.model.VideoProcessingResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CreditApiService {

    @GET("credit")
    fun getThreeSixtyInteriorByShootId(
    ): Call<CreditPlansRes>?
}