package com.spyneai.db

import android.provider.BaseColumns

object ImageFiles  : BaseColumns {
    const val TABLE_NAME = "image_files"
    const val COLUMN_NAME_SKU_NAME = "sku_name"
    const val COLUMN_NAME_SKU_ID = "sku_id"
    const val COLUMN_NAME_CATEGORY_NAME = "category_name"
    const val COLUMN_NAME_IMAGE_PATH = "image_path"
    const val COLUMN_NAME_IMAGE_SEQUENCE = "image_sequence"
    const val COLUMN_NAME_IS_UPLOADED = "is_uploaded"
}