package com.spyneai.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class DBHelper (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_PROJECTS_TABLE)
        db.execSQL(SQL_CREATE_ENTRIES)
        db.execSQL(CREATE_IMAGES_TABLE)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_PROJECTS)
        db.execSQL(SQL_DELETE_ENTRIES)
        db.execSQL(SQL_DELETE_IMAGES)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 7
        const val DATABASE_NAME = "Shoot.db"

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${ShootContract.ShootEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_IMAGE_ID} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES} INTEGER," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_UPLOADED_IMAGES} INTEGER," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_CATEGORY_NAME} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_PROJECT_NAME} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_SKU_NAME} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_CREATED_ON} INTEGER," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_EXTERIOR_ANGLES} INTEGER," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_CATEGORY_ID} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_SUB_CATEGORY_NAME} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_SUB_CATEGORY_ID} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_BACKGROUND_ID} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_BACKGROUND_COLOR} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_MARKET_PLACE_ID} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_DEALERSHIP_LOG} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_PROJECT_ID} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_PROCESS_SKU} INTEGER," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_IS_PROCESSED} INTEGER," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_IS_360} INTEGER," +
                    "${ShootContract.ShootEntry.TABLE_NAME} TEXT)"

        private const val CREATE_IMAGES_TABLE =  "CREATE TABLE ${Images.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${Images.COLUMN_NAME_PROJECT_ID} TEXT," +
                "${Images.COLUMN_NAME_SKU_ID} TEXT," +
                "${Images.COLUMN_NAME_CATEGORY_NAME} TEXT," +
                "${Images.COLUMN_NAME_IMAGE_PATH} TEXT," +
                "${Images.COLUMN_NAME_IMAGE_SEQUENCE} INTEGER," +
                "${Images.COLUMN_NAME_IS_UPLOADED} INTEGER," +
                "${Images.TABLE_NAME} TEXT)"

        private const val CREATE_PROJECTS_TABLE =  "CREATE TABLE ${Projects.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${Projects.COLUMN_NAME_PROJECT_NAME} TEXT," +
                "${Projects.COLUMN_NAME_CREATED_ON} INTEGER," +
                "${Projects.COLUMN_NAME_CATEGORY_NAME} TEXT," +
                "${Projects.COLUMN_NAME_CATEGORY_ID} TEXT," +
                "${Projects.COLUMN_NAME_SUB_CATEGORY_NAME} TEXT," +
                "${Projects.COLUMN_NAME_SUB_CATEGORY_ID} TEXT," +
                "${Projects.COLUMN_NAME_EXTERIOR_ANGLES} INTEGER," +
                "${Projects.COLUMN_NAME_PROJECT_ID} TEXT," +
                "${Projects.COLUMN_NAME_STATUS} TEXT)"


        private const val SQL_DELETE_PROJECTS = "DROP TABLE IF EXISTS ${Projects.TABLE_NAME}"
        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ShootContract.ShootEntry.TABLE_NAME}"
        private const val SQL_DELETE_IMAGES = "DROP TABLE IF EXISTS ${Images.TABLE_NAME}"
    }
}