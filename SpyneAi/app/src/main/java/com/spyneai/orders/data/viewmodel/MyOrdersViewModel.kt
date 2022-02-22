package com.spyneai.orders.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.room.withTransaction
import com.spyneai.BaseApplication
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.ProjectApiClient
import com.spyneai.base.network.Resource
import com.spyneai.base.room.AppDatabase
import com.spyneai.isInternetActive
import com.spyneai.orders.data.paging.PagedRepository
import com.spyneai.orders.data.paging.ProjectPagedRes
import com.spyneai.orders.data.repository.MyOrdersRepository
import com.spyneai.orders.data.response.CompletedSKUsResponse
import com.spyneai.orders.data.response.GetOngoingSkusResponse
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.processedimages.ui.data.ProcessedRepository
import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.sku.Sku
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MyOrdersViewModel : ViewModel() {
    private val repository = MyOrdersRepository()
    private val processedRepository = ProcessedRepository()
    private val appDatabase = AppDatabase.getInstance(BaseApplication.getContext())



    @ExperimentalPagingApi
    fun getAllProjects(status: String): Flow<PagingData<Project>> {
        return PagedRepository(
            ProjectApiClient().getClient(),
            AppDatabase.getInstance(BaseApplication.getContext()),
            status
        ).getSearchResultStream()
            .cachedIn(viewModelScope)
    }


    val position: MutableLiveData<Int> = MutableLiveData()

    private val _getProjectsResponse: MutableLiveData<Resource<GetProjectsResponse>> =
        MutableLiveData()
    val getProjectsResponse: LiveData<Resource<GetProjectsResponse>>
        get() = _getProjectsResponse


    fun getProjects(
        tokenId: String, status: String
    ) = viewModelScope.launch {
        _getProjectsResponse.value = Resource.Loading
        _getProjectsResponse.value = repository.getProjects(tokenId, status)

    }

    private val _getCompletedProjectsResponse: MutableLiveData<Resource<GetProjectsResponse>> =
        MutableLiveData()
    val getCompletedProjectsResponse: LiveData<Resource<GetProjectsResponse>>
        get() = _getCompletedProjectsResponse

    fun getCompletedProjects(
        tokenId: String, status: String
    ) = viewModelScope.launch {
        _getCompletedProjectsResponse.value = Resource.Loading
        _getCompletedProjectsResponse.value = repository.getProjects(tokenId, status)
    }

    private val _imagesOfSkuRes: MutableLiveData<Resource<ImagesOfSkuRes>> = MutableLiveData()
    val imagesOfSkuRes: LiveData<Resource<ImagesOfSkuRes>>
        get() = _imagesOfSkuRes

    fun getImages(skuId: String?, projectUuid: String,skuUuid: String) = viewModelScope.launch {
        _imagesOfSkuRes.value = Resource.Loading

        if (skuId != null && BaseApplication.getContext().isInternetActive()) {
            val response = processedRepository.getImagesOfSku(
                skuId = skuId
            )

            if (response is Resource.Success) {
                appDatabase.withTransaction {
                    val ss = appDatabase.imageDao().insertImagesWithCheck(
                        response.value.data as ArrayList<Image>,
                        projectUuid,
                        skuUuid)

                }

                GlobalScope.launch(Dispatchers.IO) {
                    val response = appDatabase.imageDao().getImagesBySkuId(
                        skuUuid = skuUuid
                    )

                    GlobalScope.launch(Dispatchers.Main) {
                        _imagesOfSkuRes.value = Resource.Success(
                            ImagesOfSkuRes(
                                data = response,
                                message = "done",
                                "",
                                "",
                                200,
                                fromLocal = false
                            )
                        )
                    }
                }
            }else{
                _imagesOfSkuRes.value = response
            }
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                val response = appDatabase.imageDao().getImagesBySkuId(
                    skuUuid = skuUuid
                )

                GlobalScope.launch(Dispatchers.Main) {
                    _imagesOfSkuRes.value = Resource.Success(
                        ImagesOfSkuRes(
                            data = response,
                            message = "done",
                            "",
                            "",
                            200,
                            fromLocal = true
                        )
                    )
                }
            }

        }
    }


}