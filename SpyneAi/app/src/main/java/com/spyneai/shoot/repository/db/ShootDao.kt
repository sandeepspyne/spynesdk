package com.spyneai.shoot.repository.db

import android.util.Log
import androidx.room.*
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.getUuid
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.model.CarsBackgroundRes

import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.project.ProjectWithSku
import com.spyneai.shoot.repository.model.sku.Sku
import io.sentry.protocol.App


@Dao
interface ShootDao {

    //subcategories  queries
    @Transaction
    fun saveSubcategoriesData(
        subCategories: List<NewSubCatResponse.Subcategory>,
        interior: List<NewSubCatResponse.Interior>,
        misc: List<NewSubCatResponse.Miscellaneous>,
        exteriorTagsTags: List<NewSubCatResponse.Tags.ExteriorTags>,
        interiorTags: List<NewSubCatResponse.Tags.InteriorTags>,
        focusTags: List<NewSubCatResponse.Tags.FocusShoot>){

        subcategories(subCategories)
        insertInterior(interior)
        insertMisc(misc)

        insertExteriorTags(exteriorTagsTags)
        insertInteriorTags(interiorTags)
        insertFocusTags(focusTags)
    }

    @Insert
    fun subcategories(list: List<NewSubCatResponse.Subcategory>)

    @Insert
    fun insertInterior(list: List<NewSubCatResponse.Interior>) : List<Long>

    @Insert
    fun insertMisc(list: List<NewSubCatResponse.Miscellaneous>)

    @Query("SELECT * FROM subcategory")
    fun getSubcategories(): List<NewSubCatResponse.Subcategory>

    @Query("SELECT * FROM interior where prodCatId = :prodCatId")
    fun getInterior(prodCatId: String) : List<NewSubCatResponse.Interior>

    @Query("SELECT * FROM miscellaneous where prod_cat_id = :prodCatId")
    fun getMisc(prodCatId: String) : List<NewSubCatResponse.Miscellaneous>

    @Insert
    fun insertExteriorTags(list: List<NewSubCatResponse.Tags.ExteriorTags>) : List<Long>

    @Insert
    fun insertInteriorTags(list: List<NewSubCatResponse.Tags.InteriorTags>)

    @Insert
    fun insertFocusTags(list: List<NewSubCatResponse.Tags.FocusShoot>)

    @Insert
    fun insertOverlays(overlays: List<OverlaysResponse.Overlays>)

    @Query("SELECT * FROM overlays where prod_sub_cat_id = :prodSubcategoryId and fetchAngle = :fetchAngle")
    fun getOverlays(prodSubcategoryId: String, fetchAngle: Int) : List<OverlaysResponse.Overlays>

    @Insert
    fun insertBackgrounds(backgrounds: List<CarsBackgroundRes.Background>)

    @Query("SELECT * FROM background where category = :category")
    fun getBackgrounds(category: String) : List<CarsBackgroundRes.Background>

    @Insert
    fun insertProject(obj: Project) : Long

    @Insert
    fun insertSku(obj: Sku) : Long

    @Update
    fun updateProject(project: Project): Int

    @Query("update project set projectId = :projectId, isCreated = :isCreated where uuid = :uuid")
    fun updateProjectServerId(uuid: String,projectId: String,isCreated: Boolean = true): Int

    @Query("select COUNT(*) from project where isCreated = :isCreated ")
    fun getPendingProjects(isCreated: Boolean = false) : Int

    @Query("select * from project where isCreated = :isCreated LIMIT :limit")
    fun getOldestProject(isCreated: Boolean = false,limit: Int = 1) : Project

    @Query("select COUNT(*) from sku where is_processed = :isProcessed and isCreated = :isCreated and background_id != 'DEFAULT_BG_ID'")
    fun getPendingSku(isProcessed: Boolean = false,isCreated: Boolean = true) : Int

    @Query("select * from sku where is_processed = :isProcessed and isCreated = :isCreated LIMIT :limit")
    fun getOldestSku(isProcessed: Boolean = false,isCreated: Boolean = true,limit: Int = 1): Sku

    @Query("select * from sku")
    fun getAllSKus() : List<Sku>

    @Update
    fun updateSku(sku: Sku): Int

    @Query("update sku set sku_id = :skuId,project_id = :projectId, isCreated = :isCreated where uuid = :uuid")
    fun updateSKuServerId(uuid: String,skuId: String,projectId: String,isCreated: Boolean = true): Int

    @Query("update image set skuId = :skuId, projectId = :projectId where skuUuid = :skuUuid")
    fun updateImageIds(skuUuid: String,skuId: String,projectId: String): Int

    @Transaction
    fun updateSkuAndImageIds(projectId: String,skuUuid: String,skuId: String){
        updateSKuServerId(skuUuid,skuId,projectId)
        updateImageIds(skuUuid,skuId,projectId)
    }

