package com.spyneai.threesixty.data

import android.content.ContentValues
import android.provider.BaseColumns
import android.util.Log
import com.spyneai.BaseApplication
import com.spyneai.db.*
import com.spyneai.needs.AppConstants


import com.spyneai.threesixty.data.model.VideoDetails

class VideoLocalRepository {

    private val dbWritable = DBHelper(BaseApplication.getContext()).writableDatabase
    private val dbReadable = DBHelper(BaseApplication.getContext()).readableDatabase
    private val TAG = "VideoLocalRepository"

    fun insertVideo(video: VideoDetails) {
        val values = ContentValues().apply {
            put(Videos.COLUMN_NAME_PROJECT_ID, video.projectId)
            put(Videos.COLUMN_NAME_SKU_NAME, video.skuName)
            put(Videos.COLUMN_NAME_SKU_ID, video.skuId)
            put(Videos.COLUMN_NAME_TYPE, video.type)
            put(Videos.COLUMN_NAME_CATEGORY_NAME, video.categoryName)
            put(Videos.COLUMN_NAME_CATEGORY_SUBCATEGORY_NAME, video.subCategory)
            put(Videos.COLUMN_NAME_VIDEO_PATH, "")
            put(Videos.COLUMN_NAME_FRAMES, video.frames)
            put(Videos.COLUMN_NAME_BACKGROUND_ID, "")
            put(Videos.COLUMN_NAME_PRE_SIGNED_URL, AppConstants.DEFAULT_PRESIGNED_URL)
            put(Videos.COLUMN_NAME_IS_UPLOADED, 0)
            put(Videos.COLUMN_NAME_IS_STATUS_UPDATED, 0)
        }

        val newRowId = dbWritable?.insert(Videos.TABLE_NAME, null, values)
        Log.d(TAG, "insertVideo: "+newRowId)
    }

    fun addVideoPath(skuId : String,videoPath : String) {
        val videoValues = ContentValues().apply {
            put(
                Videos.COLUMN_NAME_VIDEO_PATH,
                videoPath
            )
        }

        val videoSelection = "${Videos.COLUMN_NAME_SKU_ID} LIKE ?"
        // Which row to update, based on the title


        val videoSelectionArgs = arrayOf(skuId)

        val videoCount = dbWritable.update(
            Videos.TABLE_NAME,
            videoValues,
            videoSelection,
            videoSelectionArgs)

        Log.d(TAG, "addVideoPath: "+videoCount)
    }

    fun addBackgroundId(skuId: String,backgroundId : String){
        val videoValues = ContentValues().apply {
            put(
                Videos.COLUMN_NAME_BACKGROUND_ID,
                backgroundId
            )
        }

        val videoSelection = "${Videos.COLUMN_NAME_SKU_ID} LIKE ?"
        // Which row to update, based on the title


        val videoSelectionArgs = arrayOf(skuId)

        val videoCount = dbWritable.update(
            Videos.TABLE_NAME,
            videoValues,
            videoSelection,
            videoSelectionArgs)

        Log.d(TAG, "addBackgroundId: "+videoCount)
    }


    fun updateSkippedVideos() : Int {
        val values = ContentValues().apply {
            put(
                Videos.COLUMN_NAME_IS_UPLOADED,
                -1
            )
        }

        // Which row to update, based on the title
        val selection = "${Videos.COLUMN_NAME_IS_UPLOADED} LIKE ?"

        val selectionArgs = arrayOf("-2")

        val count = dbWritable.update(
            Videos.TABLE_NAME,
            values,
            selection,
            selectionArgs)


        Log.d(TAG, "updateSkippedVideos: "+count)
        return count
    }





    fun updateProjectStatus(projectId : String) {
        val projectValues = ContentValues().apply {
            put(
                Projects.COLUMN_NAME_STATUS,
                "Ongoing"
            )
        }

        val projectSelection = "${ShootContract.ShootEntry.COLUMN_NAME_PROJECT_ID} LIKE ?"

        val projectSelectionArgs = arrayOf(projectId)

        val projectCount = dbWritable.update(
            Projects.TABLE_NAME,
            projectValues,
            projectSelection,
            projectSelectionArgs)

        com.spyneai.shoot.utils.log("Upload prject(update): "+projectCount)
    }

    fun getVideoPath(skuId: String) : String? {
        val projection = arrayOf(
            BaseColumns._ID,
            Videos.COLUMN_NAME_VIDEO_PATH)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${Videos.COLUMN_NAME_SKU_ID} = ?"
        val projectSelectionArgs = arrayOf(skuId)

        val cursor = dbReadable.query(
            Videos.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            projectSelectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null
        )

        var videoPath : String? = null


        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                videoPath = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_VIDEO_PATH))
            }
        }

        return videoPath
    }

    fun getVideoId(skuId: String) : String? {
        val projection = arrayOf(
            BaseColumns._ID,
            Videos.COLUMN_NAME_VIDEO_ID)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${Videos.COLUMN_NAME_SKU_ID} = ?"
        val projectSelectionArgs = arrayOf(skuId)

        val cursor = dbReadable.query(
            Videos.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            projectSelectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null
        )

        var videoId : String? = null


        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                videoId = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_VIDEO_ID))
            }
        }

        return videoId
    }


}