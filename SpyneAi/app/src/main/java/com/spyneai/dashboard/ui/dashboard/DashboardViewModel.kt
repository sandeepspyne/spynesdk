package com.spyneai.dashboard.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.dashboard.data.repository.BaseRepository
import com.spyneai.dashboard.data.repository.DashboardRepository
import com.spyneai.dashboard.network.Resource
import com.spyneai.model.categories.CategoriesResponse
import com.spyneai.model.categories.Data
import com.spyneai.model.projects.CompletedProjectResponse
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class DashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _categoriesResponse: MutableLiveData<Resource<CategoriesResponse>> = MutableLiveData()
    val categoriesResponse: LiveData<Resource<CategoriesResponse>>
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