    @Transaction
    fun updateSubcategory(project: Project,sku: Sku){
        val projectUpdate = updateProject(project)
        Log.d(AppConstants.SHOOT_DAO_TAG, "updateSubcategory: $projectUpdate")
        val skuUpdate = updateSku(sku)
        Log.d(AppConstants.SHOOT_DAO_TAG, "updateSubcategory: $skuUpdate")
    }

    @Query("SELECT * FROM project where status = 'draft'")
    fun getDraftProjects(): List<Project>

    @Query("SELECT * FROM sku where project_uuid = :projectUuid")
    fun getSkusByProjectId(projectUuid: String) : List<Sku>

    @Query("select * from image where skuUuid = :skuUuid")
    fun getImagesBySkuId(skuUuid: String): List<Image>

    @Transaction
    fun saveSku(sku : Sku,project: Project){
        val skuid = insertSku(sku)
        Log.d(AppConstants.SHOOT_DAO_TAG, "saveSku: $skuid")
        Log.d(AppConstants.SHOOT_DAO_TAG, "saveSku: "+sku.projectUuid)
        val projectudpate = updateProjectSkuCount(project.uuid)
        Log.d(AppConstants.SHOOT_DAO_TAG, "saveSku: $projectudpate")
    }

    @Insert
    fun insertImage(obj: Image) : Long

    @Query("UPDATE project SET imagesCount = imagesCount + 1 WHERE uuid =:uuid ")
    fun updateProjectImageCount(uuid: String) : Int

    @Query("UPDATE project SET skuCount = skuCount + 1, isCreated = :isCreated WHERE uuid =:uuid ")
    fun updateProjectSkuCount(uuid: String,isCreated: Boolean = false) : Int

    @Query("Select * from project where uuid = :uuid")
    fun getProject(uuid: String) : Project

    @Query("select * from sku where uuid = :uuid")
    fun getSku(uuid: String) : Sku

    @Query("select * from sku where project_uuid = :uuid")
    fun getSkuWithProjectUuid(uuid: String) : Sku

    @Query("UPDATE project SET imagesCount = imagesCount + 1, thumbnail= :thumbnail WHERE uuid =:uuid ")
    fun updateProjectThumbnail(uuid: String,thumbnail: String) : Int

    @Query("UPDATE sku SET imagesCount = imagesCount + 1, thumbnail= :thumbnail WHERE uuid =:uuid ")
    fun updateSkuThumbnail(uuid: String,thumbnail: String) : Int

    @Query("UPDATE sku SET imagesCount = imagesCount + 1 WHERE uuid =:uuid ")
    fun updateSkuImageCount(uuid: String) : Int

    @Transaction
    fun saveImage(image: Image){
        val imageId = insertImage(image)
        Log.d(AppConstants.SHOOT_DAO_TAG, "saveImage: $imageId")
        if (image.sequence == 1){
            val thumbUpdate = updateProjectThumbnail(image.projectUuid!!,image.path)
            val skuThumbUpdate = updateSkuThumbnail(image.skuUuid!!,image.path)
            Log.d(AppConstants.SHOOT_DAO_TAG, "saveImage: $thumbUpdate")
            Log.d(AppConstants.SHOOT_DAO_TAG, "saveImage: $skuThumbUpdate")
        }
        else{
            val updateProjectImagesCount = updateProjectImageCount(image.projectUuid!!)
            Log.d(AppConstants.SHOOT_DAO_TAG, "saveImage: $updateProjectImagesCount")

            val skuImagesUpdate = updateSkuImageCount(image.skuUuid!!)
            Log.d(AppConstants.SHOOT_DAO_TAG, "saveImage: $skuImagesUpdate")
        }
    }

    @Query("Select * from exteriortags")
    fun getExteriorTags(): List<NewSubCatResponse.Tags.ExteriorTags>

    @Query("Select * from interiortags")
    fun getInteriorTags(): List<NewSubCatResponse.Tags.InteriorTags>

    @Query("Select * from focusshoot")
    fun getFocusTags(): List<NewSubCatResponse.Tags.FocusShoot>

    @Transaction
    fun saveProjectData(projectList:ArrayList<Project>,
                        skuList: ArrayList<Sku>,
                        imageList: ArrayList<Image>){

        projects()
        skus()
        images()
        val a = insertProjects(projectList)
        val b = insertSkus(skuList)
        val c = insertImages(imageList)
        val s = ""

        Log.d(AppConstants.SHOOT_DAO_TAG, "saveProjectData: $a$b$c")
    }

    @Insert
    fun insertProjects(projects: ArrayList<Project>) : List<Long>

    @Insert
    fun insertSkus(skus: ArrayList<Sku>) : List<Long>

    @Insert
    fun insertImages(images: ArrayList<Image>) : List<Long>

    @Query("DELETE FROM project")
    fun projects()

    @Query("DELETE FROM sku")
    fun skus()

