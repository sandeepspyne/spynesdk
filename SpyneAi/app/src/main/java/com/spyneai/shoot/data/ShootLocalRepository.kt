package com.spyneai.shoot.data

import android.content.ContentValues
import android.provider.BaseColumns
import com.spyneai.BaseApplication
import com.spyneai.db.DBHelper
import com.spyneai.db.Images
import com.spyneai.db.Projects
import com.spyneai.db.ShootContract
import com.spyneai.shoot.data.model.Image
import com.spyneai.shoot.data.model.Project
import com.spyneai.shoot.data.model.Sku

class ShootLocalRepository {

    private val dbWritable = DBHelper(BaseApplication.getContext()).writableDatabase
    private val dbReadable = DBHelper(BaseApplication.getContext()).readableDatabase
    private val TAG = "ShootLocalRepository"

    fun insertProject(project : Project) {
        val values = ContentValues().apply {
            put(Projects.COLUMN_NAME_PROJECT_NAME, project.projectName)
            put(Projects.COLUMN_NAME_CREATED_ON, project.createdOn)
            put(Projects.COLUMN_NAME_CATEGORY_NAME, project.categoryName)
            put(Projects.COLUMN_NAME_CATEGORY_ID, project.categoryId)
            put(Projects.COLUMN_NAME_SUB_CATEGORY_NAME, "")
            put(Projects.COLUMN_NAME_SUB_CATEGORY_ID, "")
            put(Projects.COLUMN_NAME_EXTERIOR_ANGLES, 0)
            put(Projects.COLUMN_NAME_PROJECT_ID, project.projectId)
            put(Projects.COLUMN_NAME_STATUS, "draft")
        }

        val newRowId = dbWritable?.insert(Projects.TABLE_NAME, null, values)
        com.spyneai.shoot.utils.log("insertImage: "+newRowId)
    }

