package com.spyneai.draft.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.base.network.Resource
import com.spyneai.orders.data.repository.MyOrdersRepository
import com.spyneai.orders.data.response.GetOngoingSkusResponse
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.processedimages.ui.data.ProcessedRepository
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import kotlinx.coroutines.launch

class DraftViewModel : ViewModel() {
    private val repository = MyOrdersRepository()
    private val processedRepository = ProcessedRepository()
    private val localRepository = ShootLocalRepository()

    private val _draftResponse: MutableLiveData<Resource<GetProjectsResponse>> = MutableLiveData()
    val draftResponse: LiveData<Resource<GetProjectsResponse>>
        get() = _draftResponse

    private val _imagesOfSkuRes: MutableLiveData<Resource<ImagesOfSkuRes>> = MutableLiveData()
    val imagesOfSkuRes: LiveData<Resource<ImagesOfSkuRes>>
        get() = _imagesOfSkuRes

    fun getDrafts(
        tokenId: String
    ) = viewModelScope.launch {
        _draftResponse.value = Resource.Loading
        _draftResponse.value = repository.getProjects(tokenId, "Draft")
    }

    fun getDraftsFromLocal() = localRepository.getDraftProjects()
    fun getSkusByProjectId(projectId : String) = localRepository.getSkusByProjectId(projectId)

    fun getImagesOfSku(
        authKey: String,
        skuId: String
    ) = viewModelScope.launch {
        _imagesOfSkuRes.value = Resource.Loading
        _imagesOfSkuRes.value = processedRepository.getImagesOfSku(authKey, skuId)
    }
}