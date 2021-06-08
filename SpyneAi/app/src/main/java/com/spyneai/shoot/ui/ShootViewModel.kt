package com.spyneai.shoot.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.data.repository.DashboardRepository
import com.spyneai.model.credit.FreeCreditEligblityResponse
import com.spyneai.shoot.data.model.UploadImageResponse
import com.spyneai.shoot.data.repository.ShootRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ShootViewModel : ViewModel() {

    private val repository = ShootRepository()

    private val _uploadImageResponse: MutableLiveData<Resource<UploadImageResponse>> = MutableLiveData()
    val uploadImageResponse: LiveData<Resource<UploadImageResponse>>
    get() = _uploadImageResponse

    fun uploadImage(
        project_id: RequestBody,
        sku_id: RequestBody,
        image_category: RequestBody,
        auth_key: RequestBody,
        image: MultipartBody.Part
    ) = viewModelScope.launch {
        _uploadImageResponse.value = Resource.Loading
        _uploadImageResponse.value = repository.uploadImage(project_id, sku_id, image_category, auth_key, image)
    }

}