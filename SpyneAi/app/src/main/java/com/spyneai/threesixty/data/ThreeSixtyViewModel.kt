package com.spyneai.threesixty.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.CategoryDetails
import com.spyneai.shoot.data.model.CreateProjectRes
import com.spyneai.shoot.data.model.CreateSkuRes
import com.spyneai.threesixty.data.model.VideoDetails
import com.spyneai.threesixty.data.response.ProcessThreeSixtyRes
import kotlinx.coroutines.launch

class ThreeSixtyViewModel : ViewModel() {

    private val repository = ShootRepository()
    private val threeSixtyRepository = ThreeSixtyRepository()

    val isDemoClicked: MutableLiveData<Boolean> = MutableLiveData()
    val title : MutableLiveData<String> = MutableLiveData()

    val videoDetails = VideoDetails()

    val showVin : MutableLiveData<Boolean> = MutableLiveData()
    val isProjectCreated : MutableLiveData<Boolean> = MutableLiveData()

    private val _subCategoriesResponse: MutableLiveData<Resource<NewSubCatResponse>> = MutableLiveData()
    val subCategoriesResponse: LiveData<Resource<NewSubCatResponse>>
        get() = _subCategoriesResponse

    val subCategory : MutableLiveData<NewSubCatResponse.Data> = MutableLiveData()
    val enableRecording :  MutableLiveData<Boolean> = MutableLiveData()

    var categoryDetails : MutableLiveData<CategoryDetails> = MutableLiveData()


    fun getSubCategories(
        authKey: String, prodId: String
    ) = viewModelScope.launch {
        _subCategoriesResponse.value = Resource.Loading
        _subCategoriesResponse.value = repository.getSubCategories(authKey, prodId)
    }

    private val _createProjectRes : MutableLiveData<Resource<CreateProjectRes>> = MutableLiveData()
    val createProjectRes: LiveData<Resource<CreateProjectRes>>
        get() = _createProjectRes

    private val _createSkuRes : MutableLiveData<Resource<CreateSkuRes>> = MutableLiveData()
    val createSkuRes: LiveData<Resource<CreateSkuRes>>
        get() = _createSkuRes

    private val _process360Res : MutableLiveData<Resource<ProcessThreeSixtyRes>> = MutableLiveData()
    val process360Res: LiveData<Resource<ProcessThreeSixtyRes>>
        get() = _process360Res

    fun createProject(
        authKey: String, projectName: String, prodCatId: String
    ) = viewModelScope.launch {
        _createProjectRes.value = Resource.Loading
        _createProjectRes.value = repository.createProject(authKey, projectName, prodCatId)
    }

    fun createSku(
        authKey: String, projectId: String, prodCatId: String, prodSubCatId: String,
        skuName: String
    ) = viewModelScope.launch {
        _createSkuRes.value = Resource.Loading
        _createSkuRes.value = repository.createSku(
            authKey,
            projectId,
            prodCatId,
            prodSubCatId,
            skuName
        )
    }

    fun process360(
        authKey: String
    ) = viewModelScope.launch {
        _process360Res.value = Resource.Loading
        _process360Res.value = threeSixtyRepository.process360(authKey,videoDetails)
    }


}