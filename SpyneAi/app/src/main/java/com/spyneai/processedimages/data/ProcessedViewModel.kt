package com.spyneai.processedimages.ui.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.base.network.Resource
import com.spyneai.orders.data.response.ImagesOfSkuRes
import kotlinx.coroutines.launch

class ProcessedViewModel : ViewModel() {

    private val repository = ProcessedRepository()

    private val _imagesOfSkuRes: MutableLiveData<Resource<ImagesOfSkuRes>> = MutableLiveData()
    val imagesOfSkuRes: LiveData<Resource<ImagesOfSkuRes>>
        get() = _imagesOfSkuRes

    var projectId : String? = null
    var skuId : String? = null
    var skuName : String? = null
    var selectedImageUrl : String? = null

    val reshoot = MutableLiveData<Boolean>()

    fun getImagesOfSku(
        authKey: String,
        skuId: String
    ) = viewModelScope.launch {
        _imagesOfSkuRes.value = Resource.Loading
        _imagesOfSkuRes.value = repository.getImagesOfSku(authKey, skuId)
    }
}