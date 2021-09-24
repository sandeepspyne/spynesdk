package com.spyneai.threesixty.data

import android.content.ContentValues
import android.provider.BaseColumns
import android.util.Log
import com.spyneai.BaseApplication
import com.spyneai.db.DBHelper
import com.spyneai.db.Projects
import com.spyneai.db.ShootContract
import com.spyneai.db.Videos

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

    fun getOldestVideo() : VideoDetails {
        val projection = arrayOf(
            BaseColumns._ID,
            Videos.COLUMN_NAME_PROJECT_ID,
            Videos.COLUMN_NAME_SKU_NAME,
            Videos.COLUMN_NAME_SKU_ID,
            Videos.COLUMN_NAME_TYPE,
            Videos.COLUMN_NAME_CATEGORY_NAME,
            Videos.COLUMN_NAME_CATEGORY_SUBCATEGORY_NAME,
            Videos.COLUMN_NAME_VIDEO_PATH,
            Videos.COLUMN_NAME_FRAMES,
            Videos.COLUMN_NAME_BACKGROUND_ID,
            Videos.COLUMN_NAME_IS_UPLOADED,
            Videos.COLUMN_NAME_IS_STATUS_UPDATED,
            Videos.COLUMN_NAME_PRE_SIGNED_URL,
            Videos.COLUMN_NAME_VIDEO_ID)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${Videos.COLUMN_NAME_IS_UPLOADED} = ? OR ${Videos.COLUMN_NAME_IS_STATUS_UPDATED} = ?"
        val projectSelectionArgs = arrayOf("0","0")

        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${BaseColumns._ID} ASC"

        val cursor = dbReadable.query(
            Videos.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            projectSelectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder,               // The sort order
            "1"
        )

        val video = VideoDetails()

        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val projectId = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_PROJECT_ID))
                val skuName = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_SKU_NAME))
                val skuId = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_SKU_ID))
                val type = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_TYPE))
                val category = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_CATEGORY_NAME))
                val subcategory = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_CATEGORY_SUBCATEGORY_NAME))
                val videoPath = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_VIDEO_PATH))
                val frames = getInt(getColumnIndexOrThrow(Videos.COLUMN_NAME_FRAMES))
                val backgroundId = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_BACKGROUND_ID))
                val isUploaded = getInt(getColumnIndexOrThrow(Videos.COLUMN_NAME_IS_UPLOADED))
                val isStatusUpdated = getInt(getColumnIndexOrThrow(Videos.COLUMN_NAME_IS_STATUS_UPDATED))
                val preSignedUrl = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_PRE_SIGNED_URL))
                val videoId = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_VIDEO_ID))

                video.itemId = itemId
                video.projectId = projectId
                video.skuName = skuName
                video.skuId = skuId
                video.type = type
                video.categoryName = category
                video.subCategory = subcategory
                video.videoPath = videoPath
                video.frames = frames
                video.backgroundId = backgroundId
                video.isUploaded = isUploaded
                video.isStatusUpdate = isStatusUpdated
                video.preSignedUrl = preSignedUrl
                video.videoId = videoId

                if (videoPath == "")
                    video.itemId = null
            }
        }

        return video
    }

    fun getOldestSkippedVideo() : VideoDetails {
        val projection = arrayOf(
            BaseColumns._ID,
            Videos.COLUMN_NAME_PROJECT_ID,
            Videos.COLUMN_NAME_SKU_NAME,
            Videos.COLUMN_NAME_SKU_ID,
            Videos.COLUMN_NAME_TYPE,
            Videos.COLUMN_NAME_CATEGORY_NAME,
            Videos.COLUMN_NAME_CATEGORY_SUBCATEGORY_NAME,
            Videos.COLUMN_NAME_VIDEO_PATH,
            Videos.COLUMN_NAME_FRAMES,
            Videos.COLUMN_NAME_BACKGROUND_ID,
            Videos.COLUMN_NAME_IS_UPLOADED,
            Videos.COLUMN_NAME_IS_STATUS_UPDATED,
            Videos.COLUMN_NAME_PRE_SIGNED_URL,
            Videos.COLUMN_NAME_VIDEO_ID)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${Videos.COLUMN_NAME_IS_UPLOADED} = ?"
        val projectSelectionArgs = arrayOf("-1")

        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${BaseColumns._ID} ASC"

        val cursor = dbReadable.query(
            Videos.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            projectSelectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder,               // The sort order
            "1"
        )

        val video = VideoDetails()


        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val projectId = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_PROJECT_ID))
                val skuName = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_SKU_NAME))
                val skuId = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_SKU_ID))
                val type = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_TYPE))
                val category = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_CATEGORY_NAME))
                val subcategory = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_CATEGORY_SUBCATEGORY_NAME))
                val videoPath = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_VIDEO_PATH))
                val frames = getInt(getColumnIndexOrThrow(Videos.COLUMN_NAME_FRAMES))
                val backgroundId = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_BACKGROUND_ID))
                val isStatusUpdated = getInt(getColumnIndexOrThrow(Videos.COLUMN_NAME_IS_STATUS_UPDATED))
                val isUploaded = getInt(getColumnIndexOrThrow(Videos.COLUMN_NAME_IS_UPLOADED))
                val preSignedUrl = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_PRE_SIGNED_URL))
                val videoId = getString(getColumnIndexOrThrow(Videos.COLUMN_NAME_VIDEO_ID))

                video.itemId = itemId
                video.projectId = projectId
                video.skuName = skuName
                video.skuId = skuId
                video.type = type
                video.categoryName = category
                video.subCategory = subcategory
                video.videoPath = videoPath
                video.frames = frames
                video.backgroundId = backgroundId
                video.isUploaded
                video.isStatusUpdate = isStatusUpdated
                video.preSignedUrl = preSignedUrl
                video.videoId = videoId

                if (videoPath == "")
                    video.itemId = null
            }
        }

        return video
    }

    fun skipVideo(itemId: Long,skip : Int) {
        val videoValues = ContentValues().apply {
            put(
                Videos.COLUMN_NAME_IS_UPLOADED,
                skip
            )
        }

        // Which row to update, based on the title
        val selection = "${BaseColumns._ID} LIKE ?"

        val selectionArgs = arrayOf(itemId.toString())

        val count = dbWritable.update(
            Videos.TABLE_NAME,
            videoValues,
            selection,
            selectionArgs)

        Log.d(TAG, "skipVideo: "+count)
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

    fun addPreSignedUrl(videoDetails: VideoDetails) {
        val projectValues = ContentValues().apply {
            put(
                Videos.COLUMN_NAME_PRE_SIGNED_URL,
                videoDetails.preSignedUrl
            )
            put(
                Videos.COLUMN_NAME_VIDEO_ID,
                videoDetails.videoId
            )
        }

        // Which row to update, based on the title
        val selection = "${BaseColumns._ID} LIKE ?"

        val selectionArgs = arrayOf(videoDetails.itemId.toString())

        val count = dbWritable.update(
            Videos.TABLE_NAME,
            projectValues,
            selection,
            selectionArgs)

        Log.d(TAG, "addPreSignedUrl: "+count)
    }

    fun markUploaded(videoDetails: VideoDetails) {
        //update project status to ongoing
        updateProjectStatus(videoDetails.projectId!!)

        val projectValues = ContentValues().apply {
            put(
                Videos.COLUMN_NAME_IS_UPLOADED,
                1
            )
        }

        // Which row to update, based on the title
        val selection = "${BaseColumns._ID} LIKE ?"

        val selectionArgs = arrayOf(videoDetails.itemId.toString())

        val count = dbWritable.update(
            Videos.TABLE_NAME,
            projectValues,
            selection,
            selectionArgs)

        Log.d(TAG, "markUploaded: "+count)
    }

    fun markStatusUploaded(videoDetails: VideoDetails) {
        //update project status to ongoing
        updateProjectStatus(videoDetails.projectId!!)

        val projectValues = ContentValues().apply {
            put(
                Videos.COLUMN_NAME_IS_STATUS_UPDATED,
                1
            )
        }

        // Which row to update, based on the title
        val selection = "${BaseColumns._ID} LIKE ?"

        val selectionArgs = arrayOf(videoDetails.itemId.toString())

        val count = dbWritable.update(
            Videos.TABLE_NAME,
            projectValues,
            selection,
            selectionArgs)

        Log.d(TAG, "markUploaded: "+count)
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