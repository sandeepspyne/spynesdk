package com.spyneai.dashboard.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.data.model.CheckInOutRes
import com.spyneai.dashboard.data.model.GetGCPUrlRes
import com.spyneai.dashboard.data.model.LocationsRes
import com.spyneai.dashboard.data.model.VersionStatusRes
import com.spyneai.dashboard.data.repository.DashboardRepository
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.orders.data.response.CompletedSKUsResponse
import com.spyneai.orders.data.response.GetOngoingSkusResponse
import com.spyneai.orders.data.response.GetProjectsResponse
import kotlinx.coroutines.launch
import org.json.JSONObject


class DashboardViewModel() : ViewModel() {

    private val repository = DashboardRepository()


    private val _categoriesResponse: MutableLiveData<Resource<NewCategoriesResponse>> = MutableLiveData()
    val categoriesResponse: LiveData<Resource<NewCategoriesResponse>>
        get() = _categoriesResponse

    private val _getOngoingSkusResponse: MutableLiveData<Resource<GetOngoingSkusResponse>> = MutableLiveData()
    val getOngoingSkusResponse: LiveData<Resource<GetOngoingSkusResponse>>
        get() = _getOngoingSkusResponse

    private val _completedSkusResponse: MutableLiveData<Resource<CompletedSKUsResponse>> = MutableLiveData()
    val completedSkusResponse: LiveData<Resource<CompletedSKUsResponse>>
        get() = _completedSkusResponse

    private val _versionResponse: MutableLiveData<Resource<VersionStatusRes>> = MutableLiveData()
    val versionResponse: LiveData<Resource<VersionStatusRes>>
        get() = _versionResponse

    var _gcpUrlResponse: MutableLiveData<Resource<GetGCPUrlRes>> = MutableLiveData()
    val gcpUrlResponse: LiveData<Resource<GetGCPUrlRes>>
        get() = _gcpUrlResponse

    var _locationResponse: MutableLiveData<Resource<LocationsRes>> = MutableLiveData()
    val locationsResponse: LiveData<Resource<LocationsRes>>
        get() = _locationResponse

    var _checkInOutRes: MutableLiveData<Resource<CheckInOutRes>> = MutableLiveData()
    val checkInOutRes: LiveData<Resource<CheckInOutRes>>
        get() = _checkInOutRes

    val isNewUser: MutableLiveData<Boolean> = MutableLiveData()
    val isStartAttendance: MutableLiveData<Boolean> = MutableLiveData()
    val creditsMessage: MutableLiveData<String> = MutableLiveData()
    var type = "checkin"
    var fileUrl = ""
    var siteImagePath = ""
    var resultCode: Int? = null
    val continueAnyway: MutableLiveData<Boolean> = MutableLiveData()


    fun getCategories(
        tokenId: String
    ) = viewModelScope.launch {
        _categoriesResponse.value = Resource.Loading
        _categoriesResponse.value = repository.getCategories(tokenId)
    }

    fun getOngoingSKUs(
        tokenId: String
    ) = viewModelScope.launch {
        _getOngoingSkusResponse.value = Resource.Loading
        _getOngoingSkusResponse.value = repository.getOngoingSKUs(tokenId)

    }

    fun getCompletedSKUs(
        auth_key : String
    ) = viewModelScope.launch {
        _completedSkusResponse.value = Resource.Loading
        _completedSkusResponse.value = repository.getCompletedProjects(auth_key)

    }

    //ongoing completed

    private val _getProjectsResponse: MutableLiveData<Resource<GetProjectsResponse>> = MutableLiveData()
    val getProjectsResponse: LiveData<Resource<GetProjectsResponse>>
        get() = _getProjectsResponse

    fun getProjects(
        tokenId: String, status: String
    ) = viewModelScope.launch {
        _getProjectsResponse.value = Resource.Loading
        _getProjectsResponse.value = repository.getProjects(tokenId, status)
    }

    private val _getCompletedProjectsResponse: MutableLiveData<Resource<GetProjectsResponse>> = MutableLiveData()
    val getCompletedProjectsResponse: LiveData<Resource<GetProjectsResponse>>
        get() = _getCompletedProjectsResponse

    fun getCompletedProjects(
        tokenId: String, status: String
    ) = viewModelScope.launch {
        _getCompletedProjectsResponse.value = Resource.Loading
        _getCompletedProjectsResponse.value = repository.getProjects(tokenId, status)
    }

    fun getVersionStatus(
        authKey: String,
        appVersion: String
    ) = viewModelScope.launch {
        _versionResponse.value = Resource.Loading
        _versionResponse.value = repository.getVersionStatus(authKey, appVersion)
    }

    fun getGCPUrl(
        imageName: String,
    )= viewModelScope.launch {
        _gcpUrlResponse.value = Resource.Loading
        _gcpUrlResponse.value = repository.getGCPUrl(imageName)
    }

    fun captureCheckInOut(
        type : String,
        location : JSONObject,
        imageUrl : String = ""
    )= viewModelScope.launch {
        _checkInOutRes.value = Resource.Loading
        _checkInOutRes.value = repository.captureCheckInOut(type,location,imageUrl)
    }


    fun getLocations(
    )= viewModelScope.launch {
        _locationResponse.value = Resource.Loading
        _locationResponse.value = repository.getLocations()
    }







}