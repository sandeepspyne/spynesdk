package com.spyneai.processedimages.ui.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.spyneai.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.base.room.AppDatabase
import com.spyneai.isInternetActive
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.shoot.repository.model.image.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProcessedViewModel : ViewModel() {

    private val appDatabase = AppDatabase.getInstance(BaseApplication.getContext())
    private val processedRepository = ProcessedRepository()

    private val _imagesOfSkuRes: MutableLiveData<Resource<ImagesOfSkuRes>> = MutableLiveData()
    val imagesOfSkuRes: LiveData<Resource<ImagesOfSkuRes>>
        get() = _imagesOfSkuRes

    var projectId : String? = null
    var skuId : String? = null
    var skuName : String? = null
    var selectedImageUrl : String? = null
    var categoryId: String? = null

    val reshoot = MutableLiveData<Boolean>()

    fun getImagesOfSku(
        skuId: String
    ) = viewModelScope.launch {
        _imagesOfSkuRes.value = Resource.Loading
        _imagesOfSkuRes.value = processedRepository.getImagesOfSku(skuId)
    }

    fun getImages(skuId: String?, projectUuid: String,skuUuid: String) = viewModelScope.launch {
        _imagesOfSkuRes.value = Resource.Loading

        if (skuId != null && BaseApplication.getContext().isInternetActive()) {
            val response = processedRepository.getImagesOfSku(
                skuId = skuId
            )

            if (response is Resource.Success) {
                appDatabase.withTransaction {
                    val ss = appDatabase.shootDao().insertImagesWithCheck(
                        response.value.data as ArrayList<Image>,
                        projectUuid,
                        skuUuid)

                }

                GlobalScope.launch(Dispatchers.IO) {
                    val response = appDatabase.shootDao().getImagesBySkuId(
                        skuUuid = skuUuid
                    )

                    GlobalScope.launch(Dispatchers.Main) {
                        _imagesOfSkuRes.value = Resource.Success(
                            ImagesOfSkuRes(
                                data = response,
                                message = "done",
                                "",
                                "",
                                200
                            ))
                    }
                }
            }else{
                _imagesOfSkuRes.value = response
            }
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                val response = appDatabase.shootDao().getImagesBySkuId(
                    skuUuid = skuUuid
                )

                GlobalScope.launch(Dispatchers.Main) {
                    _imagesOfSkuRes.value = Resource.Success(
                        ImagesOfSkuRes(
                            data = response,
                            message = "done",
                            "",
                            "",
                            200
                        ))
                }
            }

        }
    }
}