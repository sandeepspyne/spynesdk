package com.spyneai.shoot.data

import android.content.ContentValues
import android.provider.BaseColumns
import android.util.Log
import com.spyneai.BaseApplication
import com.spyneai.db.*
import com.spyneai.shoot.data.model.Image
import com.spyneai.shoot.utils.logUpload
import com.spyneai.threesixty.data.model.VideoDetails


class ImageLocalRepository {


    val TAG = "ImageLocalRepository"
    private val dbWritable = DBHelper(BaseApplication.getContext()).writableDatabase
    private val dbReadable = DBHelper(BaseApplication.getContext()).readableDatabase

    fun isImageExist(skuId : String,
                     imageName : String) : Boolean{
        val projection = arrayOf(
            BaseColumns._ID,
            Images.COLUMN_NAME_SKU_ID,
            Images.COLUMN_NAME_IMAGE_NAME)

        val selection = "${Images.COLUMN_NAME_SKU_ID} = ? AND ${Images.COLUMN_NAME_IMAGE_NAME} = ?"
        val selectionArgs = arrayOf(skuId,imageName)

        val cursor = dbReadable.query(
            Images.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null               // The sort order
        )

        var image : Image? = null

        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val skuId = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_SKU_ID))
                val imageName = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_NAME))

                image = Image()
            }
        }

        return image != null
    }

    fun insertImage(image : Image) {
        val values = ContentValues().apply {
            put(Images.COLUMN_NAME_PROJECT_ID, image.projectId)
            put(Images.COLUMN_NAME_SKU_NAME, image.skuName?.uppercase())
            put(Images.COLUMN_NAME_SKU_ID, image.skuId)
            put(Images.COLUMN_NAME_CATEGORY_NAME, image.categoryName)
            put(Images.COLUMN_NAME_IMAGE_PATH, image.imagePath)
            put(Images.COLUMN_NAME_IMAGE_SEQUENCE, image.sequence)
            put(Images.COLUMN_NAME_OVERLAY_ID, image.overlayId)
            put(Images.COLUMN_NAME_IS_UPLOADED, 0)
            put(Images.COLUMN_NAME_IMAGE_ANGLE, image.angle)
            put(Images.COLUMN_NAME_IMAGE_META, image.meta)
            put(Images.COLUMN_NAME_IMAGE_NAME, image.name)
            put(Images.COLUMN_NAME_IMAGE_DEBUG_DATA, image.debugData)
            put(Images.COLUMN_NAME_IS_STATUS_UPDATED, 0)
            put(Images.COLUMN_NAME_IS_RE_CLICK, 0)
            put(Images.COLUMN_NAME_IS_RESHOOT, image.isReshoot)
        }

        val newRowId = dbWritable?.insert(Images.TABLE_NAME, null, values)

        com.spyneai.shoot.utils.log("insertImage: "+newRowId)
    }

    fun updateImage(image: Image){
        val updatevalues = ContentValues().apply {
            put(Images.COLUMN_NAME_IMAGE_PATH,image.imagePath)
            put(Images.COLUMN_NAME_IS_UPLOADED,0)
            put(Images.COLUMN_NAME_IS_STATUS_UPDATED,0)
            put(Images.COLUMN_NAME_IS_RE_CLICK,1)
            put(Images.COLUMN_NAME_IS_RESHOOT,image.isReshoot)
        }

        val selection = "${Images.COLUMN_NAME_SKU_ID} = ? AND ${Images.COLUMN_NAME_IMAGE_NAME} = ?"
        val selectionArgs = arrayOf(image.skuId,image.name)

        val updateCount = dbWritable.update(
            Images.TABLE_NAME,
            updatevalues,
            selection,
            selectionArgs)

        Log.d(TAG, "updateImage: "+updateCount)

        //getImage(image.skuId!!, image.name!!)
    }

