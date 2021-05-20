package com.spyneai.imagesdowloading

class DownloadTask {
    var user_id : String = ""
    var skuName: String = ""
    var skuId: String = ""
    var type: String = ""
    var category: String = ""
    var isCompleted = false
    var onFailure = false
    var isFailure = false
    var isDownloadedBefore = false
    var retry = 0
    var downloadCount = 0
    var remainingCredits = 0
    var creditsToReduce = 0
    var price = 0
    var listHdQuality = ArrayList<String>()
    var failureNotified = false
    var imageDir : String = ""
}