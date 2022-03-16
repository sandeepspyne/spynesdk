package com.spyneai.threesixty.data

import com.spyneai.threesixty.data.model.VideoDetails

class VideoLocalRepoV2(val videoDao: VideoDao) {

    fun insertVideo(videoDetails: VideoDetails) = videoDao.insert(videoDetails)

    fun getVideo(uuid: String) = videoDao.getVideo(uuid)

    fun updateVideo(videoDetails: VideoDetails) = videoDao.update(videoDetails)

    fun updateVideoPath(uuid: String,path: String) = videoDao.updateVideoPath(uuid = uuid,path)

    fun updateVideoBackground(uuid: String,backgroundId: String,bgName: String?) = videoDao.updateVideoBackground(uuid,backgroundId,bgName)

    fun getOldestVideo() = videoDao.getOldestVideo()

    fun skipVideo(uuid: String, toProcessAt: Long) = videoDao.skipVideo(uuid,toProcessAt)

    fun getVideoPath(uuid: String) = videoDao.getVideoPath(uuid)

    fun getVideoByProjectUuid(uuid: String) = videoDao.getVideoByProjectUuid(uuid)


    fun getVideoId(uuid: String) = videoDao.getVideoId(uuid)

    fun totalRemainingUpload() = videoDao.totalRemainingUpload()

    fun totalRemainingMarkDone() = videoDao.totalRemainingUpload(true, isMarkedDone = false)
}