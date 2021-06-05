package com.spyneai.shoot.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.dashboard.data.repository.DashboardRepository
import com.spyneai.dashboard.network.Resource
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import kotlinx.coroutines.launch

class ShootViewModel : ViewModel(){

    private val repository = ShootRepository()

    private val _subCategoriesResponse: MutableLiveData<Resource<NewSubCatResponse>> = MutableLiveData()
    val subCategoriesResponse: LiveData<Resource<NewSubCatResponse>>
        get() = _subCategoriesResponse

    public val _angles: MutableLiveData<Int> = MutableLiveData()
    val angles: LiveData<Int>
        get() = _angles

    fun getSubCategories(
        authKey : String,prodId : String
    ) = viewModelScope.launch {
        _subCategoriesResponse.value = Resource.Loading
        _subCategoriesResponse.value = repository.getSubCategories(authKey, prodId)

    }
}