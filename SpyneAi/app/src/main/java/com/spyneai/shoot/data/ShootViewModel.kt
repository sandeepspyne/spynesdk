package com.spyneai.shoot.data

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
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.reshoot.data.ReshootOverlaysRes
import com.spyneai.shoot.data.model.*
import com.spyneai.shoot.response.SkuProcessStateResponse
import com.spyneai.shoot.response.UpdateVideoSkuRes
import com.spyneai.shoot.workmanager.OverlaysPreloadWorker
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ShootViewModel : ViewModel() {
    private val TAG = "ShootViewModel"
    private val repository = ShootRepository()
    private val localRepository = ShootLocalRepository()
    private val imageRepository = ImageLocalRepository()

    val showHint: MutableLiveData<Boolean> = MutableLiveData()

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

    val location_data: MutableLiveData<JSONObject> = MutableLiveData()

    val isSubCategorySelected: MutableLiveData<Boolean> = MutableLiveData()

    val categoryPosition: MutableLiveData<Int> = MutableLiveData()

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

    private val _updateFootwearSubcatRes: MutableLiveData<Resource<UpdateFootwearSubcatRes>> =
        MutableLiveData()
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

    var currentShoot = 0
    var allExteriorClicked = false
    var allEcomOverlyasClicked = false
    var allInteriorClicked = false
    var allMisc = false
    var allReshootClicked = false

    val shootData: MutableLiveData<ShootData> = MutableLiveData()
    val reshootCompleted: MutableLiveData<Boolean> = MutableLiveData()

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
    val miscAngles: MutableLiveData<Int> = MutableLiveData()

    val reshootCapturedImage: MutableLiveData<Boolean> = MutableLiveData()
   // val confirmCapturedImage: MutableLiveData<Boolean> = MutableLiveData()
    val projectId: MutableLiveData<String> = MutableLiveData()
    val showFoodBackground: MutableLiveData<Boolean> = MutableLiveData()

    val addMoreAngle: MutableLiveData<Boolean> = MutableLiveData()
    var isReshoot = false
    var isReclick = false
    var reshotImageName = ""
    var updateSelectItem : MutableLiveData<Boolean> = MutableLiveData()


    private val _skuProcessStateWithBgResponse: MutableLiveData<Resource<SkuProcessStateResponse>> =
        MutableLiveData()
    val skuProcessStateWithBgResponse: LiveData<Resource<SkuProcessStateResponse>>
        get() = _skuProcessStateWithBgResponse


    private val _skuProcessStateWithShadowResponse: MutableLiveData<Resource<SkuProcessStateResponse>> =
        MutableLiveData()
    val skuProcessStateWithShadowResponse: LiveData<Resource<SkuProcessStateResponse>>
        get() = _skuProcessStateWithShadowResponse

    private val _reshootOverlaysRes: MutableLiveData<Resource<ReshootOverlaysRes>> =
        MutableLiveData()
    val reshootOverlaysRes: LiveData<Resource<ReshootOverlaysRes>>
        get() = _reshootOverlaysRes

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

    fun getSelectedAngles(appName: String): Int {
        return if (exterirorAngles.value == null) {
            when (appName) {
                AppConstants.CARS24, AppConstants.CARS24_INDIA -> 5
                AppConstants.SELL_ANY_CAR -> 4
                else -> 8
            }
        } else {
            exterirorAngles.value!!
        }
    }

    fun insertImage(shootData: ShootData) {
        val image = Image()
        image.projectId = shootData.project_id
        image.skuId = shootData.sku_id
        image.categoryName = shootData.image_category
        image.imagePath = shootData.capturedImage
        image.sequence = shootData.sequence
        image.overlayId = overlayId.toString()
        image.skuName = sku.value?.skuName
        image.angle = shootData.angle
        image.meta = shootData.meta
        image.name = if (shootData.name.contains(".")) shootData.name else shootData.name + "." + shootData.capturedImage.substringAfter(".")
        image.debugData = shootData.debugData
        image.isReshoot = if (isReshoot) 1 else 0
        image.isReclick = if (isReclick) 1 else 0

        if (imageRepository.isImageExist(image.skuId!!, image.name!!)) {
            imageRepository.updateImage(image)
        } else {
            imageRepository.insertImage(image)
        }
    }

    fun createProject(
        authKey: String, projectName: String, prodCatId: String,
        dynamicLayout: JSONObject? = null,
        location_data : JSONObject? = null
    ) = viewModelScope.launch {
        _createProjectRes.value = Resource.Loading
        _createProjectRes.value =
            repository.createProject(authKey, projectName, prodCatId, dynamicLayout,location_data)
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
        _skuProcessStateWithBgResponse.value =
            repository.skuProcessStateWithBackgroundId(auth_key, project_id, background_id)
    }

    fun skuProcessStateWithShadowOption(
        auth_key: String, project_id: String, background_id: Int, shadow: String
    ) = viewModelScope.launch {
        _skuProcessStateWithShadowResponse.value = Resource.Loading
        _skuProcessStateWithShadowResponse.value =
            repository.skuProcessStateWithShadowOption(auth_key, project_id, background_id, shadow)
    }


    fun createSku(
        authKey: String, projectId: String, prodCatId: String, prodSubCatId: String,
        skuName: String, totalFrames: Int
    ) = viewModelScope.launch {
        _createSkuRes.value = Resource.Loading
        _createSkuRes.value =
            repository.createSku(
                authKey,
                projectId,
                prodCatId,
                prodSubCatId,
                skuName,
                totalFrames,
                1,
                0
            )
    }

    fun updateVideoSku(
        skuId: String,
        prodSubCatId: String,
        initialImageCount: Int
    ) = viewModelScope.launch {
        _updateVideoSkuRes.value = Resource.Loading
        _updateVideoSkuRes.value = repository.updateVideoSku(skuId, prodSubCatId, initialImageCount)
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

    fun getImagesbySkuId(skuId: String) = imageRepository.getImagesBySkuId(skuId)

    fun updateProjectStatus(projectId: String) = localRepository.updateProjectStatus(projectId)


    fun updateFootwearSubcategory(
    ) = viewModelScope.launch {
        _updateFootwearSubcatRes.value = Resource.Loading
        _updateFootwearSubcatRes.value = repository.updateFootwearSubcategory(
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString(),
            sku.value?.skuId!!,
            exterirorAngles.value!!,
            subCategory.value?.prod_sub_cat_id!!
        )
    }

    fun updateVideoSkuLocally(sku: Sku) {
        localRepository.updateVideoSkuLocally(sku)
    }


    fun getFileName(
        interiorSize: Int?,
        miscSize: Int?,
    ): String {
        return if (isReshoot){
            reshotImageName
        }else{
            val filePrefix = FileNameManager().getFileName(
                if (categoryDetails.value?.imageType == "Misc") "Focus Shoot" else categoryDetails.value?.imageType!!,
                currentShoot,
                shootList.value,
                interiorSize,
                miscSize
            )

            sku.value?.skuName + "_" + sku.value?.skuId + "_" + filePrefix
        }

    }

    fun getSequenceNumber(exteriorSize: Int, interiorSize: Int, miscSize: Int): Int {
        return SequeneNumberManager().getSequenceNumber(
            fromDrafts,
            if (categoryDetails.value?.imageType == "Misc") "Focus Shoot" else categoryDetails.value?.imageType!!,
            currentShoot,
            shootList.value?.size!!,
            exteriorSize,
            interiorSize,
            miscSize
        )
    }

    fun getOnImageConfirmed(): Boolean {
        return if (onImageConfirmed.value == null) true
        else !onImageConfirmed.value!!
    }

    fun getOverlay(): String {
        return displayThumbanil
//        val overlayRes = (overlaysResponse.value as Resource.Success).value
//        return overlayRes.data[overlayRes.data.indexOf(selectedOverlay)].display_thumbnail
    }

    fun getName(): String {
//        val overlayRes = (overlaysResponse.value as Resource.Success).value
//        return overlayRes.data[overlayRes.data.indexOf(selectedOverlay)].display_name
        return displayName
    }


    var displayName = ""
    var displayThumbanil = ""
    //var sequence = 0
    var overlayId = 0

    // var selectedOverlay : OverlaysResponse.Data? = null
    val getSubCategories = MutableLiveData<Boolean>()
    var isSubcategoriesSelectionShown = false
    val selectAngles = MutableLiveData<Boolean>()

    val onImageConfirmed = MutableLiveData<Boolean>()


    fun getCurrentShoot() = shootList.value?.firstOrNull() {
        it.overlayId == overlayId
    }

    fun checkMiscShootStatus(appName: String) {
        val subCatResponse = (subCategoriesResponse.value as Resource.Success).value

        when {
            subCatResponse.miscellaneous.isNotEmpty() -> {
                showMiscDialog.value = true
            }
            else -> {
                selectBackground(appName)
            }
        }
    }

    fun selectBackground(appName: String) {
        if (appName == AppConstants.OLA_CABS)
            show360InteriorDialog.value = true
        else
            selectBackground.value = true
    }

    fun skipImage(appName: String) {
        when (categoryDetails.value?.imageType) {
            "Interior" -> {
                checkMiscShootStatus(appName)
            }

            "Focus Shoot" -> {
                selectBackground(appName)
            }
        }
    }

    val notifyItemChanged = MutableLiveData<Int>()
    val scrollView = MutableLiveData<Int>()

    fun setSelectedItem(thumbnails: List<Any>) {
        if (getCurrentShoot() == null){
            Log.d(TAG, "setSelectedItem: "+overlayId)
        }else{
            when (categoryDetails.value?.imageType) {
                "Exterior","Footwear" -> {
                    val list = thumbnails as List<OverlaysResponse.Data>

                    val position = currentShoot

                    list[position].isSelected = false
                    list[position].imageClicked = true
                    list[position].imagePath = getCurrentShoot()!!.capturedImage

                    notifyItemChanged.value = position

                    if (position != list.size.minus(1)) {
                        var foundNext = false

                        for (i in position..list.size.minus(1)) {
                            val s = ""
                            if (!list[i].isSelected && !list[i].imageClicked) {
                                foundNext = true
                                list[i].isSelected = true
                                currentShoot = i

                                notifyItemChanged.value = i
                                scrollView.value = i
                                break
                            }
                        }

                        if (!foundNext) {
                            val element = list.firstOrNull {
                                !it.isSelected && !it.imageClicked
                            }

                            if (element != null) {
                                element?.isSelected = true
                                notifyItemChanged.value = list.indexOf(element)
                                scrollView.value = element?.sequenceNumber!!
                            }
                        }
                    } else {
                        val element = list.firstOrNull {
                            !it.isSelected && !it.imageClicked
                        }

                        if (element != null) {
                            element?.isSelected = true
                            notifyItemChanged.value = list.indexOf(element)
                            scrollView.value = element?.sequenceNumber!!
                        }
                    }
                }

                "Interior" -> {
                    val list = thumbnails as List<NewSubCatResponse.Interior>

                    val position = currentShoot

                    list[position].isSelected = false
                    list[position].imageClicked = true
                    list[position].imagePath = getCurrentShoot()!!.capturedImage

                    notifyItemChanged.value = position

                    if (position != list.size.minus(1)) {
                        var foundNext = false

                        for (i in position..list.size.minus(1)) {
                            if (!list[i].isSelected && !list[i].imageClicked) {
                                foundNext = true
                                list[i].isSelected = true
                                notifyItemChanged.value = i
                                scrollView.value = i
                                break
                            }
                        }

                        if (!foundNext) {
                            val element = list.firstOrNull {
                                !it.isSelected && !it.imageClicked
                            }

                            if (element != null) {
                                element?.isSelected = true
                                notifyItemChanged.value = list.indexOf(element)
                                scrollView.value = element?.sequenceNumber!!
                            }
                        }
                    } else {
                        val element = list.firstOrNull {
                            !it.isSelected && !it.imageClicked
                        }

                        if (element != null) {
                            element?.isSelected = true
                            notifyItemChanged.value = list.indexOf(element)
                            scrollView.value = element?.sequenceNumber!!
                        }
                    }
                }

                "Focus Shoot" -> {
                    val list = thumbnails as List<NewSubCatResponse.Miscellaneous>

                    val position = currentShoot

                    list[position].isSelected = false
                    list[position].imageClicked = true
                    list[position].imagePath = getCurrentShoot()!!.capturedImage

                    notifyItemChanged.value = position

                    if (position != list.size.minus(1)) {
                        var foundNext = false

                        for (i in position..list.size.minus(1)) {
                            val s = ""
                            if (!list[i].isSelected && !list[i].imageClicked) {
                                foundNext = true
                                list[i].isSelected = true
                                notifyItemChanged.value = i
                                scrollView.value = i
                                break
                            }
                        }

                        if (!foundNext) {
                            val element = list.firstOrNull {
                                !it.isSelected && !it.imageClicked
                            }

                            if (element != null) {
                                element?.isSelected = true
                                notifyItemChanged.value = list.indexOf(element)
                                scrollView.value = element?.sequenceNumber!!
                            }
                        }
                    } else {
                        val element = list.firstOrNull {
                            !it.isSelected && !it.imageClicked
                        }

                        if (element != null) {
                            element?.isSelected = true
                            notifyItemChanged.value = list.indexOf(element)
                            scrollView.value = element?.sequenceNumber!!
                        }
                    }
                }
            }
        }
    }


    fun getOverlayIds(
        ids: JSONArray
    ) = viewModelScope.launch {
        _reshootOverlaysRes.value = Resource.Loading
        _reshootOverlaysRes.value = repository.getOverlayIds(ids)
    }

    fun updateSkuExteriorAngles(skuId: String,angles: Int,subcatId : String) {
        UpdateExteriorAngles(skuId,angles,subcatId).update()
        localRepository.updateSkuExteriorAngles(skuId,angles)
    }

    var gifDialogShown = false
    var createProjectDialogShown = false

    init {
        if (showVin.value == null) {
            Log.d(TAG, ": showvin null")
            showHint.value = true
        }

        if (showVin.value != null && isProjectCreated.value == null)
            showVin.value = true

        if (isProjectCreated.value == true)
            getSubCategories.value = true

    }
}