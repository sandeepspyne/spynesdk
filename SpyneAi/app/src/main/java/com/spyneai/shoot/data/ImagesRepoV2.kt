package com.spyneai.shoot.data

import android.provider.BaseColumns
import androidx.room.Room
import com.spyneai.BaseApplication
import com.spyneai.base.room.AppDatabase
import com.spyneai.db.Images
import com.spyneai.shoot.repository.db.ShootDao
import com.spyneai.shoot.repository.model.image.Image

class ImagesRepoV2(val shootDao: ShootDao) {

    fun isImageExist(skuUuid: String,imageName: String) : Boolean {
        val image = shootDao.getImage(skuUuid,imageName)

        return image != null
    }

    fun updateImage(image: Image) = shootDao.updateImage(image)

    fun getImagesBySkuId(uuid: String) = shootDao.getImagesBySkuId(uuid)

    fun getImagesPathBySkuId(uuid: String) = shootDao.getImagesPathBySkuId(uuid)

    fun totalRemainingUpload() = shootDao.totalRemainingUpload()

    fun totalRemainingMarkDone() = shootDao.totalRemainingUpload(true, isMarkedDone = false)

    fun getRemainingAbove(uuid: String) = 0

    fun getRemainingAboveSkipped(uuid: String) = 0

    fun getRemainingBelow(uuid: String) = 0

    fun getRemainingBelowSkipped(uuid: String) = 0

    fun getOldestImage() = shootDao.getOldestImage()

    fun skipImage(uuid: String,toProcessAt: Long) = shootDao.skipImage(uuid,toProcessAt)

    fun markDone(uuid: String) = shootDao.markDone(uuid)

    fun addPreSignedUrl(image: Image) = shootDao.addPreSignedUrl(image.uuid,image.preSignedUrl!!,image.imageId!!)

    fun markUploaded(uuid: String) = shootDao.markUploaded(uuid)

    fun markStatusUploaded(uuid: String) = shootDao.markStatusUploaded(uuid)

    fun getImage(uuid: String): Image = shootDao.getImage(uuid)
}