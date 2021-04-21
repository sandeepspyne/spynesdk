package com.spyneai.videorecording.model

class VideoTask {
    var user_id : String = ""
    var skuName: String = ""
    var skuId: String = ""
    var type: String = ""
    var category: String = ""
    var isCompleted = false
    var onFailure = false
    var filePath : String = ""
    // 0 for upload 1 for processing
    var taskType = 0
    var responseUrl : String = ""
    var videoUrl : String = ""
    lateinit var frames : List<String>
    var shootMode = 0
}