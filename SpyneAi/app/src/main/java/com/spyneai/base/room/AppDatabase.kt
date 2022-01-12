package com.spyneai.base.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.spyneai.StringListConvertor
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.repository.DashboardDao
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.orders.data.paging.PagingDao
import com.spyneai.orders.data.paging.ProjectPagedRes
import com.spyneai.orders.data.paging.RemoteKeys
import com.spyneai.orders.data.paging.RemoteKeysDao
import com.spyneai.shoot.data.model.CarsBackgroundRes
import com.spyneai.shoot.repository.db.ShootDao
import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.sku.Sku

@Database(entities = [User::class,
    NewCategoriesResponse.Category::class,
    NewSubCatResponse.Subcategory::class,
    NewSubCatResponse.Interior::class,
    NewSubCatResponse.Miscellaneous::class,
    NewSubCatResponse.Tags.ExteriorTags::class,
    NewSubCatResponse.Tags.InteriorTags::class,
    NewSubCatResponse.Tags.FocusShoot::class,
    OverlaysResponse.Overlays::class,
    CarsBackgroundRes.Background::class,
    Project::class,
    Sku::class,
    Image::class,
    ProjectPagedRes.ProjectPagedResItem::class,
    RemoteKeys::class],
    version = 1,
    exportSchema = false)
@TypeConverters(StringListConvertor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun dashboardDao(): DashboardDao
    abstract fun shootDao(): ShootDao

    abstract fun getRepoDao(): RemoteKeysDao
    abstract fun getPagingDao(): PagingDao


    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "spyne_app_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}