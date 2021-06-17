package com.spyneai.shoot.data.sqlite

import android.provider.BaseColumns

object ShootContract {

    // Table contents are grouped together in an anonymous object.
    object ShootEntry : BaseColumns {
        const val TABLE_NAME = "shoot_table"
        const val COLUMN_NAME_IMAGE_ID = "image_id"
        const val COLUMN_NAME_TOTAL_IMAGES = "total_images"
        const val COLUMN_NAME_UPLOADED_IMAGES = "uploaded_images"
        const val COLUMN_NAME_SKU_ID = "sku_id"
        const val COLUMN_NAME_CATEGORY_NAME = "category_name"
        const val COLUMN_NAME_BACKGROUND_ID = "background_id"
        const val COLUMN_NAME_BACKGROUND_COLOR = "background_color"
        const val COLUMN_NAME_MARKET_PLACE_ID = "market_place_id"
        const val COLUMN_NAME_DEALERSHIP_LOG = "dealership_logo"
        const val COLUMN_NAME_PROCESS_SKU = "process_sku"
        const val COLUMN_NAME_PROJECT_ID = "project_id"
    }
}