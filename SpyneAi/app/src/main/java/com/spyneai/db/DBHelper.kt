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
        db.execSQL(CREATE_IMAGES_FILES_TABLE)
        db.execSQL(CREATE_VIDEOS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        if (newVersion == 9){
            db.execSQL(DATABASE_ALTER_SKU_TABLE)
            db.execSQL(DATABASE_ALTER_IMAGE_TABLE)
            db.execSQL(DATABASE_ALTER_SKU)
        }else {
            db.execSQL(SQL_DELETE_PROJECTS)
            db.execSQL(SQL_DELETE_ENTRIES)
            db.execSQL(SQL_DELETE_IMAGES)
            db.execSQL(SQL_DELETE_IMAGE_FILES)
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 9
        const val DATABASE_NAME = "Shoot.db"

        private val DATABASE_ALTER_SKU_TABLE = ("ALTER TABLE "
                + Images.TABLE_NAME) + " ADD COLUMN " + Images.COLUMN_NAME_IMAGE_META + " TEXT;"

        private val DATABASE_ALTER_SKU = ("ALTER TABLE "
                + ShootContract.ShootEntry.TABLE_NAME) + " ADD COLUMN " + ShootContract.ShootEntry.COLUMN_NAME_THREE_SIXTY_FRAMES + " INTEGER;"

        private val DATABASE_ALTER_IMAGE_TABLE = ("ALTER TABLE "
                + Images.TABLE_NAME) + " ADD COLUMN " + Images.COLUMN_NAME_IMAGE_ANGLE + " INTEGER;"


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
                    "${ShootContract.ShootEntry.COLUMN_NAME_THREE_SIXTY_FRAMES} INTEGER," +
                    "${ShootContract.ShootEntry.TABLE_NAME} TEXT)"

        private const val CREATE_IMAGES_TABLE =  "CREATE TABLE ${Images.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${Images.COLUMN_NAME_PROJECT_ID} TEXT," +
                "${Images.COLUMN_NAME_SKU_ID} TEXT," +
                "${Images.COLUMN_NAME_SKU_NAME} TEXT," +
                "${Images.COLUMN_NAME_CATEGORY_NAME} TEXT," +
                "${Images.COLUMN_NAME_IMAGE_PATH} TEXT," +
                "${Images.COLUMN_NAME_IMAGE_SEQUENCE} INTEGER," +
                "${Images.COLUMN_NAME_IMAGE_ANGLE} INTEGER," +
                "${Images.COLUMN_NAME_IS_UPLOADED} INTEGER," +
                "${Images.COLUMN_NAME_IMAGE_META} TEXT," +
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

        private const val CREATE_IMAGES_FILES_TABLE =  "CREATE TABLE ${ImageFiles.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${ImageFiles.COLUMN_NAME_SKU_ID} TEXT," +
                "${ImageFiles.COLUMN_NAME_SKU_NAME} TEXT," +
                "${ImageFiles.COLUMN_NAME_CATEGORY_NAME} TEXT," +
                "${ImageFiles.COLUMN_NAME_IMAGE_PATH} TEXT NOT NULL UNIQUE," +
                "${ImageFiles.COLUMN_NAME_IMAGE_SEQUENCE} TEXT," +
                "${ImageFiles.COLUMN_NAME_IS_UPLOADED} INTEGER," +
                "${ImageFiles.TABLE_NAME} TEXT)"

        private const val CREATE_VIDEOS_TABLE =  "CREATE TABLE ${Videos.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${Videos.COLUMN_NAME_PROJECT_ID} TEXT," +
                "${Videos.COLUMN_NAME_SKU_NAME} TEXT," +
                "${Videos.COLUMN_NAME_SKU_ID} TEXT," +
                "${Videos.COLUMN_NAME_TYPE} TEXT," +
                "${Videos.COLUMN_NAME_CATEGORY_NAME} TEXT," +
                "${Videos.COLUMN_NAME_CATEGORY_SUBCATEGORY_NAME} TEXT," +
                "${Videos.COLUMN_NAME_VIDEO_PATH} TEXT NOT NULL UNIQUE," +
                "${Videos.COLUMN_NAME_FRAMES} INTEGER," +
                "${Videos.COLUMN_NAME_BACKGROUND_ID} TEXT," +
                "${Videos.COLUMN_NAME_IS_UPLOADED} INTEGER," +
                "${Videos.COLUMN_NAME_IS_STATUS_UPDATED} INTEGER," +
                "${Videos.COLUMN_NAME_PRE_SIGNED_URL} TEXT," +
                "${Videos.COLUMN_NAME_VIDEO_ID} TEXT," +
                "${Videos.TABLE_NAME} TEXT)"


        private const val SQL_DELETE_PROJECTS = "DROP TABLE IF EXISTS ${Projects.TABLE_NAME}"
        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ShootContract.ShootEntry.TABLE_NAME}"
        private const val SQL_DELETE_IMAGES = "DROP TABLE IF EXISTS ${Images.TABLE_NAME}"
        private const val SQL_DELETE_IMAGE_FILES = "DROP TABLE IF EXISTS ${ImageFiles.TABLE_NAME}"
    }
}