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
import com.spyneai.shoot.workmanager.UploadImageWorker
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import kotlin.collections.ArrayList

class ShootViewModel : ViewModel() {

    val shootList: MutableLiveData<ArrayList<ShootData>> = MutableLiveData()
    private val repository = ShootRepository()

    fun setShoot(shoot: ArrayList<ShootData>) {
        shootList.value = shoot
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