package com.spyneai.model.uploadRough

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Frame(
        val displayImage : String,
        val frameNumber : Int
)