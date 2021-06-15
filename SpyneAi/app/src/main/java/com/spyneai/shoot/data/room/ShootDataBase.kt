package com.spyneai.shoot.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.spyneai.shoot.data.room.entities.ShootEntity

@Database(
    entities = [ShootEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ShootDataBase: RoomDatabase() {

    abstract fun getShootDao(): ShootDAO

    companion object{
        @Volatile
        private var instance: ShootDataBase? = null
        private val Lock= Any()

        operator fun invoke(context: Context) = instance ?: synchronized(Lock){
            instance?:buildDatabase(context).also {
                instance = it
            }
        }
        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ShootDataBase::class.java,
                "SHOOT_DATABASE"
            ).build()
    }
}