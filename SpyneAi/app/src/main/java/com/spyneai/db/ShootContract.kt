package com.spyneai.db

import android.provider.BaseColumns

object ShootContract {

    // Table contents are grouped together in an anonymous object.
    object ShootEntry : BaseColumns {
        const val TABLE_NAME = "shoot_table"
        const val COLUMN_NAME_IMAGE_ID = "image_id"
        const val COLUMN_NAME_TOTAL_IMAGES = "total_images"
        const val COLUMN_NAME_UPLOADED_IMAGES = "uploaded_images"
        const val COLUMN_NAME_PROJECT_NAME = "project_name"
        const val COLUMN_NAME_SKU_NAME = "sku_name"
        const val COLUMN_NAME_SKU_ID = "sku_id"
        const val COLUMN_NAME_CREATED_ON = "created_on"
        const val COLUMN_NAME_CATEGORY_NAME = "category_name"
        const val COLUMN_NAME_CATEGORY_ID = "category_id"
        const val COLUMN_NAME_SUB_CATEGORY_NAME = "subcategory_name"
        const val COLUMN_NAME_SUB_CATEGORY_ID = "subcategory_id"
        const val COLUMN_NAME_BACKGROUND_ID = "background_id"
        const val COLUMN_NAME_BACKGROUND_COLOR = "background_color"
        const val COLUMN_NAME_MARKET_PLACE_ID = "market_place_id"
        const val COLUMN_NAME_DEALERSHIP_LOG = "dealership_logo"
        const val COLUMN_NAME_PROCESS_SKU = "process_sku"
        const val COLUMN_NAME_PROJECT_ID = "project_id"
        const val COLUMN_NAME_EXTERIOR_ANGLES = "exterior_angles"
        const val COLUMN_NAME_THREE_SIXTY_FRAMES = "frames"
        const val COLUMN_NAME_IS_PROCESSED = "is_processed"
        const val COLUMN_NAME_IS_360 = "is_360"
    }
}