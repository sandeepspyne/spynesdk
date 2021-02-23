package com.spyneai.model.uploadRough

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PhotoNode(
        val photoId: String,
        val photoLabel: String,
        val displayThumbnail: String,
        val rawPhotoUrl: String,
        val editedPhotoUrl: String,
        val editedPhotoDisplayThumbnail: String,
        val category: String,
        val photoName: String,
        val folderPath: String,
        val indexNumber: Int
)






