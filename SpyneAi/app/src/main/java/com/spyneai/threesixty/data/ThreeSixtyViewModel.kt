package com.spyneai.threesixty.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.base.room.AppDatabase
import com.spyneai.camera2.ShootDimensions
import com.spyneai.credits.model.DownloadHDRes
import com.spyneai.credits.model.ReduceCreditResponse
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.shoot.data.ShootLocalRepository
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.data.model.CarsBackgroundRes
import com.spyneai.shoot.data.model.CreateProjectRes
import com.spyneai.shoot.data.model.CreateSkuRes
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.sku.Sku
import com.spyneai.threesixty.data.model.VideoDetails
import com.spyneai.threesixty.data.response.ProcessThreeSixtyRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class ThreeSixtyViewModel : ViewModel() {

    private val repository = ShootRepository()
    private val threeSixtyRepository = ThreeSixtyRepository()
    private val localRepository = ShootLocalRepository(AppDatabase.getInstance(BaseApplication.getContext()).shootDao())
    private val videoRepository = VideoLocalRepoV2(AppDatabase.getInstance(BaseApplication.getContext()).videoDao())

    var sku: com.spyneai.shoot.repository.model.sku.Sku? = null
    var project: com.spyneai.shoot.repository.model.project.Project? = null

    var fromDrafts  = false
    val isDemoClicked: MutableLiveData<Boolean> = MutableLiveData()
    val isFramesUpdated: MutableLiveData<Boolean> = MutableLiveData()
    val title : MutableLiveData<String> = MutableLiveData()
    var processingStarted : MutableLiveData<Boolean> = MutableLiveData()

    var videoDetails : VideoDetails? = null

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




    fun getBackgroundGifCars(
        category: String
    ) = viewModelScope.launch {
        _carGifRes.value = Resource.Loading
        GlobalScope.launch(Dispatchers.IO) {
            val backgroundList = localRepository.getBackgrounds(category)

            if (!backgroundList.isNullOrEmpty()){
                GlobalScope.launch(Dispatchers.Main) {
                    _carGifRes.value = Resource.Success(
                        CarsBackgroundRes(
                            backgroundList,
                            "Fetched backgrounds successfully",
                            200
                        )
                    )
                }
            }else {
                val response =threeSixtyRepository.getBackgroundGifCars(category)

                if (response is Resource.Success){
                    //insert overlays
                    val bgList = response.value.data

                    bgList.forEach {
                        it.category = category
                    }
                    localRepository.insertBackgrounds(bgList)

                    GlobalScope.launch(Dispatchers.Main) {
                        _carGifRes.value = response
                    }
                }else {
                    GlobalScope.launch(Dispatchers.Main) {
                        _carGifRes.value = response
                    }
                }
            }
        }
    }


    fun process360(
        authKey: String
    ) = viewModelScope.launch {
        _process360Res.value = Resource.Loading
        _process360Res.value = threeSixtyRepository.process360(authKey,videoDetails!!)
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

    fun insertProject() {
        localRepository.insertProject(project!!)
    }

    suspend fun insertSku() {
        localRepository.insertSku(sku!!,project!!)
        videoRepository.updateVideo(videoDetails!!)
    }


    fun insertVideo(video: VideoDetails) {
           viewModelScope.launch(Dispatchers.IO){
               videoRepository.insertVideo(video)
           }
    }

    fun updateVideoPath() {
        videoRepository.updateVideo(videoDetails!!)
    }

    fun updateVideoDetails() = viewModelScope.launch(Dispatchers.IO) {
        videoRepository.updateVideo(videoDetails!!)
    }

    suspend fun updateBackground(totalFrames: Int) = localRepository.updateBackground(HashMap<String,Any>()
        .apply {
            put("project_uuid", videoDetails!!.projectUuid!!)
            put("sku_uuid", videoDetails!!.skuUuid!!)
            put("bg_id", videoDetails?.backgroundId?.toInt()!!)
            put("bg_name", videoDetails?.bgName.toString())
            put("total_frames", 0)
        })

    fun setVideoDatils(uuid : String) {
        viewModelScope.launch(Dispatchers.IO) {
            videoDetails = videoRepository.getVideo(uuid)
        }
    }
}