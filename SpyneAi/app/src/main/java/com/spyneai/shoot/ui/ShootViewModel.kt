package com.spyneai.shoot.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.data.model.ShootProgress
import com.spyneai.shoot.data.model.UploadImageResponse
import com.spyneai.shoot.data.repository.ShootRepository
import com.spyneai.shoot.data.room.entities.ShootEntity
import com.spyneai.shoot.workmanager.UploadImageWorker
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import kotlin.collections.ArrayList

class ShootViewModel : ViewModel() {
    private val repository = ShootRepository()
    val shootList: MutableLiveData<ArrayList<ShootData>> = MutableLiveData()
    val shootData: LiveData<ShootEntity>? = null
    private val _subCategoriesResponse: MutableLiveData<Resource<NewSubCatResponse>> =
        MutableLiveData()
    val subCategoriesResponse: LiveData<Resource<NewSubCatResponse>>
        get() = _subCategoriesResponse

    private val _uploadImageResonse: MutableLiveData<Resource<UploadImageResponse>> =
        MutableLiveData()
    val uploadImageResonse: LiveData<Resource<UploadImageResponse>>
        get() = _uploadImageResonse

    val selectedAngles: MutableLiveData<Int> = MutableLiveData()

    val shootNumber: MutableLiveData<Int> = MutableLiveData()

    fun setShoot(shoot: ArrayList<ShootData>) {
        shootList.value = shoot
    }

    fun insertShootData(context: Context, shootEntity: ShootEntity){
        repository.insertShootData(context, shootEntity)

    }

    fun getShootData(context: Context, sku_id: String): LiveData<ShootEntity>? {
        repository.getShootData(context, sku_id)
        return shootData
    }

    fun uploadImageWithWorkManager(
        requireContext: Context,
        shootData: ShootData
    ) {
        val uploadWorkRequest = OneTimeWorkRequest.Builder(UploadImageWorker::class.java)
        val data = Data.Builder()
        data.putString("capturedImage", shootData.capturedImage)
        data.putString("projectId", shootData.project_id)
        data.putString("skuId", shootData.sku_id)
        data.putString("imageCategory", shootData.image_category)
        data.putString("authKey", shootData.auth_key)
        uploadWorkRequest.setInputData(data.build())
        WorkManager.getInstance(requireContext).enqueue(uploadWorkRequest.build())
    }


    fun getSubCategories(
        authKey: String, prodId: String
    ) = viewModelScope.launch {
        _subCategoriesResponse.value = Resource.Loading
        _subCategoriesResponse.value = repository.getSubCategories(authKey, prodId)
    }

    fun uploadImage(
        project_id: RequestBody,
        sku_id: RequestBody,
        image_category: RequestBody,
        auth_key: RequestBody,
        image: MultipartBody.Part
    ) = viewModelScope.launch {
        _uploadImageResonse.value = Resource.Loading
        _uploadImageResonse.value = repository.uploadImage(project_id, sku_id, image_category, auth_key, image)
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