package com.spyneai.model.projects

import com.spyneai.model.dashboard.Payload
import com.spyneai.model.login.Header
import com.spyneai.model.login.MsgInfo

data class CompletedProjectResponse(

    val output_image_url: String,
    val sku_id: String,
    val sku_name: String,
    val created_at: String,

)
