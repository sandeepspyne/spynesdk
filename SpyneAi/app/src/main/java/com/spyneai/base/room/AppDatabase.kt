package com.spyneai.base.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spyneai.shoot.data.room.Project
import com.spyneai.shoot.data.room.ProjectDao
import com.spyneai.shoot.data.room.Sku
import com.spyneai.shoot.data.room.SkuDao

@Database(entities = [User::class, Project::class, Sku::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun skuDao(): SkuDao
}