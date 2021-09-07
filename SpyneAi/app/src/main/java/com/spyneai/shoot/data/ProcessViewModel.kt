package com.spyneai.shoot.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.spyneai.BaseApplication
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.credits.model.DownloadHDRes
import com.spyneai.credits.model.ReduceCreditResponse
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.model.CarsBackgroundRes
import com.spyneai.shoot.data.model.ProcessSkuRes
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.data.model.UpdateTotalFramesRes
import com.spyneai.shoot.response.SkuProcessStateResponse
import com.spyneai.shoot.workmanager.ProjectStateUpdateWorker
import kotlinx.coroutines.launch
import okhttp3.RequestBody

class ProcessViewModel : ViewModel() {

    private val repository = ProcessRepository()
    private val localRepository = ShootLocalRepository()

    val exteriorAngles: MutableLiveData<Int> = MutableLiveData()

    val sku : MutableLiveData<Sku> = MutableLiveData()
    val startTimer : MutableLiveData<Boolean> = MutableLiveData()
    val processSku : MutableLiveData<Boolean> = MutableLiveData()
    val skuQueued : MutableLiveData<Boolean> = MutableLiveData()
    var addRegularShootSummaryFragment : MutableLiveData<Boolean> = MutableLiveData()
    var backgroundSelect : String? = null

    val projectId: MutableLiveData<String> = MutableLiveData()

    var frontFramesList = ArrayList<String>()
    var isRegularShootSummaryActive = false

    var interiorMiscShootsCount = 0

    var categoryName : String? = null


    val _carGifRes : MutableLiveData<Resource<CarsBackgroundRes>> = MutableLiveData()
    val carGifRes: LiveData<Resource<CarsBackgroundRes>>
        get() = _carGifRes

    private val _processSkuRes : MutableLiveData<Resource<ProcessSkuRes>> = MutableLiveData()
    val processSkuRes: LiveData<Resource<ProcessSkuRes>>
        get() = _processSkuRes

    private val _userCreditsRes : MutableLiveData<Resource<CreditDetailsResponse>> = MutableLiveData()
    val userCreditsRes: LiveData<Resource<CreditDetailsResponse>>
        get() = _userCreditsRes


    private val _reduceCreditResponse : MutableLiveData<Resource<ReduceCreditResponse>> = MutableLiveData()
    val reduceCreditResponse: LiveData<Resource<ReduceCreditResponse>>
        get() = _reduceCreditResponse

    private val _downloadHDRes : MutableLiveData<Resource<DownloadHDRes>> = MutableLiveData()
    val downloadHDRes: LiveData<Resource<DownloadHDRes>>
        get() = _downloadHDRes

    private val _skuProcessStateWithBgResponse: MutableLiveData<Resource<SkuProcessStateResponse>> = MutableLiveData()
    val skuProcessStateWithBgResponse: LiveData<Resource<SkuProcessStateResponse>>
        get() = _skuProcessStateWithBgResponse

    private val _updateTotalFramesRes : MutableLiveData<Resource<UpdateTotalFramesRes>> = MutableLiveData()
    val updateTotalFramesRes: LiveData<Resource<UpdateTotalFramesRes>>
        get() = _updateTotalFramesRes

