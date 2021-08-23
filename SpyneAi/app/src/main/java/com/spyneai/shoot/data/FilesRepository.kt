package com.spyneai.shoot.data

import android.content.ContentValues
import android.provider.BaseColumns
import android.util.Log
import com.spyneai.BaseApplication
import com.spyneai.db.DBHelper
import com.spyneai.db.ImageFiles
import com.spyneai.db.Images
import com.spyneai.shoot.data.model.Image
import com.spyneai.shoot.data.model.ImageFile

class FilesRepository {
    val TAG = "FilesRepository"
    private val dbWritable = DBHelper(BaseApplication.getContext()).writableDatabase
    private val dbReadable = DBHelper(BaseApplication.getContext()).readableDatabase

    fun insertImageFile(image : ImageFile) {
        val values = ContentValues().apply {
            put(ImageFiles.COLUMN_NAME_SKU_NAME, image.skuName)
            put(ImageFiles.COLUMN_NAME_SKU_ID, image.skuId)
            put(ImageFiles.COLUMN_NAME_CATEGORY_NAME, image.categoryName)
            put(ImageFiles.COLUMN_NAME_IMAGE_PATH, image.imagePath)
            put(ImageFiles.COLUMN_NAME_IMAGE_SEQUENCE, image.sequence)
            put(ImageFiles.COLUMN_NAME_IS_UPLOADED, 0)
        }

        val newRowId = dbWritable?.insert(ImageFiles.TABLE_NAME, null, values)

        Log.d(TAG, "insertImage: "+newRowId+ " "+image.imagePath)
    }

    fun getAllImages() {
        val projection = arrayOf(
            BaseColumns._ID,
            ImageFiles.COLUMN_NAME_SKU_NAME,
            ImageFiles.COLUMN_NAME_SKU_ID,
            ImageFiles.COLUMN_NAME_CATEGORY_NAME,
            ImageFiles.COLUMN_NAME_IMAGE_PATH,
            ImageFiles.COLUMN_NAME_IMAGE_SEQUENCE)

        val cursor = dbReadable.query(
            ImageFiles.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            null,              // The columns for the WHERE clause
            null,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            null,               // The sort order
        )

        Log.d(TAG, "getAllImages: "+cursor.count)
    }
}