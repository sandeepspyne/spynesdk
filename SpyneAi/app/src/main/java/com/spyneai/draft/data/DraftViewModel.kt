package com.spyneai.draft.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.room.withTransaction
import com.google.gson.Gson
import com.spyneai.BaseApplication
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.ProjectApiClient
import com.spyneai.base.network.Resource
import com.spyneai.base.room.AppDatabase
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.getUuid
import com.spyneai.isInternetActive
import com.spyneai.orders.data.repository.MyOrdersRepository
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.processedimages.ui.data.ProcessedRepository
import com.spyneai.shoot.data.ImageLocalRepository
import com.spyneai.shoot.data.ImagesRepoV2
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.sku.Sku
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DraftViewModel : ViewModel() {
    private val repository = MyOrdersRepository()
    private val shootRepository = ShootRepository()
    private val processedRepository = ProcessedRepository()
    private val appDatabase = AppDatabase.getInstance(BaseApplication.getContext())
    private val localRepository = ShootLocalRepository(appDatabase.shootDao())
    private val imageRepositoryV2 = ImagesRepoV2(appDatabase.shootDao())


    private val _draftResponse: MutableLiveData<Resource<GetProjectsResponse>> = MutableLiveData()
    val draftResponse: LiveData<Resource<GetProjectsResponse>>
        get() = _draftResponse

    private val _imagesOfSkuRes: MutableLiveData<Resource<ImagesOfSkuRes>> = MutableLiveData()
    val imagesOfSkuRes: LiveData<Resource<ImagesOfSkuRes>>
        get() = _imagesOfSkuRes

    @ExperimentalPagingApi
    fun getSkus(projectId: String?, projectUuid: String): Flow<PagingData<Sku>> {
        return SkuRepository(
            ProjectApiClient().getClient(),
            AppDatabase.getInstance(BaseApplication.getContext()),
            projectId,
            projectUuid
        ).getSearchResultStream()
            .cachedIn(viewModelScope)
    }


    fun getImages(skuId: String?, skuUuid: String) = viewModelScope.launch {
        _imagesOfSkuRes.value = Resource.Loading

        if (skuId != null && BaseApplication.getContext().isInternetActive()) {
            val response = processedRepository.getImagesOfSku(
                skuId = skuId
            )

            if (response is Resource.Success) {
                appDatabase.withTransaction {
                    val ss = appDatabase.shootDao().insertImagesWithCheck(
                        response.value.data as ArrayList<Image>,
                        skuUuid)

                }

                GlobalScope.launch(Dispatchers.IO) {
                    val response = appDatabase.shootDao().getImagesBySkuId(
                        skuUuid = skuUuid
                    )

                    GlobalScope.launch(Dispatchers.Main) {
                        _imagesOfSkuRes.value = Resource.Success(
                            ImagesOfSkuRes(
                                data = response,
                                message = "done",
                                "",
                                "",
                                200
                            ))
                    }
                }
            }else{
                _imagesOfSkuRes.value = response
            }
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                val response = appDatabase.shootDao().getImagesBySkuId(
                    skuUuid = skuUuid
                )

                GlobalScope.launch(Dispatchers.Main) {
                    _imagesOfSkuRes.value = Resource.Success(
                        ImagesOfSkuRes(
                            data = response,
                            message = "done",
                            "",
                            "",
                            200
                        ))
                }
            }

        }
    }

    fun getDrafts(
        tokenId: String
    ) = viewModelScope.launch {
        _draftResponse.value = Resource.Loading
        val response = repository.getDrafts(tokenId, "Draft")

        if (response is Resource.Success) {
            //insert projects sku's and images to db
            val projectList = ArrayList<Project>()
            val skuList = ArrayList<Sku>()
            val imageList = ArrayList<Image>()

            val looplist = ArrayList<GetProjectsResponse.Project_data>().apply {
                add(response.value.data.project_data[0])
                add(response.value.data.project_data[1])
                add(response.value.data.project_data[2])
                add(response.value.data.project_data[3])
                add(response.value.data.project_data[4])
                add(response.value.data.project_data[5])
                add(response.value.data.project_data[6])
            }

            GlobalScope.launch(Dispatchers.IO) {
                looplist.forEach {
                    val project = Project(
                        uuid = getUuid(),
                        categoryId = it.categoryId,
                        categoryName = it.category,
                        subCategoryName = it.sub_category,
                        subCategoryId = it.subCategoryId,
                        projectName = it.project_name,
                        projectId = it.project_id,
                        status = it.status.lowercase(),
                        skuCount = it.total_sku,
                        imagesCount = it.total_images,
                        //thumbnail = it.sku[0].images[0].input_lres
                    )
                    projectList.add(project)

                    it.sku.forEach { sku ->
                        val newSku = Sku(
                            uuid = getUuid(),
                            categoryId = sku.categoryId,
                            categoryName = sku.category,
                            subcategoryName = sku.subCategory,
                            subcategoryId = sku.subCategoryId,
                            skuId = sku.sku_id,
                            skuName = sku.sku_name,
                            projectUuid = project.uuid,
                            projectId = project.projectId,
                            initialFrames = sku.exteriorClicks,
                            totalFrames = sku.total_frames_no,
                            isThreeSixty = sku.is360,
                            isPaid = sku.paid,
                            threeSixtyFrames = sku.exteriorClicks,
                            status = it.status.lowercase(),
                            imagesCount = sku.total_images
                        )
                        skuList.add(newSku)

                        sku.images.forEach { images ->
                            val image = Image(
                                uuid = getUuid(),
                                projectUuid = project.uuid,
                                projectId = project.projectId,
                                skuName = newSku.skuName,
                                skuUuid = newSku.uuid,
                                skuId = newSku.skuId,
                                path = if (images.input_lres.isNullOrEmpty()) images.input_hres else images.input_lres,
                                isUploaded = true,
                                isMarkedDone = true,
                                name = "",
                                sequence = 0,
                                overlayId = "1234",
                                angle = 0,
                                isReshoot = false,
                                isReclick = false,
                                image_category = "Exterior"
                            )
                            imageList.add(image)
                        }

                    }
                }
                localRepository.saveProjectData(
                    projectList,
                    skuList,
                    imageList
                )

                GlobalScope.launch(Dispatchers.Main) {
                    _draftResponse.value = response
                }
            }
        } else {
            _draftResponse.value = response
        }
    }

    suspend fun getDraftsFromLocal() = localRepository.getDraftProjects()

    suspend fun getSkusByProjectId(projectId: String) =
        localRepository.getSkusByProjectId(projectId)

    suspend fun getImagesbySkuId(skuId: String) = imageRepositoryV2.getImagesBySkuId(skuId)

    fun getImagesOfSku(
        skuId: String
    ) = viewModelScope.launch {
        _imagesOfSkuRes.value = Resource.Loading
        _imagesOfSkuRes.value = processedRepository.getImagesOfSku(skuId)
    }

    private val _subCategoriesResponse: MutableLiveData<Resource<NewSubCatResponse>> =
        MutableLiveData()
    val subCategoriesResponse: LiveData<Resource<NewSubCatResponse>>
        get() = _subCategoriesResponse

    fun getSubCategories(
        authKey: String, prodId: String
    ) = viewModelScope.launch {
        _subCategoriesResponse.value = Resource.Loading
        _subCategoriesResponse.value = shootRepository.getSubCategories(authKey, prodId)
    }
}