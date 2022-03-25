package com.spyneai.shoot.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

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
        @SerializedName("backgroundCredit")
        val imageCredit: Int,
        @SerializedName("backgroundId")
        val imageId: String,
        @SerializedName("lowResImageUrl")
        val imageUrl: String
    )
}