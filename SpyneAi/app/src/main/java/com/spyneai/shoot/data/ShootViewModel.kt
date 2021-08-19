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
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.shoot.data.model.*
import com.spyneai.shoot.response.SkuProcessStateResponse
import com.spyneai.shoot.workmanager.FrameUpdateWorker
import com.spyneai.shoot.workmanager.OverlaysPreloadWorker
import com.spyneai.shoot.workmanager.RecursiveImageWorker
import kotlinx.coroutines.launch
import java.util.*

class ShootViewModel : ViewModel(){

    private val TAG = "ShootViewModel"
    private val repository = ShootRepository()
    private val localRepository = ShootLocalRepository()

     var isCameraButtonClickable = true
    var processSku : Boolean = true
     var isStopCaptureClickable = false

    var fromDrafts = false
    val isSensorAvaliable : MutableLiveData<Boolean> = MutableLiveData()
    var showDialog = true

    val skuNumber : MutableLiveData<Int> = MutableLiveData()

    val isSubCategorySelected : MutableLiveData<Boolean> = MutableLiveData()

    val isSubCatAngleConfirmed : MutableLiveData<Boolean> = MutableLiveData()

    val startInteriorShoot : MutableLiveData<Boolean> = MutableLiveData()

    val totalSkuCaptured : MutableLiveData<String> = MutableLiveData()
    val totalImageCaptured : MutableLiveData<String> = MutableLiveData()

    val iniProgressFrame : MutableLiveData<Boolean> = MutableLiveData()

    val subCatName : MutableLiveData<String> = MutableLiveData()

    val shootList: MutableLiveData<ArrayList<ShootData>> = MutableLiveData()

    private val _subCategoriesResponse: MutableLiveData<Resource<NewSubCatResponse>> = MutableLiveData()
    val subCategoriesResponse: LiveData<Resource<NewSubCatResponse>>
        get() = _subCategoriesResponse

    private val _projectDetailResponse: MutableLiveData<Resource<ProjectDetailResponse>> = MutableLiveData()
    val projectDetailResponse: LiveData<Resource<ProjectDetailResponse>>
        get() = _projectDetailResponse

    private val _skuProcessStateResponse: MutableLiveData<Resource<SkuProcessStateResponse>> = MutableLiveData()
    val skuProcessStateResponse: LiveData<Resource<SkuProcessStateResponse>>
        get() = _skuProcessStateResponse

    private val _updateTotalFramesRes : MutableLiveData<Resource<UpdateTotalFramesRes>> = MutableLiveData()
    val updateTotalFramesRes: LiveData<Resource<UpdateTotalFramesRes>>
        get() = _updateTotalFramesRes

    private var _overlaysResponse: MutableLiveData<Resource<OverlaysResponse>> = MutableLiveData()
    val overlaysResponse: LiveData<Resource<OverlaysResponse>>
        get() = _overlaysResponse

    var _createProjectRes : MutableLiveData<Resource<CreateProjectRes>> = MutableLiveData()
    val createProjectRes: LiveData<Resource<CreateProjectRes>>
        get() = _createProjectRes

    private val _createSkuRes : MutableLiveData<Resource<CreateSkuRes>> = MutableLiveData()
    val createSkuRes: LiveData<Resource<CreateSkuRes>>
        get() = _createSkuRes


    val shootDimensions : MutableLiveData<ShootDimensions> = MutableLiveData()
    val sku : MutableLiveData<Sku> = MutableLiveData()
    var subCategory : MutableLiveData<NewSubCatResponse.Data> = MutableLiveData()
    var categoryDetails : MutableLiveData<CategoryDetails> = MutableLiveData()
    val isSubCategoryConfirmed : MutableLiveData<Boolean> = MutableLiveData()
    val showVin : MutableLiveData<Boolean> = MutableLiveData()
    val isProjectCreated : MutableLiveData<Boolean> = MutableLiveData()
    val isProjectCreatedEcom : MutableLiveData<Boolean> = MutableLiveData()
    val isSkuCreated : MutableLiveData<Boolean> = MutableLiveData()
    val showLeveler : MutableLiveData<Boolean> = MutableLiveData()
    var isHintShowen : MutableLiveData<Boolean> = MutableLiveData()

    val subCategoryId : MutableLiveData<String> = MutableLiveData()
    val exterirorAngles: MutableLiveData<Int> = MutableLiveData()
    val shootNumber: MutableLiveData<Int> = MutableLiveData()
    val shootData : MutableLiveData<ShootData> =  MutableLiveData()

    val showConfirmReshootDialog : MutableLiveData<Boolean> = MutableLiveData()

