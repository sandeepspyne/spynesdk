package com.spyneai.aipack

import retrofit2.http.Part

data class FetchBulkUploadRequest (

        val user_id : String,
        val sku_id : String,
)

