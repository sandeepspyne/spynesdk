package com.spyneai.dashboard.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.spyneai.BaseApplication
import com.spyneai.base.network.ProjectApiClient
import com.spyneai.base.network.Resource
import com.spyneai.base.room.AppDatabase
import com.spyneai.dashboard.repository.model.CheckInOutRes
import com.spyneai.dashboard.repository.model.GetGCPUrlRes
import com.spyneai.dashboard.repository.model.LocationsRes
import com.spyneai.dashboard.repository.model.VersionStatusRes
import com.spyneai.dashboard.repository.DashboardRepository
import com.spyneai.dashboard.repository.model.category.DynamicLayout
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.orders.data.paging.PagedRepository
import com.spyneai.orders.data.response.CompletedSKUsResponse
import com.spyneai.orders.data.response.GetOngoingSkusResponse
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.shoot.repository.model.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.json.JSONObject


class DashboardViewModel() : ViewModel() {

    private val TAG = DashboardViewModel::class.java.simpleName
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

        GlobalScope.launch(Dispatchers.IO) {
            val catList = repository.getCategories()

            if (!catList.isNullOrEmpty()){
                GlobalScope.launch(Dispatchers.Main) {
                    _categoriesResponse.value = Resource.Success(
                        NewCategoriesResponse(
                            200,
                            "",
                            catList
                        )
                    )
                }

            }else {
                val response = repository.getCategories(tokenId)

                if (response is Resource.Success){
                    //save response to local DB
                    GlobalScope.launch(Dispatchers.IO) {
                        val catList = response.value.data
                        val dynamicList = ArrayList<DynamicLayout>()

                        catList.forEach {
                            dynamicList.add(
                                DynamicLayout(it.categoryId,it.dynamic_layout?.project_dialog)
                            )
                        }
                        repository.insertCategories(
                            catList,
                            dynamicList
                        )

                        GlobalScope.launch(Dispatchers.Main) {
                            _categoriesResponse.value = Resource.Success(
                                NewCategoriesResponse(
                                    200,
                                    "",
                                    catList
                                )
                            )
                        }
                    }
                }else {
                    GlobalScope.launch(Dispatchers.Main) {
                        _categoriesResponse.value = response
                    }
                }
            }
        }
    }

    @ExperimentalPagingApi
    fun getAllProjects(status: String): Flow<PagingData<Project>> {
        return PagedRepository(
            ProjectApiClient().getClient(),
            AppDatabase.getInstance(BaseApplication.getContext()),
            status
        ).getSearchResultStream()
            .cachedIn(viewModelScope)
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
        location_id : String,
        imageUrl : String = ""
    )= viewModelScope.launch {
        _checkInOutRes.value = Resource.Loading
        _checkInOutRes.value = repository.captureCheckInOut(type,location,location_id,imageUrl)
    }


    fun getLocations(
    )= viewModelScope.launch {
        _locationResponse.value = Resource.Loading
        _locationResponse.value = repository.getLocations()
    }







}