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
import com.spyneai.orders.data.response.GetProjectsResponse

class DashboardViewModel() : ViewModel() {

    private val repository = DashboardRepository()

    private val _categoriesResponse: MutableLiveData<Resource<NewCategoriesResponse>> = MutableLiveData()
    val categoriesResponse: LiveData<Resource<NewCategoriesResponse>>
        get() = _categoriesResponse

    private val _getOngoingSkusResponse: MutableLiveData<Resource<GetOngoingSkusResponse>> = MutableLiveData()
    val getOngoingSkusResponse: LiveData<Resource<GetOngoingSkusResponse>>
        get() = _getOngoingSkusResponse

    private val _completedSkusResponse: MutableLiveData<Resource<CompletedSKUsResponse>> = MutableLiveData()
    val completedSkusResponse: LiveData<Resource<CompletedSKUsResponse>>
        get() = _completedSkusResponse

    val isNewUser: MutableLiveData<Boolean> = MutableLiveData()
    val creditsMessage: MutableLiveData<String> = MutableLiveData()

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

    fun getCompletedSKUs(
        auth_key : String
    ) = viewModelScope.launch {
        _completedSkusResponse.value = Resource.Loading
        _completedSkusResponse.value = repository.getCompletedProjects(auth_key)

    }

    //ongoing completed

    private val _getProjectsResponse: MutableLiveData<Resource<GetProjectsResponse>> = MutableLiveData()
    val getProjectsResponse: LiveData<Resource<GetProjectsResponse>>
        get() = _getProjectsResponse

    fun getProjects(
        tokenId: String, status: String
    ) = viewModelScope.launch {
        _getProjectsResponse.value = Resource.Loading
        _getProjectsResponse.value = repository.getProjects(tokenId, status)

    }

    private val _getCompletedProjectsResponse: MutableLiveData<Resource<GetProjectsResponse>> = MutableLiveData()
    val getCompletedProjectsResponse: LiveData<Resource<GetProjectsResponse>>
        get() = _getCompletedProjectsResponse

    fun getCompletedProjects(
        tokenId: String, status: String
    ) = viewModelScope.launch {
        _getCompletedProjectsResponse.value = Resource.Loading
        _getCompletedProjectsResponse.value = repository.getProjects(tokenId, status)

    }


}