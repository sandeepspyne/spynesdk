package com.spyneai.db

import android.provider.BaseColumns

object Projects : BaseColumns {
    const val TABLE_NAME = "projects"
    const val COLUMN_NAME_PROJECT_NAME = "project_name"
    const val COLUMN_NAME_CREATED_ON = "created_on"
    const val COLUMN_NAME_CATEGORY_NAME = "category_name"
    const val COLUMN_NAME_CATEGORY_ID = "category_id"
    const val COLUMN_NAME_SUB_CATEGORY_NAME = "sub_category_name"
    const val COLUMN_NAME_SUB_CATEGORY_ID = "sub_category_id"
    const val COLUMN_NAME_EXTERIOR_ANGLES = "exterior_angles"
    const val COLUMN_NAME_PROJECT_ID = "project_id"
    const val COLUMN_NAME_STATUS = "status"
}