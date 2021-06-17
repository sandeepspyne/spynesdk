package com.spyneai.dashboard.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.spyneai.dashboard.data.repository.DashboardRepository
import com.spyneai.model.categories.CategoriesResponse
import com.spyneai.model.credit.FreeCreditEligblityResponse
import com.spyneai.model.projects.CompletedProjectResponse
import com.spyneai.model.shoot.CreateCollectionRequest
import com.spyneai.model.shoot.CreateCollectionResponse
import com.spyneai.model.shoot.UpdateShootCategoryRequest
import com.spyneai.model.shoot.UpdateShootCategoryResponse
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.response.NewCategoriesResponse

class DashboardViewModel() : ViewModel() {

    private val repository = DashboardRepository()

    private val _categoriesResponse: MutableLiveData<Resource<NewCategoriesResponse>> = MutableLiveData()
    val categoriesResponse: LiveData<Resource<NewCategoriesResponse>>
        get() = _categoriesResponse

    private val _completedProjectResponse: MutableLiveData<Resource<List<CompletedProjectResponse>>> = MutableLiveData()
    val completedProjectResponse: LiveData<Resource<List<CompletedProjectResponse>>>
        get() = _completedProjectResponse


    fun getCategories(
        tokenId: String
    ) = viewModelScope.launch {
        _categoriesResponse.value = Resource.Loading
        _categoriesResponse.value = repository.getCategories(tokenId)

    }

    fun getCompletedProjects(
        user_id: RequestBody
    ) = viewModelScope.launch {
        _completedProjectResponse.value = Resource.Loading
        _completedProjectResponse.value = repository.getCompletedProjects(user_id)

    }


}