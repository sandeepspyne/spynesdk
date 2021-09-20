package com.spyneai.db

import android.provider.BaseColumns

object Videos : BaseColumns {
    const val TABLE_NAME = "shoot_videos"
    const val COLUMN_NAME_PROJECT_ID = "project_id"
    const val COLUMN_NAME_SKU_NAME = "sku_name"
    const val COLUMN_NAME_SKU_ID = "sku_id"
    const val COLUMN_NAME_TYPE = "type"
    const val COLUMN_NAME_CATEGORY_NAME = "category_name"
    const val COLUMN_NAME_CATEGORY_SUBCATEGORY_NAME = "subcategory_name"
    const val COLUMN_NAME_VIDEO_PATH = "video_path"
    const val COLUMN_NAME_FRAMES = "frames"
    const val COLUMN_NAME_BACKGROUND_ID = "background_id"
    const val COLUMN_NAME_IS_UPLOADED = "is_uploaded"
    const val COLUMN_NAME_IS_STATUS_UPDATED = "is_status_updated"
    const val COLUMN_NAME_PRE_SIGNED_URL = "pre_signed_url"
    const val COLUMN_NAME_VIDEO_ID = "video_id"
}