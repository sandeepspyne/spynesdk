package com.spyneai.orders.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.orders.data.repository.OnGoingRepository
import com.spyneai.orders.data.response.ProjectCountResponse
import kotlinx.coroutines.launch

class MyOrdersViewModel  : ViewModel() {

    private val repository = OnGoingRepository()

    private val _projectCount : MutableLiveData<ProjectCountResponse> = MutableLiveData()

    val projectCount: LiveData<ProjectCountResponse>
        get() = _projectCount


    fun getProjectsCount(
        tokenId : String
    ) = viewModelScope.launch {
        _projectCount.value = repository.getProjectCount(tokenId)
    }
}