package com.spyneai.shoot.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.spyneai.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.camera2.ShootDimensions
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.model.carbackgroundgif.CarBackgrounGifResponse
import com.spyneai.shoot.data.model.*
import com.spyneai.shoot.workmanager.LongRunningWorker
import com.spyneai.shoot.workmanager.UploadImageWorker
import kotlinx.coroutines.launch
import java.util.ArrayList

class ShootViewModel : ViewModel(){

    private val TAG = "ShootViewModel"
    private val repository = ShootRepository()
    private val localRepository = ShootLocalRepository()

     var isCameraButtonClickable = true
    var processSku : Boolean = true
     var isStopCaptureClickable = false
    var isLongRunningWorkerAlive = false

    val totalSkuCaptured : MutableLiveData<String> = MutableLiveData()
    val totalImageCaptured : MutableLiveData<String> = MutableLiveData()

    val shootList: MutableLiveData<ArrayList<ShootData>> = MutableLiveData()

    private val _subCategoriesResponse: MutableLiveData<Resource<NewSubCatResponse>> = MutableLiveData()
    val subCategoriesResponse: LiveData<Resource<NewSubCatResponse>>
        get() = _subCategoriesResponse

    private val _projectDetailResponse: MutableLiveData<Resource<ProjectDetailResponse>> = MutableLiveData()
    val projectDetailResponse: LiveData<Resource<ProjectDetailResponse>>
        get() = _projectDetailResponse

    private val _overlaysResponse: MutableLiveData<Resource<OverlaysResponse>> = MutableLiveData()
    val overlaysResponse: LiveData<Resource<OverlaysResponse>>
        get() = _overlaysResponse

    private val _createProjectRes : MutableLiveData<Resource<CreateProjectRes>> = MutableLiveData()
    val createProjectRes: LiveData<Resource<CreateProjectRes>>
        get() = _createProjectRes

    private val _createSkuRes : MutableLiveData<Resource<CreateSkuRes>> = MutableLiveData()
    val createSkuRes: LiveData<Resource<CreateSkuRes>>
        get() = _createSkuRes


    val shootDimensions : MutableLiveData<ShootDimensions> = MutableLiveData()
    val sku : MutableLiveData<Sku> = MutableLiveData()
    val subCategory : MutableLiveData<NewSubCatResponse.Data> = MutableLiveData()
    var categoryDetails : MutableLiveData<CategoryDetails> = MutableLiveData()
    val isSubCategoryConfirmed : MutableLiveData<Boolean> = MutableLiveData()
    val showVin : MutableLiveData<Boolean> = MutableLiveData()
    val isProjectCreated : MutableLiveData<Boolean> = MutableLiveData()
    val isProjectCreatedEcom : MutableLiveData<Boolean> = MutableLiveData()
    val isSkuCreated : MutableLiveData<Boolean> = MutableLiveData()

    val subCategoryId : MutableLiveData<String> = MutableLiveData()
    val exterirorAngles: MutableLiveData<Int> = MutableLiveData()
    val shootNumber: MutableLiveData<Int> = MutableLiveData()
    val shootData : MutableLiveData<ShootData> =  MutableLiveData()

    //interior and misc shots
    val showInteriorDialog : MutableLiveData<Boolean> = MutableLiveData()
    val startInteriorShots : MutableLiveData<Boolean> = MutableLiveData()
    val showMiscDialog : MutableLiveData<Boolean> = MutableLiveData()
    val startMiscShots : MutableLiveData<Boolean> = MutableLiveData()
    val selectBackground : MutableLiveData<Boolean> = MutableLiveData()
    val stopShoot : MutableLiveData<Boolean> = MutableLiveData()
    val showProjectDetail : MutableLiveData<Boolean> = MutableLiveData()


    val interiorAngles : MutableLiveData<Int> = MutableLiveData()
    val interiorShootNumber: MutableLiveData<Int> = MutableLiveData()
    val miscAngles: MutableLiveData<Int> = MutableLiveData()
    val miscShootNumber: MutableLiveData<Int> = MutableLiveData()

