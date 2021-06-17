package com.spyneai.shoot.data.sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class DBHelper (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Shoot.db"

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${ShootContract.ShootEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_IMAGE_ID} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_TOTAL_IMAGES} INTEGER," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_UPLOADED_IMAGES} INTEGER," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_SKU_ID} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_CATEGORY_NAME} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_BACKGROUND_ID} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_BACKGROUND_COLOR} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_MARKET_PLACE_ID} TEXT," +
                    "${ShootContract.ShootEntry.COLUMN_NAME_DEALERSHIP_LOG} TEXT," +
                    "${ShootContract.ShootEntry.TABLE_NAME} TEXT)"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ShootContract.ShootEntry.TABLE_NAME}"
    }
}