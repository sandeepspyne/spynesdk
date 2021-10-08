package com.spyneai.db

import android.provider.BaseColumns

object Images : BaseColumns {
    const val TABLE_NAME = "shoot_images"
    const val COLUMN_NAME_PROJECT_ID = "project_id"
    const val COLUMN_NAME_SKU_NAME = "sku_name"
    const val COLUMN_NAME_SKU_ID = "sku_id"
    const val COLUMN_NAME_CATEGORY_NAME = "category_name"
    const val COLUMN_NAME_IMAGE_PATH = "image_path"
    const val COLUMN_NAME_IMAGE_SEQUENCE = "image_sequence"
    const val COLUMN_NAME_IS_UPLOADED = "is_uploaded"
    const val COLUMN_NAME_IMAGE_ANGLE = "angle"
    const val COLUMN_NAME_IMAGE_META = "image_meta"
    const val COLUMN_NAME_IMAGE_NAME = "image_name"
    const val COLUMN_NAME_IMAGE_PRE_SIGNED_URL = "image_pre_signed_url"
    const val COLUMN_NAME_OVERLAY_ID = "overlay_id"
    const val COLUMN_NAME_IMAGE_ID = "image_id"
    const val COLUMN_NAME_IMAGE_DEBUG_DATA = "image_debug_data"
    const val COLUMN_NAME_IS_STATUS_UPDATED = "is_status_updated"
    const val COLUMN_NAME_IS_RE_CLICK = "is_reclick"
    const val COLUMN_NAME_IS_RESHOOT = "is_reshoot"
}