    fun getBackgroundGifCars(
        category: RequestBody,
        auth_key: RequestBody
    ) = viewModelScope.launch {
        _carGifRes.value = Resource.Loading
        when(BaseApplication.getContext().getString(R.string.app_name)){
            AppConstants.SWIGGY -> {
                val list = ArrayList<CarsBackgroundRes.Data>()

                list.add(CarsBackgroundRes.Data("Rustic White",
                    "https://storage.googleapis.com/spyne/AI/raw/72cbc0ea-3373-4312-99ba-4ef69de4384e.jpg",
                    1,
                    "11001",
                    "https://storage.googleapis.com/spyne/AI/raw/b972331d-5a82-46bf-b347-007962ce9158.png"))


                list.add(CarsBackgroundRes.Data("Colonial Blue",
                    "https://storage.googleapis.com/spyne/AI/raw/5a2f8ef2-ecd8-4d35-b747-fcb1b08223cd.jpg",
                    1,
                    "12001",
                    "https://storage.googleapis.com/spyne/AI/raw/74a28609-218e-4ed8-9d6f-24e869b1beeb.png"))


                list.add(CarsBackgroundRes.Data("Boho Chic",
                    "https://storage.googleapis.com/spyne/AI/raw/1892526c-72c9-4331-8ede-dec5f72cf52e.png",
                    1,
                    "13001",
                    "https://storage.googleapis.com/spyne-website/Food%20Backgrounds/Rustic%20White%20-%200.jpg"))


                val response = CarsBackgroundRes(list,"Got Background",200)
                _carGifRes.value = Resource.Success(response)

            }
            AppConstants.SPYNE_AI -> {
                val list = ArrayList<CarsBackgroundRes.Data>()

                list.add(CarsBackgroundRes.Data("Xyz",
                    "https://storage.googleapis.com/spyne/AI/raw/1892526c-72c9-4331-8ede-dec5f72cf52e.png",
                    1,
                    "2222",
                    "https://storage.googleapis.com/spyne/AI/raw/1892526c-72c9-4331-8ede-dec5f72cf52e.png"))


                val response = CarsBackgroundRes(list,"Got Background",200)
                _carGifRes.value = Resource.Success(response)

            }
            else -> {
            _carGifRes.value = repository.getBackgroundGifCars(category, auth_key)
        }
        }

    }

    fun processSku(authKey : String,skuId : String, backgroundId : String,is360 : Boolean)
            = viewModelScope.launch {

        //queue process request
        localRepository.queueProcessRequest(sku.value?.skuId!!, backgroundId,is360)

        _processSkuRes.value = Resource.Loading
        _processSkuRes.value = repository.processSku(authKey, skuId, backgroundId,is360)
    }

    fun checkImagesUploadStatus(backgroundSelect: String) {
        if (localRepository.isImagesUploaded(sku.value?.skuId!!)){
            processSku.value = true
        }else{
           // localRepository.queueProcessRequest(sku.value?.skuId!!, backgroundSelect)
            skuQueued.value =  true
        }
    }

    fun updateCarTotalFrames(authKey: String,skuId: String,totalFrames: String)
    = viewModelScope.launch {
        _updateTotalFramesRes.value = Resource.Loading
        _updateTotalFramesRes.value = repository.updateTotalFrames(authKey, skuId,totalFrames)
    }

    fun getUserCredits(
        userId : String
    ) = viewModelScope.launch {
        _userCreditsRes.value = Resource.Loading
        _userCreditsRes.value = repository.getUserCredits(userId)
    }

    fun reduceCredit(
        userId : String,
        creditReduce:String
    ) = viewModelScope.launch {
        _reduceCreditResponse.value = Resource.Loading
        _reduceCreditResponse.value = repository.reduceCredit(userId,creditReduce)
    }

    fun updateDownloadStatus(
        userId : String,
        skuId: String,
        enterpriseId: String,
        downloadHd: Boolean
    ) = viewModelScope.launch {
        _downloadHDRes.value = Resource.Loading
        _downloadHDRes.value = repository.updateDownloadStatus(userId,skuId, enterpriseId, downloadHd)
    }

    fun updateIsProcessed(projectId: String,skuId: String) {
        localRepository.updateIsProcessed(projectId,skuId)
    }

    fun skuProcessStateWithBackgroundid(
        auth_key: String, project_id: String, background_id: String
    ) = viewModelScope.launch {
        _skuProcessStateWithBgResponse.value = Resource.Loading
        _skuProcessStateWithBgResponse.value = repository.skuProcessStateWithBackgroundId(auth_key, project_id, background_id)
    }

    fun updateProjectState(authKey: String,projectId : String) {
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
                    .build())
    }
}