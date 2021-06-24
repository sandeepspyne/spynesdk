package com.spyneai.shoot.data.model

data class CarsBackgroundRes(
    val `data`: List<Data>,
    val message: String,
    val status: Int
) {
    data class Data(
        val bgName: String,
        val gifUrl: String,
        val imageCredit: Int,
        val imageId: String,
        val imageUrl: String
    )
}