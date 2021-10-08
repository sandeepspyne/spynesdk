package com.spyneai.shoot.data.model

import org.json.JSONObject

class Image {
    var itemId : Long? = null
    var projectId : String? = null
    var skuId : String? = null
    var skuName : String? = null
    var categoryName : String? = null
    var imagePath : String? = null
    var sequence : Int? = null
    var isUploaded : Int? = null
    var angle : Int? = null
    var meta : String? = null
    var name : String? = null
    var preSignedUrl : String? = null
    var imageId : String? = null
    var overlayId : String? = null
    var debugData : String? = null
    var isStatusUpdated : Int? = null
    var isReclick = 0
    var isReshoot = 0
}