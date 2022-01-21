package com.spyneai.shoot.data

import android.provider.BaseColumns
import androidx.room.Room
import com.spyneai.BaseApplication
import com.spyneai.base.room.AppDatabase
import com.spyneai.db.Images
import com.spyneai.shoot.repository.db.ShootDao
import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.repository.model.image.ImageDao

class ImagesRepoV2(val imageDao: ImageDao) {

    fun isImageExist(skuUuid: String,imageName: String) : Boolean {
        val image = imageDao.getImage(skuUuid,imageName)

        return image != null
    }

    fun updateImage(image: Image) = imageDao.updateImage(image)

    fun getImagesBySkuId(uuid: String) = imageDao.getImagesBySkuId(uuid)

    fun getImagesPathBySkuId(uuid: String) = imageDao.getImagesPathBySkuId(uuid)

    fun totalRemainingUpload() = imageDao.totalRemainingUpload()

    fun totalRemainingMarkDone() = imageDao.totalRemainingUpload(true, isMarkedDone = false)

    fun getRemainingAbove(uuid: String) = 0

    fun getRemainingAboveSkipped(uuid: String) = 0

    fun getRemainingBelow(uuid: String) = 0

    fun getRemainingBelowSkipped(uuid: String) = 0

    fun getOldestImage() = imageDao.getOldestImage()

    fun skipImage(uuid: String,toProcessAt: Long) = imageDao.skipImage(uuid,toProcessAt)

    fun markDone(uuid: String) = imageDao.markDone(uuid)

    fun addPreSignedUrl(image: Image) = imageDao.addPreSignedUrl(image.uuid,image.preSignedUrl!!,image.imageId!!)

    fun markUploaded(uuid: String) = imageDao.markUploaded(uuid)

    fun markStatusUploaded(uuid: String) = imageDao.markStatusUploaded(uuid)

    fun getImage(uuid: String): Image = imageDao.getImage(uuid)
}