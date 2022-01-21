package com.spyneai.shoot.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.spyneai.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.base.room.AppDatabase
import com.spyneai.credits.model.DownloadHDRes
import com.spyneai.credits.model.ReduceCreditResponse
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.shoot.data.model.CarsBackgroundRes
import com.spyneai.shoot.data.model.ProcessSkuRes
import com.spyneai.shoot.data.model.UpdateTotalFramesRes
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.sku.Sku
import com.spyneai.shoot.response.SkuProcessStateResponse
import com.spyneai.shoot.workmanager.ProjectStateUpdateWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class ProcessViewModel : ViewModel() {

    private val repository = ProcessRepository()
    private val db = AppDatabase.getInstance(BaseApplication.getContext())
    private val localRepository = ShootLocalRepository(db.shootDao(),db.projectDao(),db.skuDao())

    var fromVideo = false
    val exteriorAngles: MutableLiveData<Int> = MutableLiveData()

    var sku: Sku? = null
    var project: Project? = null
    val startTimer: MutableLiveData<Boolean> = MutableLiveData()
    val processSku: MutableLiveData<Boolean> = MutableLiveData()
    val skuQueued: MutableLiveData<Boolean> = MutableLiveData()
    var addRegularShootSummaryFragment: MutableLiveData<Boolean> = MutableLiveData()
    var backgroundSelect: String? = null
    var bgName = ""

    var numberPlateBlur = false
    var windowCorrection = false
    var tintWindow = false

    val projectId: MutableLiveData<String> = MutableLiveData()

    var frontFramesList = ArrayList<String>()
    var isRegularShootSummaryActive = false

    var interiorMiscShootsCount = 0

   // var categoryName: String? = null
    var categoryId : String? = null

    val _carGifRes: MutableLiveData<Resource<CarsBackgroundRes>> = MutableLiveData()
    val carGifRes: LiveData<Resource<CarsBackgroundRes>>
        get() = _carGifRes

    private val _processSkuRes: MutableLiveData<Resource<ProcessSkuRes>> = MutableLiveData()
    val processSkuRes: LiveData<Resource<ProcessSkuRes>>
        get() = _processSkuRes

    private val _userCreditsRes: MutableLiveData<Resource<CreditDetailsResponse>> =
        MutableLiveData()
    val userCreditsRes: LiveData<Resource<CreditDetailsResponse>>
        get() = _userCreditsRes


    private val _reduceCreditResponse: MutableLiveData<Resource<ReduceCreditResponse>> =
        MutableLiveData()
    val reduceCreditResponse: LiveData<Resource<ReduceCreditResponse>>
        get() = _reduceCreditResponse

    private val _downloadHDRes: MutableLiveData<Resource<DownloadHDRes>> = MutableLiveData()
    val downloadHDRes: LiveData<Resource<DownloadHDRes>>
        get() = _downloadHDRes

    private val _skuProcessStateWithBgResponse: MutableLiveData<Resource<SkuProcessStateResponse>> =
        MutableLiveData()
    val skuProcessStateWithBgResponse: LiveData<Resource<SkuProcessStateResponse>>
        get() = _skuProcessStateWithBgResponse

    private val _updateTotalFramesRes: MutableLiveData<Resource<UpdateTotalFramesRes>> =
        MutableLiveData()
    val updateTotalFramesRes: LiveData<Resource<UpdateTotalFramesRes>>
        get() = _updateTotalFramesRes

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
                val response =repository.getBackgroundGifCars(category)

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



//    fun checkImagesUploadStatus(backgroundSelect: String) {
//        if (localRepository.isImagesUploaded(sku?.skuId!!)) {
//            processsku = true
//        } else {
//            // localRepository.queueProcessRequest(sku?.skuId!!, backgroundSelect)
//            skuQueued.value = true
//        }
//    }

    fun updateCarTotalFrames(authKey: String, skuId: String, totalFrames: String) =
        viewModelScope.launch {
            _updateTotalFramesRes.value = Resource.Loading
            _updateTotalFramesRes.value = repository.updateTotalFrames(authKey, skuId, totalFrames)
        }

    fun getUserCredits(
        userId: String
    ) = viewModelScope.launch {
        _userCreditsRes.value = Resource.Loading
        _userCreditsRes.value = repository.getUserCredits(userId)
    }

    fun reduceCredit(
        userId: String,
        creditReduce: String,
        skuId: String
    ) = viewModelScope.launch {
        _reduceCreditResponse.value = Resource.Loading
        _reduceCreditResponse.value = repository.reduceCredit(userId, creditReduce,skuId)
    }

    fun updateDownloadStatus(
        userId: String,
        skuId: String,
        enterpriseId: String,
        downloadHd: Boolean
    ) = viewModelScope.launch {
        _downloadHDRes.value = Resource.Loading
        _downloadHDRes.value =
            repository.updateDownloadStatus(userId, skuId, enterpriseId, downloadHd)
    }




    suspend fun setProjectAndSkuData(projectUuid: String, skuUuid: String) {
        project = localRepository.getProject(projectUuid)
        sku = localRepository.getSkuById(skuUuid)
    }

    fun updateProjectState(authKey: String, projectId: String) {
        val data = Data.Builder()
            .putString("auth_key", authKey)
            .putString("project_id", projectId)

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val longWorkRequest = OneTimeWorkRequest.Builder(ProjectStateUpdateWorker::class.java)
            .addTag("Project State Update")

        WorkManager.getInstance(BaseApplication.getContext())
            .enqueue(
                longWorkRequest
                    .setConstraints(constraints)
                    .setInputData(data.build())
                    .build()
            )
    }

    suspend fun updateBackground() = localRepository.updateBackground(HashMap<String,Any>()
        .apply {
            put("project_uuid", sku!!.projectUuid!!)
            put("sku_uuid", sku!!.uuid!!)
            put("bg_id", backgroundSelect!!)
            put("bg_name", bgName!!)
            put("total_frames", getTotalFrames())
        })

    private fun getTotalFrames(): Int {
        return if (fromVideo) sku?.threeSixtyFrames?.plus(sku?.imagesCount!!)!! else sku?.imagesCount!!
    }
}