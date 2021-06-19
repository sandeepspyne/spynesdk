package com.spyneai.dashboard.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.spyneai.dashboard.data.repository.DashboardRepository
import kotlinx.coroutines.launch
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.orders.data.response.CompletedSKUsResponse
import com.spyneai.orders.data.response.GetOngoingSkusResponse

class DashboardViewModel() : ViewModel() {

    private val repository = DashboardRepository()

    private val _categoriesResponse: MutableLiveData<Resource<NewCategoriesResponse>> = MutableLiveData()
    val categoriesResponse: LiveData<Resource<NewCategoriesResponse>>
        get() = _categoriesResponse

    private val _getOngoingSkusResponse: MutableLiveData<Resource<GetOngoingSkusResponse>> = MutableLiveData()
    val getOngoingSkusResponse: LiveData<Resource<GetOngoingSkusResponse>>
        get() = _getOngoingSkusResponse

    private val _completedProjectResponse: MutableLiveData<Resource<CompletedSKUsResponse>> = MutableLiveData()
    val completedProjectResponse: LiveData<Resource<CompletedSKUsResponse>>
        get() = _completedProjectResponse


    fun getCategories(
        tokenId: String
    ) = viewModelScope.launch {
        _categoriesResponse.value = Resource.Loading
        _categoriesResponse.value = repository.getCategories(tokenId)
    }

    fun getOngoingSKUs(
        tokenId: String
    ) = viewModelScope.launch {
        _getOngoingSkusResponse.value = Resource.Loading
        _getOngoingSkusResponse.value = repository.getOngoingSKUs(tokenId)

    }

    fun getCompletedProjects(
        auth_key : String
    ) = viewModelScope.launch {
        _completedProjectResponse.value = Resource.Loading
        _completedProjectResponse.value = repository.getCompletedProjects(auth_key)

    }


}