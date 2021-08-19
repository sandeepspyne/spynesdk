package com.spyneai.shoot.data

import android.content.ContentValues
import android.provider.BaseColumns
import com.spyneai.BaseApplication
import com.spyneai.shoot.data.model.Sku
import com.spyneai.db.DBHelper
import com.spyneai.db.Images
import com.spyneai.db.ShootContract
import com.spyneai.shoot.data.model.Image

class ShootLocalRepository {

    private val dbWritable = DBHelper(BaseApplication.getContext()).writableDatabase
    private val dbReadable = DBHelper(BaseApplication.getContext()).readableDatabase
    private val TAG = "ShootLocalRepository"


    fun insertImage(image : Image) {
        val values = ContentValues().apply {
            put(Images.COLUMN_NAME_PROJECT_ID, image.projectId)
            put(Images.COLUMN_NAME_SKU_NAME, image.skuName)
            put(Images.COLUMN_NAME_SKU_ID, image.skuId)
            put(Images.COLUMN_NAME_CATEGORY_NAME, image.categoryName)
            put(Images.COLUMN_NAME_IMAGE_PATH, image.imagePath)
            put(Images.COLUMN_NAME_IMAGE_SEQUENCE, image.sequence)
        }

        val newRowId = dbWritable?.insert(Images.TABLE_NAME, null, values)

        com.spyneai.shoot.utils.log("insertImage: "+newRowId)
    }

    fun getOldestImage() : Image {
        val projection = arrayOf(
            BaseColumns._ID,
            Images.COLUMN_NAME_PROJECT_ID,
            Images.COLUMN_NAME_SKU_NAME,
            Images.COLUMN_NAME_SKU_ID,
            Images.COLUMN_NAME_CATEGORY_NAME,
            Images.COLUMN_NAME_IMAGE_PATH,
            Images.COLUMN_NAME_IMAGE_SEQUENCE)

        // Filter results WHERE "title" = 'My Title'
       // val selection = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} = ?"


        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${BaseColumns._ID} ASC"

        val cursor = dbReadable.query(
            Images.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            null,              // The columns for the WHERE clause
            null,          // The values for the WHERE clause
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

                image.itemId = itemId
                image.projectId = projectId
                image.skuName = skuName
                image.skuId = skuId
                image.categoryName = categoryName
                image.imagePath = imagePath
                image.sequence = sequence
            }
        }

