package com.spyneai.threesixty.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.spyneai.threesixty.data.model.VideoDetails
import java.util.*

@Dao
interface VideoDao {

    @Insert
    fun insert(videoDetails: VideoDetails): Long

    @Update
    fun update(videoDetails: VideoDetails): Int

    @Query("select * from videodetails where uuid= :id")
    fun getVideo(id: String) : VideoDetails

    @Query("select * from videodetails where skuId NOT NUll and projectId NOT NULL and isUploaded = :isUploaded or isMarkedDone = :isMarkedDone and toProcessAt <= :currentTime limit 1")
    fun getOldestVideo(isUploaded: Boolean = false,isMarkedDone : Boolean = false,currentTime: Long = System.currentTimeMillis()) : VideoDetails

    @Query("update videodetails set toProcessAt = :toProcessAt, retryCount = retryCount + 1 where uuid = :uuid")
    fun skipVideo(uuid: String,toProcessAt: Long) : Int

    @Query("select videoPath from videodetails where uuid= :id")
    fun getVideoPath(id: String) : String

    @Query("select videoId from videodetails where uuid= :id")
    fun getVideoId(id: String) : String

    @Query("select Count(*) from videodetails where isUploaded = :isUploaded and isMarkedDone = :isMarkedDone")
    fun totalRemainingUpload(isUploaded: Boolean = false,isMarkedDone : Boolean = false) : Int

}