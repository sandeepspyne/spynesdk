package com.spyneai.shoot.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.spyneai.BaseApplication
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.base.room.AppDatabase
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.camera2.ShootDimensions
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.getUuid
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.reshoot.data.ReshootOverlaysRes
import com.spyneai.shoot.data.model.*
import com.spyneai.shoot.response.SkuProcessStateResponse
import com.spyneai.shoot.response.UpdateVideoSkuRes
import com.spyneai.shoot.workmanager.OverlaysPreloadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ShootViewModel : ViewModel() {
    private val TAG = "ShootViewModel"
    private val repository = ShootRepository()
    private val appDatabase = AppDatabase.getInstance(BaseApplication.getContext())
    private val localRepository = ShootLocalRepository(appDatabase.shootDao(),appDatabase.projectDao(),appDatabase.skuDao())
   // private val imageRepository = ImageLocalRepository()
    private val imageRepositoryV2 = ImagesRepoV2(appDatabase.imageDao())

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

    var dafault_project: MutableLiveData<String> = MutableLiveData()
    var dafault_sku: MutableLiveData<String> = MutableLiveData()

    val isSubCatAngleConfirmed: MutableLiveData<Boolean> = MutableLiveData()

    val startInteriorShoot: MutableLiveData<Boolean> = MutableLiveData()
    val begin: MutableLiveData<Long> = MutableLiveData()
    val end: MutableLiveData<Long> = MutableLiveData()

    val totalSkuCaptured: MutableLiveData<String> = MutableLiveData()
    val totalImageCaptured: MutableLiveData<Int> = MutableLiveData()
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




    private val _updateTotalFramesRes: MutableLiveData<Resource<UpdateTotalFramesRes>> =
        MutableLiveData()
    val updateTotalFramesRes: LiveData<Resource<UpdateTotalFramesRes>>
        get() = _updateTotalFramesRes

    private var _overlaysResponse: MutableLiveData<Resource<OverlaysResponse>> = MutableLiveData()
    val overlaysResponse: LiveData<Resource<OverlaysResponse>>
        get() = _overlaysResponse

    private val _updateVideoSkuRes: MutableLiveData<Resource<UpdateVideoSkuRes>> = MutableLiveData()
    val updateVideoSkuRes: LiveData<Resource<UpdateVideoSkuRes>>
        get() = _updateVideoSkuRes

    private val _updateFootwearSubcatRes: MutableLiveData<Resource<UpdateFootwearSubcatRes>> =
        MutableLiveData()
    val updateFootwearSubcatRes: LiveData<Resource<UpdateFootwearSubcatRes>>
        get() = _updateFootwearSubcatRes


    val shootDimensions: MutableLiveData<ShootDimensions> = MutableLiveData()

    // val sku: MutableLiveData<Sku> = MutableLiveData()
    var sku: com.spyneai.shoot.repository.model.sku.Sku? = null
    var project: com.spyneai.shoot.repository.model.project.Project? = null

    var subCategory: MutableLiveData<NewSubCatResponse.Subcategory> = MutableLiveData()
    var categoryDetails: MutableLiveData<CategoryDetails> = MutableLiveData()
    val isSubCategoryConfirmed: MutableLiveData<Boolean> = MutableLiveData()
    val showVin: MutableLiveData<Boolean> = MutableLiveData()
    val isProjectCreated: MutableLiveData<Boolean> = MutableLiveData()
    val isProjectCreatedEcom: MutableLiveData<Boolean> = MutableLiveData()
    val isSkuCreated: MutableLiveData<Boolean> = MutableLiveData()
    val showLeveler: MutableLiveData<Boolean> = MutableLiveData()
    val showOverlay: MutableLiveData<Boolean> = MutableLiveData()
    val showGrid: MutableLiveData<Boolean> = MutableLiveData()
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
    val showCropDialog: MutableLiveData<Boolean> = MutableLiveData()

    //interior and misc shots
    val showInteriorDialog: MutableLiveData<Boolean> = MutableLiveData()
    val startInteriorShots: MutableLiveData<Boolean> = MutableLiveData()
    val hideLeveler: MutableLiveData<Boolean> = MutableLiveData()
    val showMiscDialog: MutableLiveData<Boolean> = MutableLiveData()
    val startMiscShots: MutableLiveData<Boolean> = MutableLiveData()
    val selectBackground: MutableLiveData<Boolean> = MutableLiveData()
    val stopShoot: MutableLiveData<Boolean> = MutableLiveData()
    val showProjectDetail: MutableLiveData<Boolean> = MutableLiveData()

    val imageTypeInfo: MutableLiveData<Boolean> = MutableLiveData()

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
    var reshootSequence = 0
    var updateSelectItem: MutableLiveData<Boolean> = MutableLiveData()


    private val _reshootOverlaysRes: MutableLiveData<Resource<ReshootOverlaysRes>> =
        MutableLiveData()
    val reshootOverlaysRes: LiveData<Resource<ReshootOverlaysRes>>
        get() = _reshootOverlaysRes

    fun getSubCategories(
        authKey: String, prodId: String
    ) = viewModelScope.launch {
        _subCategoriesResponse.value = Resource.Loading

        GlobalScope.launch(Dispatchers.IO) {
            val subcatList = localRepository.getSubcategories()

            if (!subcatList.isNullOrEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    _subCategoriesResponse.value = Resource.Success(
                        NewSubCatResponse(
                            subcatList,
                            ArrayList(),
                            "",
                            ArrayList(),
                            200,
                            NewSubCatResponse.Tags(ArrayList(), ArrayList(), ArrayList())
                        )
                    )
                }
            } else {
                val response = repository.getSubCategories(authKey, prodId)

                if (response is Resource.Success) {
                    //save response to local DB
                    GlobalScope.launch(Dispatchers.IO) {
                        val subcatList = response.value.data
                        val interiorList =
                            if (response.value.interior.isNullOrEmpty()) ArrayList() else response.value.interior
                        val miscList =
                            if (response.value.miscellaneous.isNullOrEmpty()) ArrayList() else response.value.miscellaneous

                        val exteriorTags =
                            if (response.value.tags.exteriorTags.isNullOrEmpty()) ArrayList() else response.value.tags.exteriorTags
                        val interiorTags =
                            if (response.value.tags.interiorTags.isNullOrEmpty()) ArrayList() else response.value.tags.interiorTags
                        val focusTags =
                            if (response.value.tags.focusShoot.isNullOrEmpty()) ArrayList() else response.value.tags.focusShoot

                        localRepository.insertSubCategories(
                            subcatList,
                            interiorList,
                            miscList,
                            exteriorTags,
                            interiorTags,
                            focusTags
                        )

                        GlobalScope.launch(Dispatchers.Main) {
                            _subCategoriesResponse.value = Resource.Success(
                                NewSubCatResponse(
                                    subcatList,
                                    interiorList,
                                    "",
                                    miscList,
                                    200,
                                    response.value.tags
                                )
                            )
                        }
                    }
                } else {
                   GlobalScope.launch(Dispatchers.Main) {
                       _subCategoriesResponse.value = response
                   }
                }
            }
        }
    }

    fun getOverlays(
        authKey: String, prodId: String,
        prodSubcategoryId: String, frames: String
    ) = viewModelScope.launch {
        _overlaysResponse.value = Resource.Loading

        GlobalScope.launch(Dispatchers.IO) {
            val overlaysList = localRepository.getOverlays(prodSubcategoryId, frames)

            if (!overlaysList.isNullOrEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    _overlaysResponse.value = Resource.Success(
                        OverlaysResponse(
                            overlaysList,
                            "Overlyas fetched successfully",
                            200
                        )
                    )
                }
            } else {
                val response = repository.getOverlays(authKey, prodId, prodSubcategoryId, frames)

                if (response is Resource.Success) {
                    //insert overlays
                    val overlaysList = response.value.data

                    overlaysList.forEach {
                        it.fetchAngle = frames.toInt()
                    }

                    localRepository.insertOverlays(overlaysList)

                    GlobalScope.launch(Dispatchers.Main) {
                        _overlaysResponse.value = response
                    }
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        _overlaysResponse.value = response
                    }
                }
            }
        }
    }

    fun getInteriorList() = localRepository.getInteriorList(subCategory.value?.prod_cat_id!!)

    fun getMiscList() = localRepository.getMiscList(subCategory.value?.prod_cat_id!!)

    public val tags = HashMap<String, Any>()

    suspend fun getTags(type: String): Any? {
        when (type) {
            "Exterior" -> {
                val extags = tags[type]
                if (extags == null) {
                    val exTags = localRepository.getExteriorTags()
                    tags[type] = exTags
                    return tags[type]
                } else {
                    return extags
                }
            }

            "Interior" -> {
                val extags = tags[type]
                if (extags == null) {
                    val exTags = localRepository.getInteriorTags()
                    tags[type] = exTags
                    return tags[type]
                } else {
                    return extags
                }
            }
            else -> {
                val extags = tags[type]
                if (extags == null) {
                    val exTags = localRepository.getFocusTags()
                    tags[type] = exTags
                    return tags[type]
                } else {
                    return extags
                }
            }
        }

    }

    fun getProjectName(
        authKey: String
    ) = viewModelScope.launch {
        _getProjectNameResponse.value = Resource.Loading
        _getProjectNameResponse.value = repository.getProjectName(authKey)
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

    suspend fun insertImage(shootData: ShootData) {
        val name = if (shootData.image_category == "360int")
            sku?.skuName?.uppercase() + "_" + sku?.uuid + "_360int_1.JPG"
        else{
            if (shootData.name.contains(".")) shootData.name else shootData.name + "." + shootData.capturedImage.substringAfter(
                "."
            )
        }

        val newImage = com.spyneai.shoot.repository.model.image.Image(
            uuid = getUuid(),
            projectUuid = project?.uuid,
            skuUuid = sku?.uuid,
            image_category = shootData.image_category,
            skuName = sku?.skuName,
            name = name,
            sequence = shootData.sequence,
            overlayId = overlayId.toString(),
            isReclick = isReclick,
            isReshoot = isReshoot,
            path = shootData.capturedImage,
            angle = shootData.angle,
            tags = shootData.meta,
            debugData = shootData.debugData
        )

        if (imageRepositoryV2.isImageExist(newImage.skuUuid!!, newImage.name!!)) {
            imageRepositoryV2.updateImage(newImage)
        } else {
            localRepository.insertImage(
                newImage
            )
        }
    }


    fun updateVideoSku(
        skuId: String,
        prodSubCatId: String,
        initialImageCount: Int
    ) = viewModelScope.launch {
        _updateVideoSkuRes.value = Resource.Loading
        _updateVideoSkuRes.value = repository.updateVideoSku(skuId, prodSubCatId, initialImageCount)
    }

    suspend fun insertSku() {
        localRepository.insertSku(sku!!,project!!)
    }



    suspend fun insertProject(): Long {
        return localRepository.insertProject(project!!)
    }

    suspend fun updateSubcategory() {
        sku?.isSelectAble = true
        localRepository.updateSubcategory(project!!,sku!!)
    }



    fun getImagesbySkuId(skuId: String) = imageRepositoryV2.getImagesBySkuId(skuId)

    fun updateProjectStatus() = localRepository.updateProjectToOngoing(project?.uuid!!)


    fun updateFootwearSubcategory(
    ) = viewModelScope.launch {
        _updateFootwearSubcatRes.value = Resource.Loading
        _updateFootwearSubcatRes.value = repository.updateFootwearSubcategory(
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString(),
            sku?.skuId!!,
            exterirorAngles.value!!,
            subCategory.value?.prod_sub_cat_id!!
        )
    }

    fun updateVideoSkuLocally() {
        localRepository.updateVideoSkuLocally(sku!!)
    }


    fun getFileName(
        interiorSize: Int?,
        miscSize: Int?,
    ): String {
        return if (isReshoot) {
            reshotImageName
        } else {
            val filePrefix = FileNameManager().getFileName(
                if (categoryDetails.value?.imageType == "Misc") "Focus Shoot" else categoryDetails.value?.imageType!!,
                currentShoot,
                shootList.value,
                interiorSize,
                miscSize
            )

            sku?.skuName?.uppercase() + "_" + sku?.uuid + "_" + filePrefix
        }
    }

    fun getSequenceNumber(exteriorSize: Int, interiorSize: Int, miscSize: Int): Int {
        return if (isReshoot)
            reshootSequence
        else SequeneNumberManager().getSequenceNumber(
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
        val response = (subCategoriesResponse.value as Resource.Success).value

        GlobalScope.launch(Dispatchers.IO) {
            val MiscList = getMiscList()
            if (!MiscList.isNullOrEmpty()) {
                response.miscellaneous = MiscList
                GlobalScope.launch(Dispatchers.Main) {
                    showMiscDialog.value = true
                }
                return@launch
            }

            GlobalScope.launch(Dispatchers.Main) {
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
        if (getCurrentShoot() == null) {
            Log.d(TAG, "setSelectedItem: " + overlayId)
        } else {
            when (categoryDetails.value?.imageType) {
                "Exterior", "Footwear", "Ecom" -> {
                    val list = thumbnails as List<OverlaysResponse.Overlays>

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

    suspend fun updateSkuExteriorAngles() {
        localRepository.updateSkuExteriorAngles(sku!!)
    }

    fun updateSkuExteriorAngles(skuId: String, angles: Int, subcatId: String) {
        UpdateExteriorAngles(skuId, angles, subcatId).update()
       // localRepository.updateSkuExteriorAngles(skuId, angles)
    }

    fun getCameraSetting(): CameraSettings {
        return if (Utilities.getPreference(
                BaseApplication.getContext(),
                AppConstants.ENTERPRISE_ID
            ) == AppConstants.KARVI_ENTERPRISE_ID
        ) {
            CameraSettings().apply {
                isGryroActive = Utilities.getBool(
                    BaseApplication.getContext(),
                    categoryDetails.value?.categoryId + AppConstants.SETTING_STATUS_GYRO, true
                )
                isOverlayActive = Utilities.getBool(
                    BaseApplication.getContext(),
                    categoryDetails.value?.categoryId + AppConstants.SETTING_STATUS_OVERLAY, false
                )
                isGridActive = Utilities.getBool(
                    BaseApplication.getContext(),
                    categoryDetails.value?.categoryId + AppConstants.SETTING_STATUS_GRID, true
                )
            }
        } else {
            CameraSettings().apply {
                isGryroActive = Utilities.getBool(
                    BaseApplication.getContext(),
                    categoryDetails.value?.categoryId + AppConstants.SETTING_STATUS_GYRO, true)
                isOverlayActive = Utilities.getBool(
                    BaseApplication.getContext(),
                    categoryDetails.value?.categoryId + AppConstants.SETTING_STATUS_OVERLAY, true)
                isGridActive = Utilities.getBool(
                    BaseApplication.getContext(),
                    categoryDetails.value?.categoryId + AppConstants.SETTING_STATUS_GRID, false)
            }
        }
    }

    suspend fun setProjectAndSkuData(projectUuid: String, skuUuid: String) {
        project = getProject(projectUuid)
        sku = localRepository.getSkuById(skuUuid)
    }

    suspend fun getProject(projectUuid: String) = localRepository.getProject(projectUuid)

    fun checkInteriorShootStatus() {
        val response = (subCategoriesResponse.value as Resource.Success).value

        GlobalScope.launch(Dispatchers.IO) {
            val interiorList = getInteriorList()

            if (!interiorList.isNullOrEmpty()){
                response.interior = interiorList
                GlobalScope.launch(Dispatchers.Main) {
                    showInteriorDialog.value = true
                }
                return@launch
            }

            val MiscList =getMiscList()
            if (!MiscList.isNullOrEmpty()){
                response.miscellaneous = MiscList
                GlobalScope.launch(Dispatchers.Main) {
                    showMiscDialog.value = true
                }
                return@launch
            }

            GlobalScope.launch(Dispatchers.Main) {
                selectBackground(BaseApplication.getContext().getString(R.string.app_name))
            }
        }
    }

    suspend fun updateTotalFrames() =  localRepository.updateSkuTotalFrames(sku?.uuid!!,sku?.imagesCount!!)

    suspend fun updateBackground(backgroundId: Int,bgName: String = "") = localRepository.updateBackground(HashMap<String,Any>()
        .apply {
            put("project_uuid", sku!!.projectUuid!!)
            put("sku_uuid", sku!!.uuid!!)
            put("bg_id", backgroundId)
            put("bg_name", bgName!!)
            put("total_frames", getTotalFrames())
        })

    private fun getTotalFrames(): Int {
        return if (fromVideo) sku?.threeSixtyFrames?.plus(sku?.imagesCount!!)!! else sku?.imagesCount!!
    }

    fun getProjectSkus() = localRepository.getSkusByProjectId(project?.uuid!!)

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