package com.spyneai.threesixty.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.spyneai.threesixty.data.model.VideoDetails
import java.util.*

@Dao
interface VideoDao {

//    @Insert
//    fun insert(videoDetails: VideoDetails): Long
//
//    @Update
//    fun update(videoDetails: VideoDetails): Long
//
//    @Query("select * from videodetails where uuid= :uuid")
//    fun getVideo(uuid: String) : VideoDetails
}