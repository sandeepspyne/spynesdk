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
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _categoriesResponse: MutableLiveData<Resource<CategoriesResponse>> = MutableLiveData()
    val categoriesResponse: LiveData<Resource<CategoriesResponse>>
        get() = _categoriesResponse


    fun getCategories(
        tokenId: String
    ) = viewModelScope.launch {
        repository.getCategories(tokenId)

        _categoriesResponse.value = repository.getCategories(tokenId)

    }

}