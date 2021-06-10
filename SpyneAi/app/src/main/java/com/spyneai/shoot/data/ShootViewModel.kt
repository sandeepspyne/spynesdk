package com.spyneai.shoot.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.base.network.Resource
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.shoot.data.model.ShootProgress
import kotlinx.coroutines.launch
import java.util.ArrayList

class ShootViewModel : ViewModel(){

    private val repository = ShootRepository()

    private val _subCategoriesResponse: MutableLiveData<Resource<NewSubCatResponse>> = MutableLiveData()
    val subCategoriesResponse: LiveData<Resource<NewSubCatResponse>>
        get() = _subCategoriesResponse

    private val _overlaysResponse: MutableLiveData<Resource<OverlaysResponse>> = MutableLiveData()
    val overlaysResponse: LiveData<Resource<OverlaysResponse>>
        get() = _overlaysResponse

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

}