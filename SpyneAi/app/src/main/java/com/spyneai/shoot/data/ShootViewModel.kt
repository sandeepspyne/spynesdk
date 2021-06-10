package com.spyneai.shoot.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.spyneai.base.network.Resource
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.shoot.data.model.CreateProjectRes
import com.spyneai.shoot.data.model.CreateSkuRes
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.data.model.ShootProgress
import com.spyneai.shoot.workmanager.UploadImageWorker
import kotlinx.coroutines.launch
import java.util.ArrayList

class ShootViewModel : ViewModel(){

    private val repository = ShootRepository()

    val shootList: MutableLiveData<ArrayList<ShootData>> = MutableLiveData()

    private val _subCategoriesResponse: MutableLiveData<Resource<NewSubCatResponse>> = MutableLiveData()
    val subCategoriesResponse: LiveData<Resource<NewSubCatResponse>>
        get() = _subCategoriesResponse

    private val _overlaysResponse: MutableLiveData<Resource<OverlaysResponse>> = MutableLiveData()
    val overlaysResponse: LiveData<Resource<OverlaysResponse>>
        get() = _overlaysResponse


    private val _createProjectRes : MutableLiveData<Resource<CreateProjectRes>> = MutableLiveData()
    val createProjectRes: LiveData<Resource<CreateProjectRes>>
        get() = _createProjectRes

    private val _createSkuRes : MutableLiveData<Resource<CreateSkuRes>> = MutableLiveData()
    val createSkuRes: LiveData<Resource<CreateSkuRes>>
        get() = _createSkuRes

    val selectedAngles: MutableLiveData<Int> = MutableLiveData()

    val shootNumber: MutableLiveData<Int> = MutableLiveData()




    fun getSubCategories(
        authKey : String,prodId : String
    ) = viewModelScope.launch {
        _subCategoriesResponse.value = Resource.Loading
        _subCategoriesResponse.value = repository.getSubCategories(authKey, prodId)
    }

    fun getOverlays(authKey: String, prodId: String,
                            prodSubcategoryId : String, frames : String) = viewModelScope.launch {
        _overlaysResponse.value = Resource.Loading
        _overlaysResponse.value = repository.getOverlays(authKey, prodId, prodSubcategoryId, frames)
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


    fun uploadImage(context : Context) {

    }

    fun uploadImageWithWorkManager(
        requireContext: Context,
        shootData: ShootData
    ) {
        val uploadWorkRequest = OneTimeWorkRequest.Builder(UploadImageWorker::class.java)
        val data = Data.Builder()
        data.putString("uri", shootData.uri.toString())
        data.putString("projectId", shootData.project_id)
        data.putString("skuId", shootData.sku_id)
        data.putString("imageCategory", shootData.image_category)
        data.putString("authKey", shootData.auth_key)
        WorkManager.getInstance(requireContext).enqueue(uploadWorkRequest.build())
    }

    fun createProject(authKey: String,projectName : String
                      ,prodCatId : String) = viewModelScope.launch {
        _createProjectRes.value = Resource.Loading
        _createProjectRes.value = repository.createProject(authKey, projectName, prodCatId)
    }

    fun createSku(authKey: String,projectId : String
                  ,prodCatId : String,prodSubCatId : String,
                  skuName : String) = viewModelScope.launch {
        _createSkuRes.value = Resource.Loading
        _createSkuRes.value = repository.createSku(authKey, projectId, prodCatId, prodSubCatId, skuName)
    }

}