package com.spyneai.model.ai

data class WaterMarkResponse(
    val status : Int,
    val message :  String,
    val output_url : String,
    val input_url : String
)
