package com.spyneai.shoot.repository.db

import android.util.Log
import androidx.room.*
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.model.CarsBackgroundRes

import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.project.ProjectWithSku
import com.spyneai.shoot.repository.model.sku.Sku



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

    @Update
    fun updateSku(sku: Sku): Int

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

    @Query("select * from image where sku_uuid = :skuUuid")
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
        updateProjectStatus(
            map["project_uuid"].toString())

        updateSkuBackground(
            map["sku_uuid"].toString(),
            map["bg_name"].toString(),
            map["bg_id"].toString()
        )
    }

    @Query("UPDATE project SET status = 'ongoing' WHERE uuid =:uuid ")
    fun updateProjectStatus(uuid: String) : Int

    @Query("UPDATE sku SET background_name = :backgroundName, background_id= :backgroundId WHERE uuid =:uuid ")
    fun updateSkuBackground(uuid: String,backgroundName: String,backgroundId: String) : Int

    @Query("Select path from image where sku_uuid = :skuUuid")
    fun getImagesPathBySkuId(skuUuid : String) : List<String>

//    @Query("select * from project left join sku on project.uuid=sku.project_uuid where sku.isCreated = :skuIsCreated and project.isCreated = :isCreated  and project.toProcessAt <= :currentTime LIMIT :limit")
//    fun getProjectWithSkus(isCreated: Boolean = false,skuIsCreated: Boolean = false,
//                           limit: Int = 1,
//                           currentTime: Long = System.currentTimeMillis()) : ProjectWithSku

    @Query("Select * from project where isCreated = :isCreated  and toProcessAt <= :currentTime LIMIT :limit")
    fun getProjectWithSkus(isCreated: Boolean = false, currentTime: Long = System.currentTimeMillis(),limit: Int = 1) : ProjectWithSku

}




















