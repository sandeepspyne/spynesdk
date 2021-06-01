package com.spyneai.dashboard.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.dashboard.data.repository.BaseRepository
import com.spyneai.dashboard.data.repository.DashboardRepository
import com.spyneai.dashboard.network.Resource
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.model.categories.CategoriesResponse
import com.spyneai.model.categories.Data
import com.spyneai.model.credit.FreeCreditEligblityResponse
import com.spyneai.model.projects.CompletedProjectResponse
import com.spyneai.model.shoot.CreateCollectionRequest
import com.spyneai.model.shoot.CreateCollectionResponse
import com.spyneai.model.shoot.UpdateShootCategoryRequest
import com.spyneai.model.shoot.UpdateShootCategoryResponse
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class DashboardViewModel() : ViewModel() {

    private val repository = DashboardRepository()

    private val _categoriesResponse: MutableLiveData<Resource<NewCategoriesResponse>> = MutableLiveData()
    val categoriesResponse: LiveData<Resource<NewCategoriesResponse>>
        get() = _categoriesResponse

    private val _completedProjectResponse: MutableLiveData<Resource<List<CompletedProjectResponse>>> = MutableLiveData()
    val completedProjectResponse: LiveData<Resource<List<CompletedProjectResponse>>>
        get() = _completedProjectResponse

    private val _createCollectionResponse: MutableLiveData<Resource<CreateCollectionResponse>> = MutableLiveData()
    val createCollectionResponse: LiveData<Resource<CreateCollectionResponse>>
        get() = _createCollectionResponse

    private val _updateShootCategoryResponse: MutableLiveData<Resource<UpdateShootCategoryResponse>> = MutableLiveData()
    val updateShootCategoryResponse: LiveData<Resource<UpdateShootCategoryResponse>>
        get() = _updateShootCategoryResponse


    private val _freeCreditEligblityResponse: MutableLiveData<Resource<FreeCreditEligblityResponse>> = MutableLiveData()
    val freeCreditEligblityResponse: LiveData<Resource<FreeCreditEligblityResponse>>
        get() = _freeCreditEligblityResponse



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

    fun createCollection(
        tokenId: String,
        createCollectionRequest: CreateCollectionRequest
    )  = viewModelScope.launch {
        _createCollectionResponse.value = Resource.Loading
        _createCollectionResponse.value = repository.createCollection(tokenId, createCollectionRequest)

    }

    fun updateShootCategory(
        tokenId: String,
        updateShootCategoryRequest: UpdateShootCategoryRequest
    )  = viewModelScope.launch {
        _updateShootCategoryResponse.value = Resource.Loading
        _updateShootCategoryResponse.value = repository.updateShootCategory(tokenId, updateShootCategoryRequest)

    }

    fun userFreeCreditEligiblityCheck(
        user_id: RequestBody?,
        email_id: RequestBody?,
    )  = viewModelScope.launch {
        _freeCreditEligblityResponse.value = Resource.Loading
        _freeCreditEligblityResponse.value = repository.UserFreeCreditEligiblityCheck(user_id, email_id)

    }



}