package com.spyneai.db

import android.provider.BaseColumns

object Images : BaseColumns {
    const val TABLE_NAME = "shoot_images"
    const val COLUMN_NAME_PROJECT_ID = "project_id"
    const val COLUMN_NAME_SKU_ID = "sku_id"
    const val COLUMN_NAME_CATEGORY_NAME = "category_name"
    const val COLUMN_NAME_IMAGE_PATH = "image_path"
    const val COLUMN_NAME_IMAGE_SEQUENCE = "image_sequence"
}