package com.spyneai.videorecording.service

import com.spyneai.videorecording.model.VideoTask

class FramesHelper {
    companion object {
        var hashMap = HashMap<String,ArrayList<String>> ()
        var videoUrlMap = HashMap<String,String>()
        var processingMap = HashMap<String,Boolean>()
        var taskMap = HashMap<String, VideoTask>()


    }
}