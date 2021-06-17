package com.spyneai.shoot.data

import android.content.ContentValues
import android.provider.BaseColumns
import android.util.Log
import com.spyneai.BaseApplication
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.data.sqlite.DBHelper
import com.spyneai.shoot.data.sqlite.ShootContract

class ShootLocalRepository {

    private val dbWritable = DBHelper(BaseApplication.getContext()).writableDatabase
    private val dbReadable = DBHelper(BaseApplication.getContext()).readableDatabase
    private val TAG = "ShootLocalRepository"


    fun insertSku(sku : Sku) {
        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put(ShootContract.ShootEntry.COLUMN_NAME_PROJECT_ID, sku.projectId)
            put(ShootContract.ShootEntry.COLUMN_NAME_SKU_ID, sku.skuId)
            put(ShootContract.ShootEntry.COLUMN_NAME_CATEGORY_NAME, sku.categoryName)
            put(ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES, sku.totalImages)
            put(ShootContract.ShootEntry.COLUMN_NAME_UPLOADED_IMAGES, 0)
            put(ShootContract.ShootEntry.COLUMN_NAME_PROCESS_SKU, false)
        }

        val newRowId = dbWritable?.insert(ShootContract.ShootEntry.TABLE_NAME, null, values)

        Log.d(TAG, "insertSku: "+newRowId)
    }

    fun getSku(skuId : String) : Sku {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(
            BaseColumns._ID,
            ShootContract.ShootEntry.COLUMN_NAME_SKU_ID,
            ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES,
            ShootContract.ShootEntry.COLUMN_NAME_UPLOADED_IMAGES)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} = ?"
        val selectionArgs = arrayOf(skuId)

        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} DESC"

        val cursor = dbReadable.query(
            ShootContract.ShootEntry.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder               // The sort order
        )

        val sku = Sku()

        with(cursor) {
            while (moveToNext()) {
//                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val skuId = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_SKU_ID))
               // val categoryName = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_CATEGORY_NAME))
                val totalImages = getInt(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES))
                val uploadedImages = getInt(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES))

                sku.skuId = skuId
              //  sku.categoryName = categoryName
                sku.totalImages = totalImages
                sku.uploadedImages = uploadedImages
            }
        }

        return sku
    }

    fun getUploadedAndTotalImagesCount(skuId : String) : Sku{
        val projection = arrayOf(
            BaseColumns._ID,
            ShootContract.ShootEntry.COLUMN_NAME_SKU_ID,
            ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES,
            ShootContract.ShootEntry.COLUMN_NAME_UPLOADED_IMAGES)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} = ?"
        val selectionArgs = arrayOf(skuId)

        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} DESC"

        val cursor = dbReadable.query(
            ShootContract.ShootEntry.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder               // The sort order
        )

        val sku = Sku()

        with(cursor) {
            while (moveToNext()) {
                val skuId = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_SKU_ID))
                val totalImages = getInt(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES))
                val uploadedImages = getInt(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES))

                sku.skuId = skuId
                sku.totalImages = totalImages
                sku.uploadedImages = uploadedImages
            }
        }

        return sku
    }

    fun processSku(skuId: String) : Boolean {
        val sku = getUploadedAndTotalImagesCount(skuId)
        return sku.uploadedImages == sku.totalImages && sku.processSku
    }

    fun updateUploadCount(skuId : String) {
        var uploadCount = getUploadedAndTotalImagesCount(skuId).uploadedImages

        val values = ContentValues().apply {
            put(
                ShootContract.ShootEntry.COLUMN_NAME_UPLOADED_IMAGES,
                uploadCount?.plus(1)
            )
        }

        // Which row to update, based on the title
        val selection = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} LIKE ?"

        val selectionArgs = arrayOf(skuId)

        val count = dbWritable.update(
            ShootContract.ShootEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs)

        Log.d(TAG, "updateUploadCount: "+count)
    }

    fun updateTotalImageCount(skuId : String, totalImages : Int) {
        val values = ContentValues().apply {
            put(ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES, totalImages)
        }

        // Which row to update, based on the title
        val selection = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} LIKE ?"

        val selectionArgs = arrayOf(skuId)

        val count = dbWritable.update(
            ShootContract.ShootEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs)

        Log.d(TAG, "updateTotalImageCount: "+count)
    }

    fun queueProcessRequest(skuId: String) {
        val values = ContentValues().apply {
            put(ShootContract.ShootEntry.COLUMN_NAME_PROCESS_SKU, true)
        }

        // Which row to update, based on the title
        val selection = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} LIKE ?"

        val selectionArgs = arrayOf(skuId)

        val count = dbWritable.update(
            ShootContract.ShootEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs)

        Log.d(TAG, "queueProcessRequest: "+count)
    }

}