    //interior and misc shots
    val showInteriorDialog : MutableLiveData<Boolean> = MutableLiveData()
    val startInteriorShots : MutableLiveData<Boolean> = MutableLiveData()
    val hideLeveler : MutableLiveData<Boolean> = MutableLiveData()
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
        authKey: String, prodId: String
    ) = viewModelScope.launch {
        _subCategoriesResponse.value = Resource.Loading
        _subCategoriesResponse.value = repository.getSubCategories(authKey, prodId)
    }

    fun getOverlays(
        authKey: String, prodId: String,
        prodSubcategoryId: String, frames: String
    ) = viewModelScope.launch {
        _overlaysResponse.value = Resource.Loading
        _overlaysResponse.value = repository.getOverlays(authKey, prodId, prodSubcategoryId, frames)
    }


    suspend fun preloadOverlays(overlays : List<String>) {
        //check if preload worker is alive
        val workManager = WorkManager.getInstance(BaseApplication.getContext())

        val workQuery = WorkQuery.Builder
            .fromTags(listOf("Preload Overlays"))
            .addStates(listOf(WorkInfo.State.BLOCKED, WorkInfo.State.ENQUEUED,WorkInfo.State.RUNNING))
            .build()

        val workInfos = workManager.getWorkInfos(workQuery).await()

        if (workInfos.size > 0) {
            // stop worker
            startPreloadWorker(overlays)
        }else{
            startPreloadWorker(overlays)
        }
    }

    private fun startPreloadWorker(overlays : List<String>) {
        val data = Data.Builder()
            .putStringArray("overlays",overlays.toTypedArray())
            .putInt("position",0)
            .build()

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val overlayPreloadWorkRequest = OneTimeWorkRequest.Builder(OverlaysPreloadWorker::class.java)
            .addTag("Preload Overlays")
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(BaseApplication.getContext())
            .enqueue(overlayPreloadWorkRequest)
    }

    fun getProjectDetail(authKey: String, projectId: String) = viewModelScope.launch {
        _projectDetailResponse.value = Resource.Loading
        _projectDetailResponse.value = repository.getProjectDetail(authKey, projectId)
    }

    fun updateTotalFrames(skuId: String, totalFrames:  String, authKey:  String) = viewModelScope.launch {
        _updateTotalFramesRes.value = Resource.Loading
        _updateTotalFramesRes.value = repository.updateTotalFrames(skuId, totalFrames, authKey)
    }


    fun getSelectedAngles() = exterirorAngles.value


    fun getShootProgressList(angles: Int, selectedAngles: Int): ArrayList<ShootProgress> {
        val shootProgressList = ArrayList<ShootProgress>()
        shootProgressList.add(ShootProgress(true))

        for (i in 1 until angles){
            if (i <= selectedAngles)
            shootProgressList.add(ShootProgress(true))
            else
                shootProgressList.add(ShootProgress(false))
        }
        return shootProgressList
    }



     suspend fun insertImage(shootData: ShootData) {
        val image = Image()
        image.projectId = shootData.project_id
        image.skuId = shootData.sku_id
        image.categoryName = shootData.image_category
        image.imagePath = shootData.capturedImage
        image.sequence = shootData.sequence

        localRepository.insertImage(image)

        //check if long running worker is alive
         val workManager = WorkManager.getInstance(BaseApplication.getContext())

         val workQuery = WorkQuery.Builder
             .fromTags(listOf("Long Running Worker"))
             .addStates(listOf(WorkInfo.State.BLOCKED, WorkInfo.State.ENQUEUED,WorkInfo.State.RUNNING))
             .build()

         val workInfos = workManager.getWorkInfos(workQuery).await()

         Log.d(TAG, "insertImage: "+workInfos.size)

         if (workInfos.size > 0) {
             com.spyneai.shoot.utils.log("alive : ")
            } else {
             com.spyneai.shoot.utils.log("not found : start new")
                //start long running worker
                startLongRunningWorker()
            }

    }

    fun startLongRunningWorker() {
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val longWorkRequest = OneTimeWorkRequest.Builder(RecursiveImageWorker::class.java)
            .addTag("Long Running Worker")

        WorkManager.getInstance(BaseApplication.getContext())
            .enqueue(
                longWorkRequest
                    .setConstraints(constraints)
                    .build())
    }


    fun createProject(
        authKey: String, projectName: String, prodCatId: String
    ) = viewModelScope.launch {
        _createProjectRes.value = Resource.Loading
        _createProjectRes.value = repository.createProject(authKey, projectName, prodCatId)
    }

    fun skuProcessState(
        auth_key: String, project_id: String
    ) = viewModelScope.launch {
        _skuProcessStateResponse.value = Resource.Loading
        _skuProcessStateResponse.value = repository.skuProcessState(auth_key, project_id)
    }

    fun createSku(authKey: String,projectId : String
                  ,prodCatId : String,prodSubCatId : String,
                  skuName : String,totalFrames : Int) = viewModelScope.launch {
        _createSkuRes.value = Resource.Loading
        _createSkuRes.value = repository.createSku(authKey, projectId, prodCatId, prodSubCatId, skuName,totalFrames)
    }

    fun insertSku(sku: Sku) {
        localRepository.insertSku(sku)
    }

    fun updateTotalImages(skuId: String) {
        localRepository.updateTotalImageCount(skuId)
    }

    fun insertProject(project: Project) {
        localRepository.insertProject(project)
    }

    fun updateSubcategoryId(subcategoryId: String,subcategoryName: String) {
        localRepository.updateSubcategoryId(sku.value?.skuId!!,subcategoryId,subcategoryName)
    }

    fun getImagesbySkuId(skuId: String) = localRepository.getImagesBySkuId(skuId)

    fun  updateProjectStatus(projectId: String) = localRepository.updateProjectStatus(projectId)

}