package com.spyneai.base.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spyneai.dashboard.repository.DashboardDao
import com.spyneai.dashboard.repository.model.category.DynamicLayout
import com.spyneai.dashboard.repository.model.category.ProjectDialog
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.shoot.repository.db.ShootDao
import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.sku.Sku

@Database(entities = [User::class,
    NewCategoriesResponse.Category::class,
    NewSubCatResponse.Subcategory::class,
    NewSubCatResponse.Interior::class,
    NewSubCatResponse.Miscellaneous::class,
    Project::class,
    Sku::class,
    Image::class],
    version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun dashboardDao(): DashboardDao
    abstract fun shootDao(): ShootDao
}