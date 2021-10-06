package com.spyneai.model.uploadRough

import com.google.gson.annotations.SerializedName

data class PhotoNode(
        @SerializedName("photoId")
        val photoId: String,
        @SerializedName("photoLabel")
        val photoLabel: String,
        @SerializedName("displayThumbnail")
        val displayThumbnail: String,
        @SerializedName("rawPhotoUrl")
        val rawPhotoUrl: String,
        @SerializedName("editedPhotoUrl")
        val editedPhotoUrl: String,
        @SerializedName("editedPhotoDisplayThumbnail")
        val editedPhotoDisplayThumbnail: String,
        @SerializedName("category")
        val category: String,
        @SerializedName("photoName")
        val photoName: String,
        @SerializedName("folderPath")
        val folderPath: String,
        @SerializedName("indexNumber")
        val indexNumber: Int
)