    @Query("DELETE FROM image")
    fun images()

    @Transaction
    fun updateBackground(map: HashMap<String,Any>){
        val p  = updateProjectStatus(
            map["project_uuid"].toString())

        Log.d(AppConstants.SHOOT_DAO_TAG, "updateBackground: $p")
        val s = updateSkuBackground(
            map["skuUuid"].toString(),
            map["bg_name"].toString(),
            map["bg_id"].toString()
        )

        Log.d(AppConstants.SHOOT_DAO_TAG, "updateBackground: $s")
    }

    @Query("UPDATE project SET status = 'ongoing' WHERE uuid =:uuid ")
    fun updateProjectStatus(uuid: String) : Int

    @Query("UPDATE sku SET background_name = :backgroundName, background_id= :backgroundId WHERE uuid =:uuid ")
    fun updateSkuBackground(uuid: String,backgroundName: String,backgroundId: String) : Int

    @Query("Select path from image where skuUuid = :skuUuid")
    fun getImagesPathBySkuId(skuUuid : String) : List<String>

//    @Query("select * from project left join sku on project.uuid=sku.project_uuid where sku.isCreated = :skuIsCreated and project.isCreated = :isCreated  and project.toProcessAt <= :currentTime LIMIT :limit")
//    fun getProjectWithSkus(isCreated: Boolean = false,skuIsCreated: Boolean = false,
//                           limit: Int = 1,
//                           currentTime: Long = System.currentTimeMillis()) : ProjectWithSku

    @Query("Select * from project where isCreated = :isCreated  and toProcessAt <= :currentTime LIMIT :limit")
    fun getProjectWithSkus(isCreated: Boolean = false, currentTime: Long = System.currentTimeMillis(),limit: Int = 1) : ProjectWithSku

    @Query("update project set toProcessAt = :toProcessAt, retryCount = retryCount + 1 where uuid = :uuid ")
    fun skipProject(uuid: String,toProcessAt: Long) : Int

    @Query("Select * from sku where is_processed = :isProcessed and isCreated = :isCreated and background_id != 'DEFAULT_BG_ID' and toProcessAt <= :currentTime LIMIT :limit")
    fun getProcessAbleSku(isProcessed: Boolean = false, isCreated: Boolean = true, currentTime: Long = System.currentTimeMillis(),limit: Int = 1) : Sku


    @Query("update sku set toProcessAt = :toProcessAt, retryCount = retryCount + 1 where uuid = :uuid ")
    fun skipSku(uuid: String,toProcessAt: Long) : Int


    @Query("select Count(*) from image where isUploaded = :isUploaded and isMarkedDone = :isMarkedDone")
    fun totalRemainingUpload(isUploaded: Boolean = false,isMarkedDone : Boolean = false) : Int

    @Query("select * from image where isUploaded = :isUploaded or isMarkedDone = :isMarkedDone and toProcessAt <= :currentTime limit 1")
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

    @Query("select * from image where skuUuid = :skuUuid and name = :imageName")
    fun getImage(skuUuid: String,imageName: String) : Image

    @Update
    fun updateImage(image: Image): Int


    @Query("SELECT * FROM sku where project_uuid = :projectUuid LIMIT :limit OFFSET :offset")
    suspend fun getSkusWithLimitAndSkip(offset: Int,projectUuid: String,limit: Int = 50) : List<Sku>

    @Transaction
    suspend fun insertWithCheck(response: ArrayList<Sku>,projectUuid: String){
        val list = ArrayList<Sku>()

        response.forEach {
            if (it.uuid ==  null)
                it.uuid = getUuid()

            it.projectUuid = projectUuid
            it.backgroundId = AppConstants.DEFAULT_BG_ID

            if (it.status == "Done")
                it.status = "completed"

            if (it.status == "In Progress")
                it.status = "ongoing"

            it.status = it.status.lowercase()

            val dbItem = getSkuWithProjectUuid(it.projectUuid!!)

            if (dbItem == null){
                list.add(it)
            }else {
                if (it.imagesCount > dbItem.imagesCount)
                    list.add(it)
            }

        }

        insertAll(list)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(skuList: List<Sku>) : List<Long>


    @Transaction
    suspend fun insertImagesWithCheck(response: ArrayList<Image>,skuUuid: String){
        val list = ArrayList<Image>()

        response.forEach {
            if (it.uuid ==  null){
                it.uuid = getUuid()
            }

            if(it.overlayId == null)
                it.overlayId = "1234"

            it.skuUuid = skuUuid
            it.path = it.input_image_lres_url



            if (it.status == "Done" || it.status == "Yet to Start" || it.status == "Failed"){
                it.isMarkedDone = true
                it.isUploaded = true
            }

            val dbItem = getImage(it.uuid)

            if (dbItem == null){
                list.add(it)
            }

        }

        insertAllImages(list)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllImages(imageList: List<Image>) : List<Long>

}




