    var overlayRightMargin = 0

    val reshootCapturedImage: MutableLiveData<Boolean> = MutableLiveData()
    val projectId: MutableLiveData<String> = MutableLiveData()

    val addMoreAngle : MutableLiveData<Boolean> = MutableLiveData()


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

    fun getProjectDetail(authKey: String, projectId:  String) = viewModelScope.launch {
        _projectDetailResponse.value = Resource.Loading
        _projectDetailResponse.value = repository.getProjectDetail(authKey, projectId)
    }

    fun getSelectedAngles() = exterirorAngles.value

    fun getShootNumber() = shootNumber.value

    fun getShootProgressList(angles : Int): ArrayList<ShootProgress> {
        val shootProgressList = ArrayList<ShootProgress>()
        shootProgressList.add(ShootProgress(true))

        for (i in 1 until angles)
            shootProgressList.add(ShootProgress(false))

        return shootProgressList
    }


    fun uploadImage(context : Context) {

    }

     suspend fun insertImage(shootData: ShootData) {
        val image = Image()
        image.projectId = shootData.project_id
        image.skuId = shootData.sku_id
        image.categoryName = shootData.image_category
        image.imagePath = shootData.capturedImage
        image.sequence = shootData.sequence

        localRepository.insertImage(image)

         startLongRunningWorker()

        //check if long running worker is alive
        if (!isLongRunningWorkerAlive){
            val workManager = WorkManager.getInstance()

            val workInfos = workManager.getWorkInfosByTag("Long Running Worker").await()



//            if (workInfos.size == 1) {
//                // for (workInfo in workInfos) {
//                val workInfo = workInfos[0]
//                Log.d("ShootViewModel", "insertImage: ${workInfo.state}, id=${workInfo.id}")
//
//                if (workInfo.state == WorkInfo.State.BLOCKED || workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING) {
//                    Log.d(TAG, "insertImage: alive")
//                    isLongRunningWorkerAlive = true
//                } else {
//                    Log.d(TAG, "insertImage: isDead")
//                    //start long running worker
//                    startLongRunningWorker()
//                }
//            } else {
//                Log.d(TAG, "insertImage: notFound")
//                //start long running worker
//                startLongRunningWorker()
//
//            }
        }
    }

    fun startLongRunningWorker() {
        val longWorkRequest = OneTimeWorkRequest.Builder(LongRunningWorker::class.java)
        WorkManager.getInstance(BaseApplication.getContext()).enqueue(longWorkRequest.build())

        isLongRunningWorkerAlive = true
    }

    fun uploadImageWithWorkManager(
        requireContext: Context,
        shootData: ShootData
    ) {
        val uploadWorkRequest = OneTimeWorkRequest.Builder(UploadImageWorker::class.java)

        val data = Data.Builder()
        data.putString("uri", shootData.capturedImage)
        data.putString("projectId", shootData.project_id)
        data.putString("skuId", shootData.sku_id)
        data.putString("imageCategory", shootData.image_category)
        data.putString("authKey", shootData.auth_key)
        data.putBoolean("processSku", processSku)

        uploadWorkRequest.setInputData(data.build())

        WorkManager.getInstance(requireContext).enqueue(uploadWorkRequest.build())
    }

    fun createProject(authKey: String,projectName : String
                      ,prodCatId : String) = viewModelScope.launch {
        _createProjectRes.value = Resource.Loading
        _createProjectRes.value = repository.createProject(authKey, projectName, prodCatId)
    }

    fun createSku(authKey: String,projectId : String
                  ,prodCatId : String,prodSubCatId : String,
                  skuName : String) = viewModelScope.launch {
        _createSkuRes.value = Resource.Loading
        _createSkuRes.value = repository.createSku(authKey, projectId, prodCatId, prodSubCatId, skuName)
    }

    fun insertSku(sku: Sku) {
        localRepository.insertSku(sku)
    }

    fun updateTotalImages(skuId : String) {
        localRepository.updateTotalImageCount(skuId)
    }

}