        return image
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
                val uploadedImages = getInt(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_UPLOADED_IMAGES))

                sku.skuId = skuId
                //  sku.categoryName = categoryName
                sku.totalImages = totalImages
                sku.uploadedImages = uploadedImages
            }
        }

        return sku
    }

    fun insertSku(sku : Sku) {
        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put(ShootContract.ShootEntry.COLUMN_NAME_PROJECT_ID, sku.projectId)
            put(ShootContract.ShootEntry.COLUMN_NAME_SKU_ID, sku.skuId)
            put(ShootContract.ShootEntry.COLUMN_NAME_CATEGORY_NAME, sku.categoryName)
            put(ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES, sku.totalImages)
            put(ShootContract.ShootEntry.COLUMN_NAME_UPLOADED_IMAGES, 0)
            put(ShootContract.ShootEntry.COLUMN_NAME_PROCESS_SKU, 0)
            put(ShootContract.ShootEntry.COLUMN_NAME_IS_PROCESSED, -1)
        }

        val newRowId = dbWritable?.insert(ShootContract.ShootEntry.TABLE_NAME, null, values)

        com.spyneai.shoot.utils.log("insertSku: "+newRowId)
    }

    fun getLastSku() : Sku {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(
            BaseColumns._ID,
            ShootContract.ShootEntry.COLUMN_NAME_SKU_ID,
            ShootContract.ShootEntry.COLUMN_NAME_BACKGROUND_ID,
            ShootContract.ShootEntry.COLUMN_NAME_IS_360)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${ShootContract.ShootEntry.COLUMN_NAME_IS_PROCESSED} = ?"
        val selectionArgs = arrayOf("-1")

        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${BaseColumns._ID} ASC"

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
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val skuId = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_SKU_ID))
                val backgroundId = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_BACKGROUND_ID))
                val is360 = getInt(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_IS_360))

                sku.itemId = itemId
                sku.skuId = skuId
                sku.backgroundId = backgroundId
                sku.is360 = is360
            }
        }

        return sku
    }

    fun getUploadedAndTotalImagesCount(skuId : String) : Sku{
        val projection = arrayOf(
            BaseColumns._ID,
            ShootContract.ShootEntry.COLUMN_NAME_SKU_ID,
            ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES,
            ShootContract.ShootEntry.COLUMN_NAME_UPLOADED_IMAGES,
            ShootContract.ShootEntry.COLUMN_NAME_PROCESS_SKU)

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
                val uploadedImages = getInt(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_UPLOADED_IMAGES))
                val processSku = getInt(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_PROCESS_SKU))

                sku.skuId = skuId
                sku.totalImages = totalImages
                sku.uploadedImages = uploadedImages
                sku.processSku = processSku
            }
        }

        return sku
    }

    fun processSku(skuId: String) : Boolean {
        val sku = getUploadedAndTotalImagesCount(skuId)
        return sku.uploadedImages == sku.totalImages && sku.processSku == 1
    }

    fun isImagesUploaded(skuId : String) : Boolean {
        val sku = getUploadedAndTotalImagesCount(skuId)
        return sku.uploadedImages == sku.totalImages
    }

    fun updateIsProcessed(skuId : String) {
        val values = ContentValues().apply {
            put(
                ShootContract.ShootEntry.COLUMN_NAME_IS_PROCESSED,
                1
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

        com.spyneai.shoot.utils.log("Upload count(update): "+count)
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

        com.spyneai.shoot.utils.log("Upload count(update): "+uploadCount)
    }

    fun updateTotalImageCount(skuId : String) {
        var totalImagesCount = getUploadedAndTotalImagesCount(skuId).totalImages

        val values = ContentValues().apply {
            put(
                ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES,
                totalImagesCount?.plus(1))
        }

        // Which row to update, based on the title
        val selection = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} LIKE ?"

        val selectionArgs = arrayOf(skuId)

        val count = dbWritable.update(
            ShootContract.ShootEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs)
        com.spyneai.shoot.utils.log("total images count(update): "+ totalImagesCount)
    }

    fun queueProcessRequest(skuId: String,backgroundId : String,is360 : Boolean) {
        val values = ContentValues().apply {
            put(ShootContract.ShootEntry.COLUMN_NAME_PROCESS_SKU, 1)
            put(ShootContract.ShootEntry.COLUMN_NAME_BACKGROUND_ID,backgroundId)
        }

        if (is360)
            values.put(ShootContract.ShootEntry.COLUMN_NAME_IS_360,1)
        else
            values.put(ShootContract.ShootEntry.COLUMN_NAME_IS_360,-1)

        // Which row to update, based on the title
        val selection = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} LIKE ?"

        val selectionArgs = arrayOf(skuId)

        val count = dbWritable.update(
            ShootContract.ShootEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs)

        com.spyneai.shoot.utils.log("Queue Process Request: "+count)
    }

    fun getBackgroundId(skuId: String): String {
        val projection = arrayOf(
            BaseColumns._ID,
            ShootContract.ShootEntry.COLUMN_NAME_SKU_ID,
            ShootContract.ShootEntry.COLUMN_NAME_BACKGROUND_ID)

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

        var backgroundId = ""

        with(cursor) {
            while (moveToNext()) {
                val skuId = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_SKU_ID))
                backgroundId = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_BACKGROUND_ID))
            }
        }

        return backgroundId
        com.spyneai.shoot.utils.log("Background id: "+backgroundId)
    }

    fun deleteImage(itemId: Long) {
        // Which row to update, based on the title
        val selection = "${BaseColumns._ID} LIKE ?"

        val selectionArgs = arrayOf(itemId.toString())

        val count = dbWritable.delete(
            Images.TABLE_NAME,
            selection,
            selectionArgs)

        com.spyneai.shoot.utils.log("deleteImage : "+count)
    }

}