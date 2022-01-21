package com.spyneai.shoot.repository.db

import android.util.Log
import androidx.room.*
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.getTimeStamp
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Query("update sku set isCreated = :isCreated where uuid = :uuid")
    fun updateSkuCreated(uuid: String,isCreated: Boolean = true): Int

    @Query("update project set isCreated = :isCreated where uuid = :uuid")
    fun updateProjectCreated(uuid: String,isCreated: Boolean = true): Int

    @Transaction
    fun updateProjectAndSkuCreated(projectUuid: String,skuUuid: String){
        val p = updateProjectCreated(projectUuid)
        Log.d(AppConstants.SHOOT_DAO_TAG, "updateProjectAndSkuCreated: $p")

        val s = updateSkuCreated(skuUuid)

        Log.d(AppConstants.SHOOT_DAO_TAG, "updateProjectAndSkuCreated: $s")
    }

    @Query("update sku set skuId = :skuId,projectId = :projectId, isCreated = :isCreated where uuid = :uuid")
    fun updateSKuServerId(uuid: String,skuId: String,projectId: String,isCreated: Boolean = true): Int

    @Query("update image set skuId = :skuId, projectId = :projectId where skuUuid = :skuUuid")
    fun updateImageIds(skuUuid: String,skuId: String,projectId: String): Int

    @Query("update videodetails set projectId =:projectId, skuId = :skuId where skuUuid = :skuUuid ")
    fun updateVideoSkuAndProjectIds(projectId: String,skuId: String,skuUuid: String) : Int

    @Transaction
    fun updateSkuAndImageIds(projectId: String,skuUuid: String,skuId: String){
        updateSKuServerId(skuUuid,skuId,projectId)
        updateImageIds(skuUuid,skuId,projectId)
    }

    @Update
    fun updateSku(sku: Sku): Int

    @Transaction
    fun updateSubcategory(project: Project,sku: Sku){
        val projectUpdate = updateProjectSubcategory(project.uuid,project.subCategoryName,project.subCategoryId)
        Log.d(AppConstants.SHOOT_DAO_TAG, "updateSubcategory: $projectUpdate")
        val skuUpdate = updateSku(sku)
        Log.d(AppConstants.SHOOT_DAO_TAG, "updateSubcategory: $skuUpdate")
    }

    @Query("update project set subCategoryName = :subcategoryName, subCategoryId = :subcategoryId where uuid = :uuid ")
    fun updateProjectSubcategory(uuid: String,subcategoryName: String?,subcategoryId: String?) : Int


    @Insert
    fun insertImage(obj: Image) : Long

    @Query("UPDATE project SET imagesCount = imagesCount + 1 WHERE uuid =:uuid ")
    fun updateProjectImageCount(uuid: String) : Int


    @Query("UPDATE project SET isCreated = :isCreated WHERE uuid =:uuid ")
    fun setProjectCreatedFalse(uuid: String,isCreated: Boolean = false) : Int


    @Query("UPDATE project SET imagesCount = imagesCount + 1, thumbnail= :thumbnail WHERE uuid =:uuid ")
    fun updateProjectThumbnail(uuid: String,thumbnail: String) : Int

    @Query("UPDATE sku SET imagesCount = imagesCount + 1, thumbnail= :thumbnail WHERE uuid =:uuid ")
    fun updateSkuThumbnail(uuid: String,thumbnail: String) : Int

    @Query("UPDATE sku SET imagesCount = imagesCount + 1 WHERE uuid =:uuid ")
    fun updateSkuImageCount(uuid: String) : Int

    @Query("select * from sku where uuid = :uuid")
    fun getSku(uuid: String) : Sku

    @Transaction
    fun saveImage(image: Image){
        val sku = getSku(image.skuUuid.toString())

        image.apply {
            projectId = sku.projectId
            skuId = sku.skuId
        }

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
    fun updateBackground(map: HashMap<String,Any>,list: List<Sku>){
        val p  = updateProjectStatus(
            map["project_uuid"].toString())

        Log.d(AppConstants.SHOOT_DAO_TAG, "updateBackground: $p")

        list.forEach {
            val s = updateSkuBackground(
                it.uuid,
                map["bg_name"].toString(),
                map["bg_id"].toString(),
                if (it.imagePresent == 1 && it.videoPresent == 1) it?.threeSixtyFrames?.plus(it?.imagesCount!!)!! else it?.imagesCount!!
            )

            Log.d(AppConstants.SHOOT_DAO_TAG, "updateBackground: $s")
        }

    }

    @Query("UPDATE project SET status = 'ongoing' WHERE uuid =:uuid ")
    fun updateProjectStatus(uuid: String) : Int

    @Query("UPDATE sku SET backgroundName = :backgroundName, backgroundId= :backgroundId, totalFrames = :totalFrames  WHERE uuid =:uuid ")
    fun updateSkuBackground(uuid: String,backgroundName: String,backgroundId: String,totalFrames : Int) : Int

    @Transaction
    fun updateComboSkuAndProject(sku: Sku){
        setProjectCreatedFalse(sku.projectUuid!!)

        updateVideoSkuLocally(
            uuid = sku.uuid,
            subcategoryId = sku.subcategoryId!!,
            subcategoryName = sku.subcategoryName!!,
            initialFrames = sku.initialFrames!!
        )
    }

    @Query("UPDATE sku SET subcategoryId = :subcategoryId,subcategoryName = :subcategoryName,initialFrames = :initialFrames, imagePresent = :imagePresent,isCreated = :isCreated WHERE uuid =:uuid ")
    fun updateVideoSkuLocally(uuid: String,subcategoryId: String,subcategoryName: String,initialFrames: Int,imagePresent: Int = 1,isCreated: Boolean = false) : Int

    @Query("UPDATE sku SET totalFrames = totalFrames + :totalFrames  WHERE uuid =:uuid ")
    fun updateSkuTotalFrames(uuid: String,totalFrames : Int) : Int


//    @Query("select * from project left join sku on project.uuid=sku.projectUuid where sku.isCreated = :skuIsCreated and project.isCreated = :isCreated  and project.toProcessAt <= :currentTime LIMIT :limit")
//    fun getProjectWithSkus(isCreated: Boolean = false,skuIsCreated: Boolean = false,
//                           limit: Int = 1,
//                           currentTime: Long = System.currentTimeMillis()) : ProjectWithSku








}




















