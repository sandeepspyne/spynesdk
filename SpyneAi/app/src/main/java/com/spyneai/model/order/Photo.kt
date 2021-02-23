package com.spyneai.model.order

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Photo(

        val photoId: String,
        val photoLabel: String,

        val displayThumbnail: String,

        val rawPhotoUrl: String,

        val editedPhotoUrl: String,

        val editedPhotoDisplayThumbnail: String,

        val category: String,

        val photoName: String,

        val folderPath: String,

        val accepted: Boolean,
        val status: String,
        val comment: String,
)
