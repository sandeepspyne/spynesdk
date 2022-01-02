package com.spyneai.shoot.repository.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.shoot.data.model.CarsBackgroundRes

import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.sku.Sku

@Dao
interface ShootDao {

    @Insert
    fun subcategories(list: List<NewSubCatResponse.Subcategory>)

    @Insert
    fun insertInterior(list: List<NewSubCatResponse.Interior>) : List<Long>

    @Insert
    fun insertMisc(list: List<NewSubCatResponse.Miscellaneous>)

    @Insert
    fun insertExteriorTags(list: List<NewSubCatResponse.Tags.ExteriorTags>) : List<Long>

    @Insert
    fun insertInteriorTags(list: List<NewSubCatResponse.Tags.InteriorTags>)

    @Insert
    fun insertFocusTags(list: List<NewSubCatResponse.Tags.FocusShoot>)

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

        val s = insertExteriorTags(exteriorTagsTags)
        insertInteriorTags(interiorTags)
        insertFocusTags(focusTags)
    }

    @Query("SELECT * FROM subcategory")
    fun getSubcategories(): List<NewSubCatResponse.Subcategory>

    @Query("SELECT * FROM interior where prodCatId = :prodCatId")
    fun getInterior(prodCatId: String) : List<NewSubCatResponse.Interior>

    @Query("SELECT * FROM miscellaneous where prod_cat_id = :prodCatId")
    fun getMisc(prodCatId: String) : List<NewSubCatResponse.Miscellaneous>

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

    @Query("SELECT * FROM project where status = 'draft'")
    fun getDraftProjects(): List<Project>

    @Insert
    fun insertSku(obj: Sku) : Long

    @Insert
    fun insertImage(obj: Image) : Long

    @Query("Select * from exteriortags")
    fun getExteriorTags(): List<NewSubCatResponse.Tags.ExteriorTags>

    @Query("Select * from interiortags")
    fun getInteriorTags(): List<NewSubCatResponse.Tags.InteriorTags>

    @Query("Select * from focusshoot")
    fun getFocusTags(): List<NewSubCatResponse.Tags.FocusShoot>
}