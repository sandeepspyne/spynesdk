package com.spyneai.orders.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.base.network.Resource
import com.spyneai.orders.data.repository.MyOrdersRepository
import com.spyneai.orders.data.response.CompletedSKUsResponse
import com.spyneai.orders.data.response.GetImagesOfSkuResponse
import com.spyneai.orders.data.response.GetOngoingSkusResponse
import kotlinx.coroutines.launch

class MyOrdersViewModel : ViewModel() {

    private val repository = MyOrdersRepository()

    private val _CompletedSKUsResponse: MutableLiveData<Resource<CompletedSKUsResponse>> = MutableLiveData()
    val completedSKUsResponse: LiveData<Resource<CompletedSKUsResponse>>
        get() = _CompletedSKUsResponse

    private val _getOngoingSkusResponse: MutableLiveData<Resource<GetOngoingSkusResponse>> = MutableLiveData()
    val getOngoingSkusResponse: LiveData<Resource<GetOngoingSkusResponse>>
        get() = _getOngoingSkusResponse

    private val _getImagesOfSkuResponse: MutableLiveData<Resource<GetImagesOfSkuResponse>> = MutableLiveData()
    val getImagesOfSkuResponse: LiveData<Resource<GetImagesOfSkuResponse>>
        get() = _getImagesOfSkuResponse

    fun getCompletedSKUs(
        tokenId: String
    ) = viewModelScope.launch {
        _CompletedSKUsResponse.value = Resource.Loading
        _CompletedSKUsResponse.value = repository.getCompletedSKUs(tokenId)

    }

    fun getOngoingSKUs(
        tokenId: String
    ) = viewModelScope.launch {
        _getOngoingSkusResponse.value = Resource.Loading
        _getOngoingSkusResponse.value = repository.getOngoingSKUs(tokenId)

    }

    fun getImagesOfSku(
        tokenId: String,
        skuId: String
    ) = viewModelScope.launch {
        _getImagesOfSkuResponse.value = Resource.Loading
        _getImagesOfSkuResponse.value = repository.getImagesOfSku(tokenId, skuId)

    }



}