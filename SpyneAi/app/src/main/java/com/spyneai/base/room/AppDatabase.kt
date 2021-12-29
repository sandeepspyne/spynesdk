package com.spyneai.base.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spyneai.shoot.repository.db.ShootDao
import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.sku.Sku

@Database(entities = [User::class,
    Project::class,
    Sku::class,
    Image::class],
    version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun shootDao(): ShootDao
}