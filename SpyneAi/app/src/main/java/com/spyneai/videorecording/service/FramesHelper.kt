package com.spyneai.videorecording.service

import com.spyneai.videorecording.model.VideoProcessingResponse
import com.spyneai.videorecording.model.VideoTask

class FramesHelper {
    companion object {
        var framesMap = HashMap<String,VideoProcessingResponse> ()
        var videoUrlMap = HashMap<String,String>()
        var processingMap = HashMap<String,Boolean>()
        var taskMap = HashMap<String, VideoTask>()
    }
}