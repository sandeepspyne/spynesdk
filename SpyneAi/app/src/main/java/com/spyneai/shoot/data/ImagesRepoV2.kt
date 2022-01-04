package com.spyneai.shoot.data

import androidx.room.Room
import com.spyneai.BaseApplication
import com.spyneai.base.room.AppDatabase
import com.spyneai.shoot.repository.db.ShootDao

class ImagesRepoV2(val shootDao: ShootDao) {

    fun getImagesBySkuId(uuid: String) = shootDao.getImagesBySkuId(uuid)

    fun getImagesPathBySkuId(uuid: String) = shootDao.getImagesPathBySkuId(uuid)
}