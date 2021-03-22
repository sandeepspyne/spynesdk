package com.spyneai.model.order

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Photo(
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
        @SerializedName("accepted")
        val accepted: Boolean,
        @SerializedName("status")
        val status: String,
        @SerializedName("comment")
        val comment: String
)