    fun getDraftProjects() : ArrayList<Project>{
        val projection = arrayOf(
            BaseColumns._ID,
            Projects.COLUMN_NAME_PROJECT_NAME,
            Projects.COLUMN_NAME_CREATED_ON,
            Projects.COLUMN_NAME_CATEGORY_NAME,
            Projects.COLUMN_NAME_CATEGORY_ID,
            Projects.COLUMN_NAME_SUB_CATEGORY_NAME,
            Projects.COLUMN_NAME_SUB_CATEGORY_ID,
            Projects.COLUMN_NAME_EXTERIOR_ANGLES,
            Projects.COLUMN_NAME_PROJECT_ID,
            Projects.COLUMN_NAME_STATUS)

        // Filter results WHERE "title" = 'My Title'
         val selection = "${Projects.COLUMN_NAME_STATUS} = ?"
        val selectionArgs = arrayOf("draft")

        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${BaseColumns._ID} DESC"

        val cursor = dbReadable.query(
            Projects.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder
        )

        val projectList = ArrayList<Project>()


        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val projectName = getString(getColumnIndexOrThrow(Projects.COLUMN_NAME_PROJECT_NAME))
                val createOn = getLong(getColumnIndexOrThrow(Projects.COLUMN_NAME_CREATED_ON))
                val categoryName = getString(getColumnIndexOrThrow(Projects.COLUMN_NAME_CATEGORY_NAME))
                val categoryId = getString(getColumnIndexOrThrow(Projects.COLUMN_NAME_CATEGORY_ID))
                val subCategoryName = getString(getColumnIndexOrThrow(Projects.COLUMN_NAME_SUB_CATEGORY_NAME))
                val subCategoryId = getString(getColumnIndexOrThrow(Projects.COLUMN_NAME_SUB_CATEGORY_ID))
                val exteriorAngles = getInt(getColumnIndexOrThrow(Projects.COLUMN_NAME_EXTERIOR_ANGLES))
                val projectId = getString(getColumnIndexOrThrow(Projects.COLUMN_NAME_PROJECT_ID))
                val status = getString(getColumnIndexOrThrow(Projects.COLUMN_NAME_STATUS))

                val project = Project()

                project.itemId = itemId
                project.projectName = projectName
                project.createdOn = createOn
                project.categoryName = categoryName
                project.categoryId = categoryId
                project.subCategoryName = subCategoryName
                project.subCategoryId = subCategoryId
                project.exteriorAngles = exteriorAngles
                project.projectId = projectId
                project.status = status

                //get sku's count
                project.skus = getSkusByProjectId(projectId).size

                if (project.skus != null && project.skus!! > 0)
                    project.thumbnail = getProjectThumbnail(projectId)

                project.images = getImagesByProjectId(projectId)

                projectList.add(project)
            }
        }

        return projectList
    }

    private fun getProjectThumbnail(projectId: String): String? {
        val projection = arrayOf(
            BaseColumns._ID,
            Images.COLUMN_NAME_PROJECT_ID,
            Images.COLUMN_NAME_SKU_ID,
            Images.COLUMN_NAME_CATEGORY_NAME,
            Images.COLUMN_NAME_IMAGE_PATH,
            Images.COLUMN_NAME_IMAGE_SEQUENCE)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${ShootContract.ShootEntry.COLUMN_NAME_PROJECT_ID} = ?"
        val selectionArgs = arrayOf(projectId)



        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${BaseColumns._ID} DESC"

        val cursor = dbReadable.query(
            Images.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
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
                val skuId = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_SKU_ID))
                val categoryName = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_CATEGORY_NAME))
                val imagePath = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_PATH))
                val sequence = getInt(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_SEQUENCE))

                image.itemId = itemId
                image.projectId = projectId
                image.skuId = skuId
                image.categoryName = categoryName
                image.imagePath = imagePath
                image.sequence = sequence
            }
        }

        return image.imagePath
    }

    private fun getSkuThumbnail(skuId: String): String? {
        val projection = arrayOf(
            BaseColumns._ID,
            Images.COLUMN_NAME_PROJECT_ID,
            Images.COLUMN_NAME_SKU_ID,
            Images.COLUMN_NAME_CATEGORY_NAME,
            Images.COLUMN_NAME_IMAGE_PATH,
            Images.COLUMN_NAME_IMAGE_SEQUENCE)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} = ?"
        val selectionArgs = arrayOf(skuId)



        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${BaseColumns._ID} DESC"

        val cursor = dbReadable.query(
            Images.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
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
                val skuId = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_SKU_ID))
                val categoryName = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_CATEGORY_NAME))
                val imagePath = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_PATH))
                val sequence = getInt(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_SEQUENCE))

                image.itemId = itemId
                image.projectId = projectId
                image.skuId = skuId
                image.categoryName = categoryName
                image.imagePath = imagePath
                image.sequence = sequence
            }
        }

        return image.imagePath
    }

    fun getSkusByProjectId(projectId: String?): ArrayList<Sku> {
        val projection = arrayOf(
            BaseColumns._ID,
            ShootContract.ShootEntry.COLUMN_NAME_PROJECT_NAME,
            ShootContract.ShootEntry.COLUMN_NAME_SKU_NAME,
            ShootContract.ShootEntry.COLUMN_NAME_CREATED_ON,
            ShootContract.ShootEntry.COLUMN_NAME_CATEGORY_NAME,
            ShootContract.ShootEntry.COLUMN_NAME_CATEGORY_ID,
            ShootContract.ShootEntry.COLUMN_NAME_SUB_CATEGORY_NAME,
            ShootContract.ShootEntry.COLUMN_NAME_SUB_CATEGORY_ID,
            ShootContract.ShootEntry.COLUMN_NAME_PROJECT_ID,
            ShootContract.ShootEntry.COLUMN_NAME_SKU_ID,
            ShootContract.ShootEntry.COLUMN_NAME_EXTERIOR_ANGLES,
            ShootContract.ShootEntry.COLUMN_NAME_BACKGROUND_ID,
            ShootContract.ShootEntry.COLUMN_NAME_IS_360,
            ShootContract.ShootEntry.COLUMN_NAME_IS_PROCESSED)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${ShootContract.ShootEntry.COLUMN_NAME_PROJECT_ID} = ?"
        val selectionArgs = arrayOf(projectId)

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

        val skuList = ArrayList<Sku>()

        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val projectName = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_PROJECT_NAME))
                val skuName = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_SKU_NAME))
                val createdOn = getLong(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_CREATED_ON))
                val categoryName = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_CATEGORY_NAME))
                val categoryId = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_CATEGORY_ID))
                val subCategoryName = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_SUB_CATEGORY_NAME))
                val subCategoryId = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_SUB_CATEGORY_ID))
                val projectId = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_PROJECT_ID))
                val skuId = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_SKU_ID))
                val exteriroAngles = getInt(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_EXTERIOR_ANGLES))
                val backgroundId = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_BACKGROUND_ID))
                val is360 = getInt(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_IS_360))
                val isProcessed = getInt(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_IS_PROCESSED))

                val sku = Sku()
                sku.itemId = itemId
                sku.projectName = projectName
                sku.skuName = skuName
                sku.createdOn = createdOn
                sku.categoryName = categoryName
                sku.categoryId = categoryId
                sku.subcategoryName = subCategoryName
                sku.subcategoryId = subCategoryId
                sku.projectId = projectId
                sku.skuId = skuId
                sku.exteriorAngles = exteriroAngles
                sku.backgroundId = backgroundId
                sku.is360 = is360
                sku.isProcessed = isProcessed

                if (skuId != null) {
                    sku.thumbnail = getSkuThumbnail(skuId)
                    sku.totalImages = getImagesBySkuId(skuId).size
                }


                skuList.add(sku)
            }
        }

        return skuList
    }

    fun getImagesBySkuId(skuId: String?):  ArrayList<Image> {
        val projection = arrayOf(
            BaseColumns._ID,
            Images.COLUMN_NAME_PROJECT_ID,
            Images.COLUMN_NAME_SKU_ID,
            Images.COLUMN_NAME_CATEGORY_NAME,
            Images.COLUMN_NAME_IMAGE_PATH,
            Images.COLUMN_NAME_IMAGE_SEQUENCE)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${Images.COLUMN_NAME_SKU_ID} = ?"
        val selectionArgs = arrayOf(skuId)


        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${BaseColumns._ID} ASC"

        val cursor = dbReadable.query(
            Images.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder                       // The sort order
        )


        val imagesList = ArrayList<Image>()

        with(cursor) {
            while (moveToNext()) {
                val itemId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val projectId = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_PROJECT_ID))
                val skuId = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_SKU_ID))
                val categoryName = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_CATEGORY_NAME))
                val imagePath = getString(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_PATH))
                val sequence = getInt(getColumnIndexOrThrow(Images.COLUMN_NAME_IMAGE_SEQUENCE))

                val image = Image()
                image.itemId = itemId
                image.projectId = projectId
                image.skuId = skuId
                image.categoryName = categoryName
                image.imagePath = imagePath
                image.sequence = sequence

                imagesList.add(image)
            }
        }

        return imagesList
    }

    private fun getImagesByProjectId(projectId: String): Int {
        val projection = arrayOf(
            BaseColumns._ID,
            Images.COLUMN_NAME_PROJECT_ID,
            Images.COLUMN_NAME_SKU_ID,
            Images.COLUMN_NAME_CATEGORY_NAME,
            Images.COLUMN_NAME_IMAGE_PATH,
            Images.COLUMN_NAME_IMAGE_SEQUENCE)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${Images.COLUMN_NAME_PROJECT_ID} = ?"
        val selectionArgs = arrayOf(projectId)


        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${BaseColumns._ID} ASC"

        val cursor = dbReadable.query(
            Images.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder                       // The sort order
        )

        return cursor.count
    }

    fun insertImage(image : Image) {
        val values = ContentValues().apply {
            put(Images.COLUMN_NAME_PROJECT_ID, image.projectId)
            put(Images.COLUMN_NAME_SKU_NAME, image.skuName)
            put(Images.COLUMN_NAME_SKU_ID, image.skuId)
            put(Images.COLUMN_NAME_CATEGORY_NAME, image.categoryName)
            put(Images.COLUMN_NAME_IMAGE_PATH, image.imagePath)
            put(Images.COLUMN_NAME_IMAGE_SEQUENCE, image.sequence)
            put(Images.COLUMN_NAME_IS_UPLOADED, 0)
            put(Images.COLUMN_NAME_IMAGE_ANGLE, image.angle)
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
         val selection = "${Images.COLUMN_NAME_IS_UPLOADED} = ?"
        val projectSelectionArgs = arrayOf("0")

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

    fun getOldestSkippedImage() : Image {
        val projection = arrayOf(
            BaseColumns._ID,
            Images.COLUMN_NAME_PROJECT_ID,
            Images.COLUMN_NAME_SKU_NAME,
            Images.COLUMN_NAME_SKU_ID,
            Images.COLUMN_NAME_CATEGORY_NAME,
            Images.COLUMN_NAME_IMAGE_PATH,
            Images.COLUMN_NAME_IMAGE_SEQUENCE)

        // Filter results WHERE "title" = 'My Title'
        val selection = "${Images.COLUMN_NAME_IS_UPLOADED} = ?"
        val projectSelectionArgs = arrayOf("-1")

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
        //update project subcategory name and subcategory id
        updateProjectSubcategory(sku)

        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put(ShootContract.ShootEntry.COLUMN_NAME_CREATED_ON, sku.createdOn)
            put(ShootContract.ShootEntry.COLUMN_NAME_PROJECT_NAME, sku.projectName)
            put(ShootContract.ShootEntry.COLUMN_NAME_SKU_NAME, sku.skuName)
            put(ShootContract.ShootEntry.COLUMN_NAME_EXTERIOR_ANGLES, sku.exteriorAngles)
            put(ShootContract.ShootEntry.COLUMN_NAME_CATEGORY_NAME, sku.categoryName)
            put(ShootContract.ShootEntry.COLUMN_NAME_CATEGORY_ID, sku.categoryId)
            put(ShootContract.ShootEntry.COLUMN_NAME_SUB_CATEGORY_NAME, sku.subcategoryName)
            put(ShootContract.ShootEntry.COLUMN_NAME_SUB_CATEGORY_ID, sku.subcategoryId)
            put(ShootContract.ShootEntry.COLUMN_NAME_PROJECT_ID, sku.projectId)
            put(ShootContract.ShootEntry.COLUMN_NAME_SKU_ID, sku.skuId)
            put(ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES, sku.totalImages)
            put(ShootContract.ShootEntry.COLUMN_NAME_UPLOADED_IMAGES, 0)
            put(ShootContract.ShootEntry.COLUMN_NAME_PROCESS_SKU, 0)
            put(ShootContract.ShootEntry.COLUMN_NAME_IS_PROCESSED, -1)
        }

        val newRowId = dbWritable?.insert(ShootContract.ShootEntry.TABLE_NAME, null, values)

        com.spyneai.shoot.utils.log("insertSku: "+newRowId)
    }

    private fun updateProjectSubcategory(sku: Sku) {
        val projectValues = ContentValues().apply {
            put(
                Projects.COLUMN_NAME_SUB_CATEGORY_NAME,
                sku.subcategoryName
            )
            put(
                Projects.COLUMN_NAME_SUB_CATEGORY_ID,
                sku.subcategoryId
            )
            put(
                Projects.COLUMN_NAME_EXTERIOR_ANGLES,
                sku.exteriorAngles
            )
        }

        val projectSelection = "${ShootContract.ShootEntry.COLUMN_NAME_PROJECT_ID} LIKE ?"
        // Which row to update, based on the title


        val projectSelectionArgs = arrayOf(sku.projectId)


        val projectCount = dbWritable.update(
            Projects.TABLE_NAME,
            projectValues,
            projectSelection,
            projectSelectionArgs)

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

        com.spyneai.shoot.utils.log("deleteImage : "+count)
    }

    fun getLastSku() : Sku {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(
            BaseColumns._ID,
            ShootContract.ShootEntry.COLUMN_NAME_SKU_ID,
            ShootContract.ShootEntry.COLUMN_NAME_PROJECT_ID,
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
                val projectId = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_PROJECT_ID))
                val backgroundId = getString(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_BACKGROUND_ID))
                val is360 = getInt(getColumnIndexOrThrow(ShootContract.ShootEntry.COLUMN_NAME_IS_360))

                sku.itemId = itemId
                sku.projectId = skuId
                sku.skuId = projectId
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

    fun updateSubcategoryId(skuId : String,subCategoryId : String,subCategoryName: String) {
        val values = ContentValues().apply {
            put(
                ShootContract.ShootEntry.COLUMN_NAME_SUB_CATEGORY_ID,
                subCategoryId
            )
            put(
                ShootContract.ShootEntry.COLUMN_NAME_SUB_CATEGORY_NAME,
                subCategoryName
            )
        }

        val selection = "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} LIKE ?"

        val selectionArgs = arrayOf(skuId)


        val count = dbWritable.update(
            ShootContract.ShootEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs)



    }

    fun processSku(skuId: String) : Boolean {
        val sku = getUploadedAndTotalImagesCount(skuId)
        return sku.uploadedImages == sku.totalImages && sku.processSku == 1
    }

    fun isImagesUploaded(skuId : String) : Boolean {
        val sku = getUploadedAndTotalImagesCount(skuId)
        return sku.uploadedImages == sku.totalImages
    }

    fun updateIsProcessed(projectId : String,skuId : String) {
        updateProjectStatus(projectId)
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



}