//    fun getImage(skuId: String,imageName: String) : Image{
//        val projection = arrayOf(
//            BaseColumns._ID,
//            Images.COLUMN_NAME_PROJECT_ID,
//            Images.COLUMN_NAME_SKU_NAME,
//            Images.COLUMN_NAME_SKU_ID,
//            Images.COLUMN_NAME_CATEGORY_NAME,
//            Images.COLUMN_NAME_IMAGE_PATH,
//            Images.COLUMN_NAME_IMAGE_SEQUENCE,
//            Images.COLUMN_NAME_IMAGE_ANGLE,
//            Images.COLUMN_NAME_IMAGE_META,
//            Images.COLUMN_NAME_IMAGE_NAME,
//            Images.COLUMN_NAME_IMAGE_ID,
//            Images.COLUMN_NAME_IS_UPLOADED,
//            Images.COLUMN_NAME_IS_STATUS_UPDATED,
//            Images.COLUMN_NAME_IS_RESHOOT,
//            Images.COLUMN_NAME_IMAGE_PRE_SIGNED_URL,
//            Images.COLUMN_NAME_IMAGE_DEBUG_DATA
//        )
//
//
//        val selection = "${Images.COLUMN_NAME_SKU_ID} = ? AND ${Images.COLUMN_NAME_IMAGE_NAME} = ?"
//        val selectionArgs = arrayOf(skuId,imageName)
//
//        // How you want the results sorted in the resulting Cursor
//        val sortOrder = "${BaseColumns._ID} ASC"
//
//        val cursor = dbReadable.query(
//            Images.TABLE_NAME,   // The table to query
//            projection,             // The array of columns to return (pass null to get all)
//            selection,              // The columns for the WHERE clause
//            selectionArgs,          // The values for the WHERE clause
//            null,                   // don't group the rows
//            null,                   // don't filter by row groups
//            sortOrder,               // The sort order
//            "1"
//        )
//
//        val image = Image()
//
//        with(cursor) {
//            while (moveToNext()) {
//                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
//                val projectId = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_PROJECT_ID))
//                val skuName = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_SKU_NAME))
//                val skuId = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_SKU_ID))
//                val categoryName = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_CATEGORY_NAME))
//                val imagePath = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_PATH))
//                val sequence = getInt(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_SEQUENCE))
//                val angle = getInt(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_ANGLE))
//                val meta = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_META))
//                val name = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_NAME))
//                val isReclick = getInt(getColumnIndexOrThrow(Images.COLUMN_NAME_IS_RE_CLICK))
//
//                image.itemId = itemId
//                image.projectId = projectId
//                image.skuName = skuName
//                image.skuId = skuId
//                image.categoryName = categoryName
//                image.imagePath = imagePath
//                image.sequence = sequence
//                image.angle = angle
//                image.meta = meta
//                image.name = name
//                image.isReclick = isReclick
//
//            }
//        }
//
//        val s = ""
//
//        return image
//    }

    fun getOldestImage(status : String) : Image {
        val projection = arrayOf(
            BaseColumns._ID,
            Images.COLUMN_NAME_PROJECT_ID,
            Images.COLUMN_NAME_SKU_NAME,
            Images.COLUMN_NAME_SKU_ID,
            Images.COLUMN_NAME_CATEGORY_NAME,
            Images.COLUMN_NAME_IMAGE_PATH,
            Images.COLUMN_NAME_IMAGE_SEQUENCE,
            Images.COLUMN_NAME_IMAGE_ANGLE,
            Images.COLUMN_NAME_IMAGE_META,
            Images.COLUMN_NAME_IMAGE_NAME,
            Images.COLUMN_NAME_IMAGE_PRE_SIGNED_URL,
            Images.COLUMN_NAME_OVERLAY_ID,
            Images.COLUMN_NAME_IMAGE_ID,
            Images.COLUMN_NAME_IMAGE_DEBUG_DATA,
            Images.COLUMN_NAME_IS_UPLOADED,
            Images.COLUMN_NAME_IS_STATUS_UPDATED,
            Images.COLUMN_NAME_IS_RE_CLICK,
            Images.COLUMN_NAME_IS_RESHOOT)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${Images.COLUMN_NAME_IS_UPLOADED} = ? OR ${Images.COLUMN_NAME_IS_STATUS_UPDATED} = ?"
        val projectSelectionArgs = arrayOf(status,"0")

        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${BaseColumns._ID} ASC"

        val cursor = dbReadable.query(
            Images.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            projectSelectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder,               // The sort order
            "1"
        )

        val image = Image()

        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val projectId = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_PROJECT_ID))
                val skuName = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_SKU_NAME))
                val skuId = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_SKU_ID))
                val categoryName = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_CATEGORY_NAME))
                val imagePath = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_PATH))
                val sequence = getInt(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_SEQUENCE))
                val angle = getInt(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_ANGLE))
                val meta = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_META))

                val name = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_NAME))
                val preSignedUrl = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_PRE_SIGNED_URL))
                val overlayId = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_OVERLAY_ID))
                val imageId = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_ID))
                val debugData = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_DEBUG_DATA))
                val isUploaded = getInt(getColumnIndexOrThrow(Images.COLUMN_NAME_IS_UPLOADED))
                val isStatusUpdated = getInt(getColumnIndexOrThrow(Images.COLUMN_NAME_IS_STATUS_UPDATED))
                val isReclick = getInt(getColumnIndexOrThrow(Images.COLUMN_NAME_IS_RE_CLICK))
                val isReshoot = getInt(getColumnIndexOrThrow(Images.COLUMN_NAME_IS_RESHOOT))

                image.itemId = itemId
                image.projectId = projectId
                image.skuName = skuName
                image.skuId = skuId
                image.categoryName = categoryName
                image.imagePath = imagePath
                image.sequence = sequence
                image.angle = angle
                image.meta = meta
                image.name = name
                image.preSignedUrl = preSignedUrl
                image.overlayId = overlayId
                image.imageId = imageId
                image.debugData = debugData
                image.isUploaded = isUploaded
                image.isStatusUpdated = isStatusUpdated
                image.isReclick = isReclick
                image.isReshoot = isReshoot
            }
        }

        return image
    }

    fun skipImage(itemId: Long,skip : Int) {
        val projectValues = ContentValues().apply {
            put(
                Images.COLUMN_NAME_IS_UPLOADED,
                skip
            )
        }

        // Which row to update, based on the title
        val selection = "${BaseColumns._ID} LIKE ?"

        val selectionArgs = arrayOf(itemId.toString())

        val count = dbWritable.update(
            Images.TABLE_NAME,
            projectValues,
            selection,
            selectionArgs)

        logUpload("Image Skipped "+skip+" "+count)
        com.spyneai.shoot.utils.log("deleteImage : "+count)
    }

    fun updateSkipedImages() : Int {
        val values = ContentValues().apply {
            put(
                Images.COLUMN_NAME_IS_UPLOADED,
                -1
            )
        }

        // Which row to update, based on the title
        val selection = "${Images.COLUMN_NAME_IS_UPLOADED} LIKE ?"

        val selectionArgs = arrayOf("-2")

        val count = dbWritable.update(
            Images.TABLE_NAME,
            values,
            selection,
            selectionArgs)


        return count
    }

    fun deleteImage(itemId: Long) {
        val projectValues = ContentValues().apply {
            put(
                Images.COLUMN_NAME_IS_UPLOADED,
                1
            )
        }

        // Which row to update, based on the title
        val selection = "${BaseColumns._ID} LIKE ?"

        val selectionArgs = arrayOf(itemId.toString())

        val count = dbWritable.update(
            Images.TABLE_NAME,
            projectValues,
            selection,
            selectionArgs)

        com.spyneai.shoot.utils.log("deleteImage : "+count)
    }

    fun addPreSignedUrl(image: Image) {
        val projectValues = ContentValues().apply {
            put(Images.COLUMN_NAME_IMAGE_PRE_SIGNED_URL, image.preSignedUrl)
            put(Images.COLUMN_NAME_IMAGE_ID, image.imageId)
        }

        // Which row to update, based on the title
        val selection = "${BaseColumns._ID} LIKE ?"

        val selectionArgs = arrayOf(image.itemId.toString())

        val count = dbWritable.update(
            Images.TABLE_NAME,
            projectValues,
            selection,
            selectionArgs)

        Log.d(TAG, "addPreSignedUrl: "+count)
    }

    fun markUploaded(image: Image) {
        //update project status to ongoing
        updateProjectStatus(image.projectId!!)

        val projectValues = ContentValues().apply {
            put(
                Images.COLUMN_NAME_IS_UPLOADED,
                1
            )
        }

        // Which row to update, based on the title
        val selection = "${BaseColumns._ID} LIKE ?"

        val selectionArgs = arrayOf(image.itemId.toString())

        val count = dbWritable.update(
            Images.TABLE_NAME,
            projectValues,
            selection,
            selectionArgs)

        Log.d(TAG, "markUploaded: "+count)
    }

    fun markStatusUploaded(videoDetails: Image) {
        //update project status to ongoing
        updateProjectStatus(videoDetails.projectId!!)

        val projectValues = ContentValues().apply {
            put(
                Images.COLUMN_NAME_IS_STATUS_UPDATED,
                1
            )
        }

        // Which row to update, based on the title
        val selection = "${BaseColumns._ID} LIKE ?"

        val selectionArgs = arrayOf(videoDetails.itemId.toString())

        val count = dbWritable.update(
            Images.TABLE_NAME,
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
}