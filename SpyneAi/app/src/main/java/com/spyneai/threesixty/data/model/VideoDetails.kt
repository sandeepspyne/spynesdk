package com.spyneai.threesixty.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class VideoDetails {
    @PrimaryKey(autoGenerate = false)
    var uuid: String? = ""
    var itemId : Long? = null
    var projectId : String? = null
    var skuName : String? = null
    var skuId : String? = null
    var categoryName : String = "Automobiles"
    var categoryId : String? = null
    var subCategory : String = "sedan"
    var type : String = "360_exterior"
    var videoPath : String? = null
    var shootMode : Int = 0
    var frames : Int = 0
    var backgroundId : String? = null
    var sample360 : String? = null
    var isUploaded : Int? = 0
    var isStatusUpdate : Int? = 0
    var preSignedUrl : String? = null
    var videoId : String? = null
}
