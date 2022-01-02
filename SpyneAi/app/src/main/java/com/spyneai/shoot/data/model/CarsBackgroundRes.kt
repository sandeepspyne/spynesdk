package com.spyneai.shoot.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class CarsBackgroundRes(
    val `data`: List<Background>,
    val message: String,
    val status: Int
) {
    @Entity
    data class Background(
        @PrimaryKey(autoGenerate = true)
        val id : Int,
        var category : String,
        val bgName: String,
        val gifUrl: String,
        val imageCredit: Int,
        val imageId: String,
        val imageUrl: String
    )
}