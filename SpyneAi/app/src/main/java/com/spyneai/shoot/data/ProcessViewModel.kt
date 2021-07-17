package com.spyneai.shoot.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.base.network.Resource
import com.spyneai.shoot.data.model.CarsBackgroundRes
import com.spyneai.shoot.data.model.ProcessSkuRes
import com.spyneai.shoot.data.model.Sku
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

    val _carGifRes : MutableLiveData<Resource<CarsBackgroundRes>> = MutableLiveData()
    val carGifRes: LiveData<Resource<CarsBackgroundRes>>
        get() = _carGifRes

    private val _processSkuRes : MutableLiveData<Resource<ProcessSkuRes>> = MutableLiveData()
    val processSkuRes: LiveData<Resource<ProcessSkuRes>>
        get() = _processSkuRes

    fun getBackgroundGifCars(
        category: RequestBody,
        auth_key: RequestBody,
        appName : String
    ) = viewModelScope.launch {
        _carGifRes.value = Resource.Loading

        when(appName){
            "Karvi.com" -> {
                val carGifList = ArrayList<CarsBackgroundRes.Data>()
                carGifList.add(
                    CarsBackgroundRes.Data(
                        "Radiant Aluminium",
                        "https://storage.googleapis.com/spyne-website/static/website-themes/clippr/comp_backgroundV2/601_2.png",
                        1,
                        "978",
                        "https://storage.googleapis.com/spyne-website/static/website-themes/clippr/comp_backgroundV2/601_2.png"
                    )
                )
                _carGifRes.value =  Resource.Success(CarsBackgroundRes(carGifList,"Success",200))
            }
            "Ola Cabs" -> {
                val carGifList = ArrayList<CarsBackgroundRes.Data>()
                carGifList.add(
                    CarsBackgroundRes.Data(
                        "Plain Krypton",
                        "https://storage.googleapis.com/spyne-website/static/website-themes/clippr/comp_backgroundV2/924_2.png",
                        1,
                        "924",
                        "https://storage.googleapis.com/spyne-website/static/website-themes/clippr/comp_backgroundV2/924_2.png"
                    )
                )

                carGifList.add(
                    CarsBackgroundRes.Data(
                        "Radiant Krypton",
                        "https://storage.googleapis.com/spyne-website/static/website-themes/clippr/comp_backgroundV2/923_2.png",
                        1,
                        "923",
                        "https://storage.googleapis.com/spyne-website/static/website-themes/clippr/comp_backgroundV2/923_2.png"
                    )
                )
                _carGifRes.value =  Resource.Success(CarsBackgroundRes(carGifList,"Success",200))

            }

            else -> _carGifRes.value = repository.getBackgroundGifCars(category, auth_key)
        }

    }

    fun processSku(authKey : String,skuId : String, backgroundId : String)
            = viewModelScope.launch {
        _processSkuRes.value = Resource.Loading
        _processSkuRes.value = repository.processSku(authKey, skuId, backgroundId)
    }

    fun checkImagesUploadStatus(backgroundSelect: String) {
        if (localRepository.isImagesUploaded(sku.value?.skuId!!)){
            processSku.value = true
        }else{
            localRepository.queueProcessRequest(sku.value?.skuId!!, backgroundSelect)
            skuQueued.value =  true
        }
    }
}