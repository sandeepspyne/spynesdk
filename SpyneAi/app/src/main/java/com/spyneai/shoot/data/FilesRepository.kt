package com.spyneai.shoot.data

import android.content.ContentValues
import android.provider.BaseColumns
import android.util.Log
import com.spyneai.BaseApplication
import com.spyneai.db.DBHelper
import com.spyneai.db.ImageFiles
import com.spyneai.db.Images
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

    fun getOldestImage() : ImageFile {
        val projection = arrayOf(
            BaseColumns._ID,
            ImageFiles.COLUMN_NAME_SKU_NAME,
            ImageFiles.COLUMN_NAME_SKU_ID,
            ImageFiles.COLUMN_NAME_CATEGORY_NAME,
            ImageFiles.COLUMN_NAME_IMAGE_PATH,
            ImageFiles.COLUMN_NAME_IMAGE_SEQUENCE)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${ImageFiles.COLUMN_NAME_IS_UPLOADED} = ?"
        val projectSelectionArgs = arrayOf("0")

        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${BaseColumns._ID} ASC"

        val cursor = dbReadable.query(
            ImageFiles.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            projectSelectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder,               // The sort order
            "1"
        )

        val image = ImageFile()

        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val skuName = getString(getColumnIndexOrThrow(ImageFiles.COLUMN_NAME_SKU_NAME))
                val skuId = getString(getColumnIndexOrThrow(ImageFiles.COLUMN_NAME_SKU_ID))
                val categoryName = getString(getColumnIndexOrThrow(ImageFiles.COLUMN_NAME_CATEGORY_NAME))
                val imagePath = getString(getColumnIndexOrThrow(ImageFiles.COLUMN_NAME_IMAGE_PATH))
                val sequence = getString(getColumnIndexOrThrow(ImageFiles.COLUMN_NAME_IMAGE_SEQUENCE))

                image.itemId = itemId
                image.skuName = skuName
                image.skuId = skuId
                image.categoryName = categoryName
                image.imagePath = imagePath
                image.sequence = sequence
            }
        }

        return image
    }

    fun skipImage(itemId: Long,skip : Int) {
        val projectValues = ContentValues().apply {
            put(
                ImageFiles.COLUMN_NAME_IS_UPLOADED,
                skip
            )
        }

        // Which row to update, based on the title
        val selection = "${BaseColumns._ID} LIKE ?"

        val selectionArgs = arrayOf(itemId.toString())

        val count = dbWritable.update(
            ImageFiles.TABLE_NAME,
            projectValues,
            selection,
            selectionArgs)

        com.spyneai.shoot.utils.log("deleteImage : "+count)
    }

    fun deleteImage(itemId: Long) {
        val projectValues = ContentValues().apply {
            put(
                ImageFiles.COLUMN_NAME_IS_UPLOADED,
                1
            )
        }

        // Which row to update, based on the title
        val selection = "${BaseColumns._ID} LIKE ?"

        val selectionArgs = arrayOf(itemId.toString())

        val count = dbWritable.update(
            ImageFiles.TABLE_NAME,
            projectValues,
            selection,
            selectionArgs)

        com.spyneai.shoot.utils.log("deleteImage : "+count)
    }

    fun getOldestSkippedImage() : ImageFile {
        val projection = arrayOf(
            BaseColumns._ID,
            ImageFiles.COLUMN_NAME_SKU_NAME,
            ImageFiles.COLUMN_NAME_SKU_ID,
            ImageFiles.COLUMN_NAME_CATEGORY_NAME,
            ImageFiles.COLUMN_NAME_IMAGE_PATH,
            ImageFiles.COLUMN_NAME_IMAGE_SEQUENCE)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${Images.COLUMN_NAME_IS_UPLOADED} = ?"
        val projectSelectionArgs = arrayOf("-1")

        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${BaseColumns._ID} ASC"

        val cursor = dbReadable.query(
            ImageFiles.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            projectSelectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder,               // The sort order
            "1"
        )

        val image = ImageFile()

        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val skuName = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_SKU_NAME))
                val skuId = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_SKU_ID))
                val categoryName = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_CATEGORY_NAME))
                val imagePath = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_PATH))
                val sequence = getInt(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_SEQUENCE))

                image.itemId = itemId
                image.skuName = skuName
                image.skuId = skuId
                image.categoryName = categoryName
                image.imagePath = imagePath
                image.sequence = sequence.toString()
            }
        }

        return image
    }

    fun updateSkipedImages() : Int {
        val values = ContentValues().apply {
            put(
                ImageFiles.COLUMN_NAME_IS_UPLOADED,
                -1
            )
        }

        // Which row to update, based on the title
        val selection = "${Images.COLUMN_NAME_IS_UPLOADED} LIKE ?"

        val selectionArgs = arrayOf("-2")

        val count = dbWritable.update(
            ImageFiles.TABLE_NAME,
            values,
            selection,
            selectionArgs)


        return count
    }
}