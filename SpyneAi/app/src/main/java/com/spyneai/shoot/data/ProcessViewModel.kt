package com.spyneai.shoot.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.spyneai.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.credits.model.DownloadHDRes
import com.spyneai.credits.model.ReduceCreditResponse
import com.spyneai.model.credit.CreditDetailsResponse
import com.spyneai.shoot.data.model.CarsBackgroundRes
import com.spyneai.shoot.data.model.ProcessSkuRes
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.workmanager.FrameUpdateWorker
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
    var frontFramesList = ArrayList<String>()
    var isRegularShootSummaryActive = false

    var interiorMiscShootsCount = 0

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


    fun getBackgroundGifCars(
        category: RequestBody,
        auth_key: RequestBody,
        appName : String
    ) = viewModelScope.launch {
        _carGifRes.value = Resource.Loading
        _carGifRes.value = repository.getBackgroundGifCars(category, auth_key)

    }

    fun processSku(authKey : String,skuId : String, backgroundId : String,is360 : Boolean)
            = viewModelScope.launch {
        _processSkuRes.value = Resource.Loading
        _processSkuRes.value = repository.processSku(authKey, skuId, backgroundId,is360)
    }

    fun checkImagesUploadStatus(backgroundSelect: String) {
        if (localRepository.isImagesUploaded(sku.value?.skuId!!)){
            processSku.value = true
        }else{
            localRepository.queueProcessRequest(sku.value?.skuId!!, backgroundSelect)
            skuQueued.value =  true
        }
    }

    fun updateCarTotalFrames(authKey: String,skuId: String,totalFrames: String) {
        val data = Data.Builder()
            .putString("auth_key", authKey)
            .putString("sku_id", skuId)
            .putString("total_frames", totalFrames)

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val longWorkRequest = OneTimeWorkRequest.Builder(FrameUpdateWorker::class.java)
            .addTag("Total Frames Update")

        WorkManager.getInstance(BaseApplication.getContext())
            .enqueue(
                longWorkRequest
                    .setConstraints(constraints)
                    .setInputData(data.build())
                    .build())
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

}