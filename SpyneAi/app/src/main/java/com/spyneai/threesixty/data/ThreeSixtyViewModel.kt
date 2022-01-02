package com.spyneai.threesixty.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.base.network.Resource
import com.spyneai.camera2.ShootDimensions
import com.spyneai.credits.model.DownloadHDRes
import com.spyneai.credits.model.ReduceCreditResponse
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.*
import com.spyneai.threesixty.data.model.VideoDetails
import com.spyneai.threesixty.data.response.ProcessThreeSixtyRes
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class ThreeSixtyViewModel : ViewModel() {

    private val repository = ShootRepository()
    private val threeSixtyRepository = ThreeSixtyRepository()
    private val localRepository = ShootLocalRepository()
    private val videoRepository = VideoLocalRepository()

    var fromDrafts  = false
    val isDemoClicked: MutableLiveData<Boolean> = MutableLiveData()
    val isFramesUpdated: MutableLiveData<Boolean> = MutableLiveData()
    val title : MutableLiveData<String> = MutableLiveData()
    var processingStarted : MutableLiveData<Boolean> = MutableLiveData()

    val videoDetails = VideoDetails()

    val isProjectCreated : MutableLiveData<Boolean> = MutableLiveData()

    val enableRecording :  MutableLiveData<Boolean> = MutableLiveData()
    val shootDimensions : MutableLiveData<ShootDimensions> = MutableLiveData()

    private val _createProjectRes : MutableLiveData<Resource<CreateProjectRes>> = MutableLiveData()
    val createProjectRes: LiveData<Resource<CreateProjectRes>>
        get() = _createProjectRes

    private val _createSkuRes : MutableLiveData<Resource<CreateSkuRes>> = MutableLiveData()
    val createSkuRes: LiveData<Resource<CreateSkuRes>>
        get() = _createSkuRes

    private val _carGifRes : MutableLiveData<Resource<CarsBackgroundRes>> = MutableLiveData()
    val carGifRes: LiveData<Resource<CarsBackgroundRes>>
        get() = _carGifRes


    private val _process360Res : MutableLiveData<Resource<ProcessThreeSixtyRes>> = MutableLiveData()
    val process360Res: LiveData<Resource<ProcessThreeSixtyRes>>
        get() = _process360Res

    private val _userCreditsRes : MutableLiveData<Resource<CreditDetailsResponse>> = MutableLiveData()
    val userCreditsRes: LiveData<Resource<CreditDetailsResponse>>
        get() = _userCreditsRes

    private val _downloadHDRes : MutableLiveData<Resource<DownloadHDRes>> = MutableLiveData()
    val downloadHDRes: LiveData<Resource<DownloadHDRes>>
        get() = _downloadHDRes

    private val _reduceCreditResponse : MutableLiveData<Resource<ReduceCreditResponse>> = MutableLiveData()
    val reduceCreditResponse: LiveData<Resource<ReduceCreditResponse>>
        get() = _reduceCreditResponse

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
            skuName,
            videoDetails.frames,
            0,
            1
        )
    }

    fun getBackgroundGifCars(
        category: String
    ) = viewModelScope.launch {
        _carGifRes.value = Resource.Loading
        _carGifRes.value = threeSixtyRepository.getBackgroundGifCars(category)
    }


    fun process360(
        authKey: String
    ) = viewModelScope.launch {
        _process360Res.value = Resource.Loading
        _process360Res.value = threeSixtyRepository.process360(authKey,videoDetails)
    }

    fun getUserCredits(
        userId : String
    ) = viewModelScope.launch {
        _userCreditsRes.value = Resource.Loading
        _userCreditsRes.value = threeSixtyRepository.getUserCredits(userId)
    }

    fun reduceCredit(
        userId : String,
        creditReduce:String,
        skuId: String
    ) = viewModelScope.launch {
        _reduceCreditResponse.value = Resource.Loading
        _reduceCreditResponse.value = threeSixtyRepository.reduceCredit(userId,creditReduce,skuId)
    }

    fun updateDownloadStatus(
        userId : String,
        skuId: String,
        enterpriseId: String,
        downloadHd: Boolean
    ) = viewModelScope.launch {
        _downloadHDRes.value = Resource.Loading
        _downloadHDRes.value = threeSixtyRepository.updateDownloadStatus(userId,skuId, enterpriseId, downloadHd)
    }

    fun insertProject(project: Project) {
        localRepository.insertProject(project)
    }

    fun insertSku(sku: Sku) {
        localRepository.insertSku(sku)
    }

    fun updateProjectStatus(projectId: String) = localRepository.updateProjectStatus(projectId)

    fun insertVideo(video: VideoDetails) {
            videoRepository.insertVideo(video)
    }

    fun updateVideoPath() {
        videoRepository.addVideoPath(videoDetails.skuId!!,videoDetails.videoPath!!)
    }

    fun updateVideoBackgroundId() {
        videoRepository.addBackgroundId(videoDetails.skuId!!,videoDetails.backgroundId!!)
    }
}