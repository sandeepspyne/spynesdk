package com.spyneai.shoot.repository.model.image

import androidx.room.*
import com.spyneai.getUuid
import com.spyneai.needs.AppConstants

@Dao
interface ImageDao {
    @Query("Select path from image where skuUuid = :skuUuid")
    fun getImagesPathBySkuId(skuUuid : String) : List<String>

    @Query("select * from image where skuId NOT NUll and projectId NOT NULL and (isUploaded = :isUploaded or isMarkedDone = :isMarkedDone) and toProcessAt <= :currentTime limit 1")
    fun getOldestImage(isUploaded: Boolean = false,isMarkedDone : Boolean = false,currentTime: Long = System.currentTimeMillis()) : Image

    @Query("update image set toProcessAt = :toProcessAt, retryCount = retryCount + 1 where uuid = :uuid")
    fun skipImage(uuid: String,toProcessAt: Long) : Int

    @Query("update image set isUploaded = :done,isMarkedDone = :done where uuid = :uuid")
    fun markDone(uuid: String,done: Boolean = true) : Int

    @Query("update image set preSignedUrl = :preUrl, imageId = :imageId where uuid = :uuid")
    fun addPreSignedUrl(uuid: String,preUrl: String,imageId : String) : Int

    @Query("update image set isUploaded = :isUploaded where uuid = :uuid")
    fun markUploaded(uuid: String,isUploaded: Boolean = true) : Int

    @Query("update image set isMarkedDone = :isMarkedDone where uuid = :uuid")
    fun markStatusUploaded(uuid: String,isMarkedDone: Boolean = true) : Int

    @Query("select * from image where uuid = :uuid")
    fun getImage(uuid: String) : Image

    @Query("select * from image where imageId = :imageId")
    fun getImageWithImageId(imageId: String?) : Image

    @Query("select * from image where skuUuid = :skuUuid and name = :imageName")
    fun getImage(skuUuid: String,imageName: String) : Image

    @Update
    fun updateImage(image: Image): Int

    @Transaction
    suspend fun insertImagesWithCheck(response: ArrayList<Image>,projectUuid: String,skuUuid: String){
        val list = ArrayList<Image>()

        response.forEach {

            var dbItem = getImageWithImageId(it.imageId)

            if (dbItem == null && it.uuid != null)
                dbItem = getImage(it.uuid)

            if (dbItem == null){
                if (it.uuid ==  null){
                    it.uuid = getUuid()
                }

                if(it.overlayId == null)
                    it.overlayId = "1234"

                if(it.output_image_hres_url == null)
                    it.output_image_hres_url = ""

                if(it.output_image_lres_url == null)
                    it.output_image_lres_url = ""

                if(it.output_image_lres_wm_url == null)
                    it.output_image_lres_wm_url = ""

                it.projectUuid = projectUuid
                it.skuUuid = skuUuid
                it.path = it.input_image_lres_url

                if (it.status == "Done" || it.status == "Yet to Start" || it.status == "Failed"){
                    it.isMarkedDone = true
                    it.isUploaded = true
                }

                it.preSignedUrl = AppConstants.DEFAULT_PRESIGNED_URL
                list.add(it)
            }
        }

        insertAllImages(list)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllImages(imageList: List<Image>) : List<Long>

    @Query("select Count(*) from image where isUploaded = :isUploaded and isMarkedDone = :isMarkedDone")
    fun totalRemainingUpload(isUploaded: Boolean = false,isMarkedDone : Boolean = false) : Int

    @Query("select * from image where skuUuid = :skuUuid")
    fun getImagesBySkuId(skuUuid: String): List<Image>
}