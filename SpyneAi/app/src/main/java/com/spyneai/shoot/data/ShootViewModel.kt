package com.spyneai.shoot.data

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
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.model.*
import com.spyneai.shoot.response.SkuProcessStateResponse
import com.spyneai.shoot.response.UpdateVideoSkuRes
import com.spyneai.shoot.workmanager.OverlaysPreloadWorker
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ShootViewModel : ViewModel() {
    private val TAG = "ShootViewModel"
    private val repository = ShootRepository()
    private val localRepository = ShootLocalRepository()



    val showHint : MutableLiveData<Boolean> = MutableLiveData()

    var isCameraButtonClickable = true
    var processSku: Boolean = true
    var isStopCaptureClickable = false

    var threeSixtyInteriorSelected = false
    var onVolumeKeyPressed: MutableLiveData<Boolean> = MutableLiveData()
    var fromDrafts = false
    var fromVideo = false
    val isSensorAvailable: MutableLiveData<Boolean> = MutableLiveData()
    var showDialog = true
    var miscDialogShowed = false

    val skuNumber: MutableLiveData<Int> = MutableLiveData()

    val isSubCategorySelected: MutableLiveData<Boolean> = MutableLiveData()

    val dafault_project: MutableLiveData<String> = MutableLiveData()
    val dafault_sku: MutableLiveData<String> = MutableLiveData()

    val isSubCatAngleConfirmed: MutableLiveData<Boolean> = MutableLiveData()

    val startInteriorShoot: MutableLiveData<Boolean> = MutableLiveData()
    val begin: MutableLiveData<Long> = MutableLiveData()
    val end: MutableLiveData<Long> = MutableLiveData()

    val totalSkuCaptured: MutableLiveData<String> = MutableLiveData()
    val totalImageCaptured: MutableLiveData<String> = MutableLiveData()
    val show360InteriorDialog: MutableLiveData<Boolean> = MutableLiveData()
    val interior360Dialog: MutableLiveData<Boolean> = MutableLiveData()

    val iniProgressFrame: MutableLiveData<Boolean> = MutableLiveData()


    val subCatName: MutableLiveData<String> = MutableLiveData()

    val shootList: MutableLiveData<ArrayList<ShootData>> = MutableLiveData()



    private val _subCategoriesResponse: MutableLiveData<Resource<NewSubCatResponse>> =
        MutableLiveData()
    val subCategoriesResponse: LiveData<Resource<NewSubCatResponse>>
        get() = _subCategoriesResponse

    private val _getProjectNameResponse: MutableLiveData<Resource<GetProjectNameResponse>> =
        MutableLiveData()
    val getProjectNameResponse: LiveData<Resource<GetProjectNameResponse>>
        get() = _getProjectNameResponse

    private val _projectDetailResponse: MutableLiveData<Resource<ProjectDetailResponse>> =
        MutableLiveData()
    val projectDetailResponse: LiveData<Resource<ProjectDetailResponse>>
        get() = _projectDetailResponse

    private val _skuProcessStateResponse: MutableLiveData<Resource<SkuProcessStateResponse>> =
        MutableLiveData()
    val skuProcessStateResponse: LiveData<Resource<SkuProcessStateResponse>>
        get() = _skuProcessStateResponse

    private val _updateTotalFramesRes: MutableLiveData<Resource<UpdateTotalFramesRes>> =
        MutableLiveData()
    val updateTotalFramesRes: LiveData<Resource<UpdateTotalFramesRes>>
        get() = _updateTotalFramesRes

    private var _overlaysResponse: MutableLiveData<Resource<OverlaysResponse>> = MutableLiveData()
    val overlaysResponse: LiveData<Resource<OverlaysResponse>>
        get() = _overlaysResponse

    var _createProjectRes: MutableLiveData<Resource<CreateProjectRes>> = MutableLiveData()
    val createProjectRes: LiveData<Resource<CreateProjectRes>>
        get() = _createProjectRes

    private val _createSkuRes: MutableLiveData<Resource<CreateSkuRes>> = MutableLiveData()
    val createSkuRes: LiveData<Resource<CreateSkuRes>>
        get() = _createSkuRes

    private val _updateVideoSkuRes: MutableLiveData<Resource<UpdateVideoSkuRes>> = MutableLiveData()
    val updateVideoSkuRes: LiveData<Resource<UpdateVideoSkuRes>>
        get() = _updateVideoSkuRes

    private val _updateFootwearSubcatRes : MutableLiveData<Resource<UpdateFootwearSubcatRes>> = MutableLiveData()
    val updateFootwearSubcatRes: LiveData<Resource<UpdateFootwearSubcatRes>>
        get() = _updateFootwearSubcatRes



    val shootDimensions: MutableLiveData<ShootDimensions> = MutableLiveData()
    val sku: MutableLiveData<Sku> = MutableLiveData()
    var subCategory: MutableLiveData<NewSubCatResponse.Data> = MutableLiveData()
    var categoryDetails: MutableLiveData<CategoryDetails> = MutableLiveData()
    val isSubCategoryConfirmed: MutableLiveData<Boolean> = MutableLiveData()
    val showVin: MutableLiveData<Boolean> = MutableLiveData()
    val isProjectCreated: MutableLiveData<Boolean> = MutableLiveData()
    val isProjectCreatedEcom: MutableLiveData<Boolean> = MutableLiveData()
    val isSkuCreated: MutableLiveData<Boolean> = MutableLiveData()
    val showLeveler: MutableLiveData<Boolean> = MutableLiveData()
    var isHintShowen: MutableLiveData<Boolean> = MutableLiveData()

    val subCategoryId: MutableLiveData<String> = MutableLiveData()
    val exterirorAngles: MutableLiveData<Int> = MutableLiveData()
    val shootNumber: MutableLiveData<Int> = MutableLiveData()
    val shootData: MutableLiveData<ShootData> = MutableLiveData()

    val showConfirmReshootDialog: MutableLiveData<Boolean> = MutableLiveData()

    //interior and misc shots
    val showInteriorDialog: MutableLiveData<Boolean> = MutableLiveData()
    val startInteriorShots: MutableLiveData<Boolean> = MutableLiveData()
    val hideLeveler: MutableLiveData<Boolean> = MutableLiveData()
    val showMiscDialog: MutableLiveData<Boolean> = MutableLiveData()
    val startMiscShots: MutableLiveData<Boolean> = MutableLiveData()
    val selectBackground: MutableLiveData<Boolean> = MutableLiveData()
    val stopShoot: MutableLiveData<Boolean> = MutableLiveData()
    val showProjectDetail: MutableLiveData<Boolean> = MutableLiveData()


    val interiorAngles: MutableLiveData<Int> = MutableLiveData()
    val interiorShootNumber: MutableLiveData<Int> = MutableLiveData()
    val miscAngles: MutableLiveData<Int> = MutableLiveData()
    val miscShootNumber: MutableLiveData<Int> = MutableLiveData()

    var overlayRightMargin = 0

    val reshootCapturedImage: MutableLiveData<Boolean> = MutableLiveData()
    val confirmCapturedImage: MutableLiveData<Boolean> = MutableLiveData()
    val projectId: MutableLiveData<String> = MutableLiveData()
    val showFoodBackground: MutableLiveData<Boolean> = MutableLiveData()

    val addMoreAngle: MutableLiveData<Boolean> = MutableLiveData()

    private val _skuProcessStateWithBgResponse: MutableLiveData<Resource<SkuProcessStateResponse>> = MutableLiveData()
    val skuProcessStateWithBgResponse: LiveData<Resource<SkuProcessStateResponse>>
        get() = _skuProcessStateWithBgResponse


    fun getSubCategories(
        authKey: String, prodId: String
    ) = viewModelScope.launch {
        _subCategoriesResponse.value = Resource.Loading
        _subCategoriesResponse.value = repository.getSubCategories(authKey, prodId)
    }

    fun getProjectName(
        authKey: String
    ) = viewModelScope.launch {
        _getProjectNameResponse.value = Resource.Loading
        _getProjectNameResponse.value = repository.getProjectName(authKey)
    }

    fun getOverlays(
        authKey: String, prodId: String,
        prodSubcategoryId: String, frames: String
    ) = viewModelScope.launch {
        _overlaysResponse.value = Resource.Loading
        _overlaysResponse.value = repository.getOverlays(authKey, prodId, prodSubcategoryId, frames)
    }


    suspend fun preloadOverlays(overlays: List<String>) {
        //check if preload worker is alive
        val workManager = WorkManager.getInstance(BaseApplication.getContext())

        val workQuery = WorkQuery.Builder
            .fromTags(listOf("Preload Overlays"))
            .addStates(
                listOf(
                    WorkInfo.State.BLOCKED,
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING,
                    WorkInfo.State.CANCELLED
                )
            )
            .build()

        val workInfos = workManager.getWorkInfos(workQuery).await()

        if (workInfos.size > 0) {
            // stop worker
            startPreloadWorker(overlays)
        } else {
            startPreloadWorker(overlays)
        }
    }

    private fun startPreloadWorker(overlays: List<String>) {
        val data = Data.Builder()
            .putStringArray("overlays", overlays.toTypedArray())
            .putInt("position", 0)
            .build()

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val overlayPreloadWorkRequest =
            OneTimeWorkRequest.Builder(OverlaysPreloadWorker::class.java)
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

    fun updateTotalFrames(skuId: String, totalFrames: String, authKey: String) =
        viewModelScope.launch {
            _updateTotalFramesRes.value = Resource.Loading
            _updateTotalFramesRes.value = repository.updateTotalFrames(skuId, totalFrames, authKey)
        }

    fun getSelectedAngles(appName : String) : Int{
         if (exterirorAngles.value == null){
            return when(appName){
                AppConstants.CARS24,AppConstants.CARS24_INDIA -> 5
                AppConstants.SELL_ANY_CAR -> 4
                else -> 8
            }
        }else {
            return exterirorAngles.value!!
        }
    }

    fun getShootProgressList(angles: Int, selectedAngles: Int): ArrayList<ShootProgress> {
        val shootProgressList = ArrayList<ShootProgress>()
        shootProgressList.add(ShootProgress(true))

        for (i in 1 until angles) {
            if (i <= selectedAngles)
                shootProgressList.add(ShootProgress(true))
            else
                shootProgressList.add(ShootProgress(false))
        }
        return shootProgressList
    }

    fun insertImage(shootData: ShootData) {
        val image = Image()
        image.projectId = shootData.project_id
        image.skuId = shootData.sku_id
        image.categoryName = shootData.image_category
        image.imagePath = shootData.capturedImage
        image.sequence = shootData.sequence
        image.skuName = sku.value?.skuName
        image.angle = shootData.angle
        image.meta = shootData.meta

        localRepository.insertImage(image)
    }

    fun createProject(
        authKey: String, projectName: String, prodCatId: String,
        dynamicLayout : JSONObject? = null
    ) = viewModelScope.launch {
        _createProjectRes.value = Resource.Loading
        _createProjectRes.value = repository.createProject(authKey, projectName, prodCatId,dynamicLayout)
    }

    fun skuProcessState(
        auth_key: String, project_id: String
    ) = viewModelScope.launch {
        _skuProcessStateResponse.value = Resource.Loading
        _skuProcessStateResponse.value = repository.skuProcessState(auth_key, project_id)
    }

    fun skuProcessStateWithBackgroundid(
        auth_key: String, project_id: String, background_id: Int
    ) = viewModelScope.launch {
        _skuProcessStateWithBgResponse.value = Resource.Loading
        _skuProcessStateWithBgResponse.value = repository.skuProcessStateWithBackgroundId(auth_key, project_id, background_id)
    }


    fun createSku(
        authKey: String, projectId: String, prodCatId: String, prodSubCatId: String,
        skuName: String, totalFrames: Int
    ) = viewModelScope.launch {
        _createSkuRes.value = Resource.Loading
        _createSkuRes.value =
            repository.createSku(authKey, projectId, prodCatId, prodSubCatId, skuName, totalFrames,1,0)
    }

    fun updateVideoSku(
        skuId: String,
        prodSubCatId : String,
        initialImageCount: Int
    ) = viewModelScope.launch {
        _updateVideoSkuRes.value = Resource.Loading
        _updateVideoSkuRes.value = repository.updateVideoSku(skuId,prodSubCatId,initialImageCount)
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

    fun updateSubcategoryId(subcategoryId: String, subcategoryName: String) {
        localRepository.updateSubcategoryId(sku.value?.skuId!!, subcategoryId, subcategoryName)
    }

    fun getImagesbySkuId(skuId: String) = localRepository.getImagesBySkuId(skuId)

    fun updateProjectStatus(projectId: String) = localRepository.updateProjectStatus(projectId)


    fun updateFootwearSubcategory(
    ) = viewModelScope.launch {
        _updateFootwearSubcatRes.value = Resource.Loading
        _updateFootwearSubcatRes.value = repository.updateFootwearSubcategory(
            Utilities.getPreference(BaseApplication.getContext(),AppConstants.AUTH_KEY).toString(),
            sku.value?.skuId!!,
            exterirorAngles.value!!,
            subCategory.value?.prod_sub_cat_id!!
            )
    }

    fun updateVideoSkuLocally(sku: Sku) {
        localRepository.updateVideoSkuLocally(sku)
    }




    fun getFileName(): String {
//        val overlayRes = (overlaysResponse.value as Resource.Success).value
//        val size = shootList.value!!.size.plus(1)
        val list = shootList.value

        return when (categoryDetails.value?.imageType) {
            "Exterior" -> {
                categoryDetails.value?.imageType!! + "_" + sequence.plus(1)
            }
            "Interior" -> {
                val interiorList = list?.filter {
                    it.image_category == "Interior"
                }

                if (interiorList == null) {
                   categoryDetails.value?.imageType!! + "_1"
                } else {
                    categoryDetails.value?.imageType!! + "_" + interiorList.size.plus(
                        1
                    )
                }
            }
            "Focus Shoot" -> {
                val miscList = list?.filter {
                    it.image_category == "Focus Shoot"
                }

                if (miscList == null) {
                    "Miscellaneous" + "_1"
                } else {
                    "Miscellaneous_" + miscList.size.plus(1)
                }
            }
            "Footwear" -> {
                categoryDetails.value?.imageType!! + "_" + shootNumber.value?.plus(
                    1
                )
            }
            "Food" -> {
                categoryDetails.value?.imageType!! + "_" + shootNumber.value?.plus(
                    1
                )
            }
            "Ecom" -> {
                categoryDetails.value?.imageType!! + "_" + shootNumber.value?.plus(
                    1
                )
            }
            else -> {
                System.currentTimeMillis().toString()
            }
        }
    }

    fun getSequenceNumber(): Int {
        if (categoryDetails.value?.imageType == "Exterior")
            return sequence.plus(1)
        else
            return shootList.value!!.size.plus(1)
    }

    fun getOnImageConfirmed(): Boolean {
        return if (onImageConfirmed.value == null) true
        else !onImageConfirmed.value!!
    }

    fun getOverlay(): String {
        val overlayRes = (overlaysResponse.value as Resource.Success).value
        return overlayRes.data[overlayRes.data.indexOf(selectedOverlay)].display_thumbnail
    }

    fun getName() : String {
        val overlayRes = (overlaysResponse.value as Resource.Success).value
        return overlayRes.data[overlayRes.data.indexOf(selectedOverlay)].display_name
    }


    var sequence = 0
    var selectedOverlay : OverlaysResponse.Data? = null
    val getSubCategories = MutableLiveData<Boolean>()
    val selectAngles = MutableLiveData<Boolean>()
    val fetchOverlays = MutableLiveData<Boolean>()
    val onImageConfirmed = MutableLiveData<Boolean>()

    init {
        if (showVin.value == null) {
            showHint.value = true
        }

        if (showVin.value != null && isProjectCreated.value == null)
            showVin.value = true

        if (isProjectCreated.value == true)
            getSubCategories.value = true

    }

    fun getCurrentShoot() = shootList.value?.first {
        it.sequence == sequence.plus(1)
    }

}