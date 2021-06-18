package com.spyneai.shoot.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.base.network.Resource
import com.spyneai.model.carbackgroundgif.CarBackgrounGifResponse
import com.spyneai.shoot.data.model.ProcessSkuRes
import com.spyneai.shoot.data.model.Sku
import kotlinx.coroutines.launch

class ProcessViewModel : ViewModel() {

    private val repository = ProcessRepository()
    private val localRepository = ShootLocalRepository()


    val exteriorAngles: MutableLiveData<Int> = MutableLiveData()

    val sku : MutableLiveData<Sku> = MutableLiveData()
    val startTimer : MutableLiveData<Boolean> = MutableLiveData()
    val processSku : MutableLiveData<Boolean> = MutableLiveData()
    val skuQueued : MutableLiveData<Boolean> = MutableLiveData()

    private val _carGifRes : MutableLiveData<Resource<List<CarBackgrounGifResponse>>> = MutableLiveData()
    val carGifRes: LiveData<Resource<List<CarBackgrounGifResponse>>>
        get() = _carGifRes

    private val _processSkuRes : MutableLiveData<Resource<ProcessSkuRes>> = MutableLiveData()
    val processSkuRes: LiveData<Resource<ProcessSkuRes>>
        get() = _processSkuRes

    fun getBackgroundGifCars() = viewModelScope.launch {
        _carGifRes.value = Resource.Loading
        _carGifRes.value = repository.getBackgroundGifCars()
    }

    fun processSku(authKey : String,skuId : String, backgroundId : String)
            = viewModelScope.launch {
        _processSkuRes.value = Resource.Loading
        _processSkuRes.value = repository.processSku(authKey, skuId, backgroundId)
    }

    fun checkImagesUploadStatus(backgroundId: String) {
        if (localRepository.isImagesUploaded(sku.value?.skuId!!)){
            processSku.value = true
        }else{
            localRepository.queueProcessRequest(sku.value?.skuId!!,backgroundId)
            skuQueued.value = true
        }
    }
}