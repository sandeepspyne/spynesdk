package com.spyneai.dashboard.response

data class NewCategoriesResponse(
    val `data`: List<Data>,
    val message: String,
    val status: Int
)