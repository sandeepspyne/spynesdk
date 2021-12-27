package com.spyneai.base.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spyneai.shoot.data.room.*

@Database(entities = [User::class,
    Project::class,
    Sku::class,
    Image::class],
    version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun skuDao(): SkuDao
    abstract fun imageDao(): ImageDao
}