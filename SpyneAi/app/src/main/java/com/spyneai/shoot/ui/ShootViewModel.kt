package com.spyneai.shoot.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.shoot.data.model.ShootProgress
import com.spyneai.shoot.data.model.UploadImageResponse
import com.spyneai.shoot.data.repository.ShootRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.*

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

    private val _subCategoriesResponse: MutableLiveData<Resource<NewSubCatResponse>> = MutableLiveData()
    val subCategoriesResponse: LiveData<Resource<NewSubCatResponse>>
        get() = _subCategoriesResponse

    val selectedAngles: MutableLiveData<Int> = MutableLiveData()

    val shootNumber: MutableLiveData<Int> = MutableLiveData()


    fun getSubCategories(
        authKey : String,prodId : String
    ) = viewModelScope.launch {
        _subCategoriesResponse.value = Resource.Loading
        _subCategoriesResponse.value = repository.getSubCategories(authKey, prodId)
    }

    fun getSelectedAngles() = selectedAngles.value

    fun getShootNumber() = shootNumber.value

    fun getShootProgressList(): ArrayList<ShootProgress> {
        val shootProgressList = ArrayList<ShootProgress>()
        shootProgressList.add(ShootProgress(true))

        for (i in 1 until selectedAngles.value!!)
            shootProgressList.add(ShootProgress(false))

        return shootProgressList
    }

}