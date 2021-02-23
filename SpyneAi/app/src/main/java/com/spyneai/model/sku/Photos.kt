package com.spyneai.model.sku

data class Photos(
    val photoId : String,
    val photoLabel : String,
    val displayThumbnail : String,
    val rawPhotoUrl : String,
    val editedPhotoUrl : String,
    val editedPhotoDisplayThumbnail : String,
    val category : String,
    val photoName : String,
    val folderPath : String,
    val indexNumber : Int,
    val accepted : Boolean,
    val status : String,
    val comment : String
)
