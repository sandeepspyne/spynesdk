package com.spyneai.shoot.data

import android.content.ContentValues
import android.provider.BaseColumns
import android.util.Log
import androidx.room.Room
import com.spyneai.BaseApplication
import com.spyneai.base.room.AppDatabase
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.db.DBHelper
import com.spyneai.db.Images
import com.spyneai.db.Projects
import com.spyneai.db.ShootContract
import com.spyneai.shoot.data.model.CarsBackgroundRes
import com.spyneai.shoot.data.model.Image
import com.spyneai.shoot.data.model.Project
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.repository.db.ShootDao
import com.spyneai.shoot.repository.model.image.ImageDao
import com.spyneai.shoot.repository.model.project.ProjectDao
import com.spyneai.shoot.repository.model.sku.SkuDao

class ShootLocalRepository(
    val shootDao: ShootDao,
    val projectDao: ProjectDao,
    val skuDao: SkuDao,
    val imageDao: ImageDao? = null
) {

    private val TAG = "ShootLocalRepository"


    fun insertProject(project: com.spyneai.shoot.repository.model.project.Project): Long {
        return projectDao.insertProject(project)
    }

    suspend fun insertSku(
        sku: com.spyneai.shoot.repository.model.sku.Sku,
        project: com.spyneai.shoot.repository.model.project.Project
    ) = skuDao.saveSku(sku, project)


    fun getDraftProjects() = projectDao.getDraftProjects()

    fun getSubcategories(): List<NewSubCatResponse.Subcategory> {
        return shootDao.getSubcategories()
    }

    fun insertSubCategories(
        data: List<NewSubCatResponse.Subcategory>,
        interior: List<NewSubCatResponse.Interior>,
        misc: List<NewSubCatResponse.Miscellaneous>,
        exteriorTagsTags: List<NewSubCatResponse.Tags.ExteriorTags>,
        interiorTags: List<NewSubCatResponse.Tags.InteriorTags>,
        focusTags: List<NewSubCatResponse.Tags.FocusShoot>
    ) {
        shootDao.saveSubcategoriesData(
            data,
            interior,
            misc,
            exteriorTagsTags,
            interiorTags,
            focusTags
        )
    }

    fun getInteriorList(subcatId: String) = shootDao.getInterior(subcatId)

    fun getMiscList(subcatId: String) = shootDao.getMisc(subcatId)

    fun insertOverlays(overlays: List<OverlaysResponse.Overlays>) =
        shootDao.insertOverlays(overlays)

    fun getOverlays(prodSubcategoryId: String, frames: String) =
        shootDao.getOverlays(prodSubcategoryId, frames.toInt())

    fun insertBackgrounds(backgrounds: List<CarsBackgroundRes.Background>) =
        shootDao.insertBackgrounds(backgrounds)

    fun getBackgrounds(category: String) = shootDao.getBackgrounds(category)

    fun getExteriorTags() = shootDao.getExteriorTags()

    fun getInteriorTags() = shootDao.getInteriorTags()

    fun getFocusTags() = shootDao.getFocusTags()

    fun updateSubcategory(
        project: com.spyneai.shoot.repository.model.project.Project,
        sku: com.spyneai.shoot.repository.model.sku.Sku
    ) = shootDao.updateSubcategory(project, sku)

    fun updateSkuExteriorAngles(sku: com.spyneai.shoot.repository.model.sku.Sku) {
        sku.isSelectAble = true
        shootDao.updateSku(sku)
    }

    fun getSkusByProjectId(uuid: String) = skuDao.getSkusByProjectId(uuid)

    fun getProject(uuid: String) = projectDao.getProject(uuid)

    fun getSkuById(uuid: String) = shootDao.getSku(uuid)

    fun updateBackground(map: HashMap<String, Any>) {
        val list = skuDao.getDraftSkusByProjectId(map["project_uuid"].toString())

        shootDao.updateBackground(map,list)
    }

    fun updateProjectToOngoing(projectUuid: String) = shootDao.updateProjectStatus(projectUuid)

    fun insertImage(image: com.spyneai.shoot.repository.model.image.Image) {
        return shootDao.saveImage(image)
    }

    fun updateSkuTotalFrames(uuid: String,totalFrames: Int) = shootDao.updateSkuTotalFrames(uuid,totalFrames)

    fun updateVideoSkuLocally(sku: com.spyneai.shoot.repository.model.sku.Sku) = shootDao.updateComboSkuAndProject(sku)

    fun getExteriorImages(uuid: String) = imageDao?.getExteriorImages(uuid)
}
