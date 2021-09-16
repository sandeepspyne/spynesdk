import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.PorterDuff
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaActionSound
import android.os.*
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import android.view.Surface.ROTATION_90
import android.view.animation.AccelerateInterpolator
import android.widget.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.posthog.android.Properties
import com.spyneai.*
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.camera2.ShootDimensions
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentCameraBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.utils.log
import com.spyneai.shoot.utils.shoot
import kotlinx.android.synthetic.main.activity_credit_plans.*
import java.io.File
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class CameraFragment : BaseFragment<ShootViewModel, FragmentCameraBinding>(), PickiTCallbacks,
    SensorEventListener, View.OnTouchListener {
    private var imageCapture: ImageCapture? = null

    private var imageAnalyzer: ImageAnalysis? = null

    private lateinit var cameraExecutor: ExecutorService
    var pickIt: PickiT? = null
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private var flashMode: Int = ImageCapture.FLASH_MODE_OFF

    // Selector showing which camera is selected (front or back)
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    lateinit var file: File
    var haveGyrometer = false
    var isSensorAvaliable = false
    var rotation = 0
    var end: Long = 0
    var begin: Long = 0
    var mid: Long = 0

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
        private const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9
    }

    private var pitch = 0.0
    var roll = 0.0
    var azimuth = 0.0
    private var centerPosition = 0
    private var topConstraint = 0
    private var bottomConstraint = 0

    private var tiltUpperBound = -100
    private var tiltLowerBound = -80
    private var sideRotationMax = -5
    private var sideRotationMin = 5

    private lateinit var mSensorManager: SensorManager
    private var mAccelerometer: Sensor? = null
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null
    private var handler: Handler? = null
    private var isGyroOnCorrectAngle = false

    private var filename = ""

    private var cameraAngle = 45

    var gravity = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shoot("onCreate called(camera fragment)")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
        shoot("onCreateView called(camera fragment)")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        mSensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        handler = Handler()

        val activity: Activity? = activity

        handler!!.postDelayed({
            if (activity != null)
                startCamera()
        }, 300)

        cameraExecutor = Executors.newSingleThreadExecutor()
        // Determine the output directory
        pickIt = PickiT(requireContext(), this, requireActivity())

        viewModel.startInteriorShots.observe(viewLifecycleOwner, {

            if (it) binding.llSkip?.visibility = View.VISIBLE
        })

        viewModel.startMiscShots.observe(viewLifecycleOwner, {
            if (it) binding.llSkip?.visibility = View.VISIBLE
        })

        viewModel.showLeveler.observe(viewLifecycleOwner, {
            if (it && isSensorAvaliable) {
                if (viewModel.categoryDetails.value?.categoryName == "Footwear" ||
                    viewModel.categoryDetails.value?.categoryName == "E-Commerce"
                )
                    binding.tvLevelIndicator.visibility = View.GONE

                binding.flLevelIndicator.visibility = View.VISIBLE

                getPreviewDimensions(binding.ivGryroRing!!, 1)
                getPreviewDimensions(binding.tvCenter!!, 2)
            }
        })

        viewModel.hideLeveler.observe(viewLifecycleOwner, {
            if (it) {
                binding.flLevelIndicator.visibility = View.GONE
            }
        })

        if (getString(R.string.app_name) == AppConstants.KARVI) {
            binding.tvSkipShoot.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.secondary
                )
            )
            binding.ivSkip?.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.secondary),
                PorterDuff.Mode.SRC_IN
            )
        }
        binding.tvSkipShoot?.setOnClickListener {
            when (viewModel.categoryDetails.value?.imageType) {
                "Interior" -> {
                    if (viewModel.interiorShootNumber.value == viewModel.interiorAngles.value?.minus(
                            1
                        )
                    ) {
                        checkMiscShootStatus()
                    } else {
                        viewModel.interiorShootNumber.value =
                            viewModel.interiorShootNumber.value!! + 1
                    }
                }

                "Focus Shoot" -> {
                    if (viewModel.miscShootNumber.value == viewModel.miscAngles.value?.minus(1)) {
                        selectBackground()
                    } else {
                        viewModel.miscShootNumber.value = viewModel.miscShootNumber.value!! + 1
                    }
                }
            }
        }

        viewModel.onVolumeKeyPressed.observe(viewLifecycleOwner, {
            when (viewModel.categoryDetails.value?.categoryName) {
                "Automobiles", "Footwear" -> {
                    if (viewModel.subCategory.value?.prod_sub_cat_id != null)
                        onCaptureClick()
                    else {
                        var s = ""
                    }
                }

                "E-Commerce" -> {
                    if (viewModel.sku.value?.skuId != null)
                        onCaptureClick()
                }
            }
        })
    }

    private fun checkMiscShootStatus() {
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    when {
                        it.value.miscellaneous.isNotEmpty() -> {
                            viewModel.showMiscDialog.value = true
                        }
                        else -> {
                            selectBackground()
                        }
                    }
                }
                else -> {
                }
            }
        })
    }

    private fun selectBackground() {
        if (getString(R.string.app_name) == AppConstants.OLA_CABS)
            viewModel.show360InteriorDialog.value = true
        else
            viewModel.selectBackground.value = true
    }

    override fun onResume() {
        super.onResume()
        shoot("onResume called(camera fragment)")

        val mAccelerometer =
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
                mSensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    SensorManager.SENSOR_DELAY_UI
                )
            }

        val magneticField =
            mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
                mSensorManager.registerListener(
                    this,
                    magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    SensorManager.SENSOR_DELAY_UI
                )
            }

        if (mAccelerometer != null && magneticField != null)
            isSensorAvaliable = true
    }

    override fun onDestroy() {
        mSensorManager.unregisterListener(this)
        shoot("onDistroy called(overlay fragment)")
        super.onDestroy()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        shoot("onActivityCreated called(overlay fragment)")


        binding.cameraCaptureButton.setOnClickListener {
            onCaptureClick()
        }
    }

    private fun onCaptureClick() {
        when (getString(R.string.app_name)) {
            AppConstants.KARVI,
            AppConstants.CARS24,
            AppConstants.CARS24_INDIA,
            AppConstants.SWEEP,
            AppConstants.TRUSTED_CARS,
            AppConstants.TRAVO_PHOTOS,
            AppConstants.YALLA_MOTOS -> {
                if (viewModel.shootList.value == null
                    && !requireActivity().intent.getBooleanExtra(AppConstants.SKU_CREATED, false)
                ) {
                    if (binding.flLevelIndicator.visibility == View.VISIBLE) {
                        if (isGyroOnCorrectAngle)
                            getProjectDetails()
                    } else {
                        getProjectDetails()
                    }
                } else {
                    if (binding.flLevelIndicator.visibility == View.VISIBLE) {
                        if (isGyroOnCorrectAngle)
                            captureImage()
                    } else {
                        captureImage()
                    }
                }
            }
            AppConstants.FLIPKART,
            AppConstants.UDAAN,
            AppConstants.LAL_10,
            AppConstants.AMAZON,
            AppConstants.SWIGGY,
            AppConstants.SWIGGYINSTAMART -> {
                if (binding.flLevelIndicator.visibility == View.VISIBLE) {
                    if (isGyroOnCorrectAngle)
                        captureImage()
                } else {
                    captureImage()
                }
            }
            AppConstants.SPYNE_AI -> {
                if (viewModel.shootList.value == null
                    && !requireActivity().intent.getBooleanExtra(AppConstants.SKU_CREATED, false)
                ) {
                    if (viewModel.categoryDetails.value?.categoryName == "Automobiles" ||
                        viewModel.categoryDetails.value?.categoryName == "Bikes"
                    ) {
                        if (binding.flLevelIndicator.visibility == View.VISIBLE) {
                            if (isGyroOnCorrectAngle) {
                                getProjectDetails()
                            }
                        } else {
                            getProjectDetails()
                        }
                    } else {
                        if (binding.flLevelIndicator.visibility == View.VISIBLE) {
                            if (isGyroOnCorrectAngle)
                                captureImage()
                        } else {
                            captureImage()
                        }
                    }
                } else {
                    if (binding.flLevelIndicator.visibility == View.VISIBLE) {
                        if (isGyroOnCorrectAngle)
                            captureImage()
                    } else {
                        captureImage()
                    }
                }
            }
            else
            -> {
                if (viewModel.shootList.value == null
                    && !requireActivity().intent.getBooleanExtra(AppConstants.SKU_CREATED, false)
                ) {
                    getProjectDetails()
                } else {
                    captureImage()
                }
            }
        }
    }


    private fun getProjectDetails() {
        viewModel.createProjectRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    val subCategory = viewModel.subCategory.value
                    createSku(
                        it.value.project_id,
                        subCategory?.prod_sub_cat_id.toString()
                    )
                }
                else -> {
                }
            }
        })
    }

    private fun createSku(projectId: String, prod_sub_cat_id: String) {
        Utilities.showProgressDialog(requireContext())
        viewModel.isCameraButtonClickable = false

        viewModel.createSku(
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString(),
            projectId,
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
            prod_sub_cat_id!!,
            viewModel.sku.value?.skuName.toString(),
            viewModel.exterirorAngles.value!!
        )

        viewModel.createSkuRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    BaseApplication.getContext().captureEvent(
                        Events.CREATE_SKU,
                        Properties().putValue("sku_name", viewModel.sku.value?.skuName.toString())
                            .putValue("project_id", projectId)
                            .putValue("prod_sub_cat_id", prod_sub_cat_id)
                            .putValue("angles", viewModel.exterirorAngles.value!!)
                    )


                    val sku = viewModel.sku.value
                    sku?.skuId = it.value.sku_id
                    sku?.projectId = projectId
                    sku?.createdOn = System.currentTimeMillis()
                    sku?.totalImages = viewModel.exterirorAngles.value
                    sku?.categoryName = viewModel.categoryDetails.value?.categoryName
                    sku?.categoryId = viewModel.categoryDetails.value?.categoryId
                    sku?.subcategoryName = viewModel.subCategory.value?.sub_cat_name
                    sku?.subcategoryId = prod_sub_cat_id
                    sku?.exteriorAngles = viewModel.exterirorAngles.value

                    viewModel.sku.value = sku
                    viewModel.isSubCategoryConfirmed.value = true


                    //add sku to local database
                    viewModel.insertSku(sku!!)

                    viewModel.isCameraButtonClickable = true
                    captureImage()
                }


                is Resource.Failure -> {
                    viewModel.isCameraButtonClickable = true
                    BaseApplication.getContext().captureFailureEvent(
                        Events.CREATE_SKU_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it) { createSku(projectId, prod_sub_cat_id) }
                }
            }
        })
    }

    private fun captureImage() {
        //ThreeSixtyInteriorHintDialog().show(requireActivity().supportFragmentManager,"ThreeSixtyInteriorHintDialog")

        if (viewModel.isCameraButtonClickable) {
            takePhoto()
            log("shoot image button clicked")
            viewModel.isCameraButtonClickable = false
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            val a: Activity? = activity
            if (a != null) a.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        var cameraProvider: ProcessCameraProvider
        cameraProviderFuture.addListener({

            // Used to bind the lifecycle of cameras to the lifecycle owner
            try {
                cameraProvider = cameraProviderFuture.get()
            } catch (e: InterruptedException) {
                Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT).show()
                return@addListener
            } catch (e: ExecutionException) {
                Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT).show()
                return@addListener
            }

            // The display information
            //val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }
            // The ratio for the output image and preview
            var height = 0
            var width = 0
            val displayMetrics = DisplayMetrics()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                if (requireContext() != null) {
                    requireContext().display?.getRealMetrics(displayMetrics)
                    height = displayMetrics.heightPixels
                    width = displayMetrics.widthPixels
                }

            } else {
                if (requireActivity() != null) {
                    requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
                    height = displayMetrics.heightPixels
                    width = displayMetrics.widthPixels
                }
            }
            val aspectRatio = aspectRatio(width, height)
            // The display rotation
            //val rotation = binding.viewFinder.display.rotation


            val localCameraProvider = cameraProvider
                ?: throw IllegalStateException("Camera initialization failed.")
            var size = Size(1024, 768)

            // Preview
            val preview = when (viewModel.categoryDetails.value?.categoryName) {
                "Automobiles", "Bikes" -> {
                    if (getString(R.string.app_name) == AppConstants.KARVI) {
                        Preview.Builder()
                            .setTargetAspectRatio(aspectRatio)
                            .setTargetResolution(size)
                            .build()
                            .also {
                                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                            }
                    } else {
                        Preview.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                            .build()
                            .also {
                                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                            }
                    }
                }
                "E-Commerce", "Food & Beverages", "Footwear" -> {
                    Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build()
                        .also {
                            it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                        }
                }
                else -> {
                    Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build()
                        .also {
                            it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                        }
                }
            }

            //for exact image cropping
            val viewPort = binding.viewFinder?.viewPort

            imageCapture = when (viewModel.categoryDetails.value?.categoryName) {
                "Automobiles", "Bikes" -> {
                    if (getString(R.string.app_name) == AppConstants.KARVI) {
                        ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .setFlashMode(flashMode)
                            .setTargetResolution(size)
                            .build()
                    } else {
                        ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .setFlashMode(flashMode)
                            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                            .build()
                    }
                }
                "E-Commerce", "Food & Beverages", "Footwear" -> {
                    ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setFlashMode(flashMode)
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setTargetRotation(ROTATION_90)
                        .build()
                }
                else -> {
                    ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setFlashMode(flashMode)
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setTargetRotation(ROTATION_90)
                        .build()
                }
            }

            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageCapture!!)
                .build()

            //   The Configuration of image analyzing
            imageAnalyzer = when(viewModel.categoryDetails.value?.categoryName){
                "Automobiles", "Bikes" -> {
                    if (getString(R.string.app_name) == AppConstants.KARVI) {
                ImageAnalysis.Builder()
                    .setTargetResolution(size)
                    .build()
            } else {
                ImageAnalysis.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()
            }
                }
                "E-Commerce", "Food & Beverages", "Footwear" -> {
                    ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build()
                }

                else -> {
                    ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build()
            }
            }


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


            // Unbind use cases before rebinding
            cameraProvider.unbindAll()
            try {
                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    useCaseGroup
                )

                cameraControl = camera.cameraControl



                cameraInfo = camera.cameraInfo

//                val rotation = cameraInfo!!.sensorRotationDegrees
//                Toast.makeText(requireContext(), "rotation- "+rotation, Toast.LENGTH_SHORT).show()

                var currentZoomRatio = cameraInfo?.zoomState?.value?.zoomRatio ?: 0F
                when (viewModel.categoryDetails.value?.categoryName) {
                    "E-Commerce" -> {
                        if (currentZoomRatio == 1.0F)
                            cameraControl?.setZoomRatio(currentZoomRatio * 1.5F)
                    }
                    "Food & Beverages" -> {
                        if (currentZoomRatio == 1.0F)
                            cameraControl?.setZoomRatio(currentZoomRatio * 1.2F)
                    }
                }
                binding.viewFinder.setOnTouchListener(this)

                if (viewModel.shootDimensions.value == null ||
                    viewModel.shootDimensions.value?.previewHeight == 0
                ) {
                    getPreviewDimensions(binding.viewFinder!!, 0)
                    getPreviewDimensions(binding.llCapture!!, 3)
                }
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                val properties = Properties()
                properties["error"] = exc?.localizedMessage
                properties["category"] = viewModel.categoryDetails.value?.categoryName

                BaseApplication.getContext().captureEvent(
                    Events.OVERLAY_CAMERA_FIALED,
                    properties
                )
            }

        }, ContextCompat.getMainExecutor(requireContext()))

    }

    override fun onDestroyView() {
        super.onDestroyView()
        shoot("onDestroyView called(overlay fragment)")

        // Shut down our background executor
        cameraExecutor.shutdown()

    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        if (getString(R.string.app_name) == "Swiggy" ||
            getString(R.string.app_name) == AppConstants.KARVI
        )
            return AspectRatio.RATIO_4_3

        return AspectRatio.RATIO_16_9
    }

    private fun takePhoto() {
        begin = System.currentTimeMillis()
        viewModel.begin.value = begin
        // Get a stable reference of the modifiable image capture use case
        val imageCapture1 = imageCapture ?: return


        // Setup image capture metadata
        val metadata = ImageCapture.Metadata().apply {
            // Mirror image when using the front camera
            isReversedHorizontal = lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA
        }

        // The Folder location where all the files will be stored
        val outputDirectory: String by lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${Environment.DIRECTORY_DCIM}/Spyne/"
            } else {
                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/Spyne/"
            }
        }
        filename = viewModel.sku.value?.skuName + "_" + viewModel.sku.value?.skuId + "_"

        if (viewModel.fromDrafts) {
            filename += when (viewModel.categoryDetails.value?.imageType) {
                "Exterior" -> {
                    viewModel.categoryDetails.value?.imageType!! + "_" + viewModel.shootNumber.value?.plus(
                        1
                    )
                }
                "Interior" -> {
                    val list = viewModel.shootList.value

                    val interiorList = list?.filter {
                        it.image_category == "Interior"
                    }

                    if (interiorList == null) {
                        viewModel.categoryDetails.value?.imageType!! + "_" +
                                requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE, 0)
                                    .plus(1)
                    } else {
                        viewModel.categoryDetails.value?.imageType!! + "_" +
                                requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE, 0)
                                    .plus(interiorList.size.plus(1))
                    }

                }
                "Focus Shoot" -> {
                    val list = viewModel.shootList.value

                    val miscList = list?.filter {
                        it.image_category == "Focus Shoot"
                    }

                    if (miscList == null) {
                        "Miscellaneous_" +
                                requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE, 0)
                                    .plus(1)
                    } else {
                        "Miscellaneous_" +
                                requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE, 0)
                                    .plus(miscList.size.plus(1))
                    }
                }
                "Footwear" -> {
                    viewModel.categoryDetails.value?.imageType!! + "_" + viewModel.shootNumber.value?.plus(
                        1
                    )
                }
                "Food & Beverages" -> {
                    viewModel.categoryDetails.value?.imageType!! + "_" + viewModel.shootNumber.value?.plus(
                        1
                    )
                }
                "E-Commerce" -> {
                    viewModel.categoryDetails.value?.imageType!! + "_" + viewModel.shootNumber.value?.plus(
                        1
                    )
                }
                else -> {
                    System.currentTimeMillis().toString()
                }
            }
        } else {
            filename += if (viewModel.shootList.value == null)
                viewModel.categoryDetails.value?.imageType + "_1"
            else {
                val size = viewModel.shootList.value!!.size.plus(1)
                val list = viewModel.shootList.value

                when (viewModel.categoryDetails.value?.imageType) {
                    "Exterior" -> {
                        viewModel.categoryDetails.value?.imageType!! + "_" + size
                    }
                    "Interior" -> {

                        val interiorList = list?.filter {
                            it.image_category == "Interior"
                        }

                        if (interiorList == null) {
                            viewModel.categoryDetails.value?.imageType!! + "_1"
                        } else {
                            viewModel.categoryDetails.value?.imageType!! + "_" + interiorList.size.plus(
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
                        viewModel.categoryDetails.value?.imageType!! + "_" + viewModel.shootNumber.value?.plus(
                            1
                        )
                    }
                    "Food" -> {
                        viewModel.categoryDetails.value?.imageType!! + "_" + viewModel.shootNumber.value?.plus(
                            1
                        )
                    }
                    "Ecom" -> {
                        viewModel.categoryDetails.value?.imageType!! + "_" + viewModel.shootNumber.value?.plus(
                            1
                        )
                    }
                    else -> {
                        System.currentTimeMillis().toString()
                    }
                }
            }
        }

        // Options fot the output image file
        val outputOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, outputDirectory)
            }

            val contentResolver = requireContext().contentResolver

            // Create the output uri
            val contentUri =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            ImageCapture.OutputFileOptions.Builder(contentResolver, contentUri, contentValues)
        } else {
            File(outputDirectory).mkdirs()
            file = File(outputDirectory, "${filename}.jpg")

            ImageCapture.OutputFileOptions.Builder(file)
        }.setMetadata(metadata).build()


        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture1.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    viewModel.isCameraButtonClickable = true
                    log("Photo capture failed: " + exc.message)

                    Toast.makeText(
                        requireContext(),
                        "Photo capture failed: " + exc.message,
                        Toast.LENGTH_LONG
                    ).show()

                    Utilities.hideProgressDialog()

                    BaseApplication.getContext().captureFailureEvent(
                        Events.IMAGE_CAPRURE_FAILED,
                        Properties(),
                        exc.localizedMessage
                    )
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    if (output.savedUri == null) {
                        if (file != null)
                            mid = System.currentTimeMillis()
                        val difference = (mid - begin) / 1000.toFloat()
                        Toast.makeText(
                            requireContext(),
                            "image clicked- " + difference,
                            Toast.LENGTH_LONG
                        ).show()
                        log("onImageSaved- " + difference)
                        addShootItem(file.path)
                    } else {
                        try {
                            mid = System.currentTimeMillis()
                            val difference = (mid - begin) / 1000.toFloat()
                            Toast.makeText(
                                requireContext(),
                                "image clicked- " + difference,
                                Toast.LENGTH_LONG
                            ).show()
                            log("onImageSaved2- " + difference)
                            var file = output.savedUri!!.toFile()
                            addShootItem(file.path)
                        } catch (ex: IllegalArgumentException) {
                            pickIt?.getPath(output.savedUri, Build.VERSION.SDK_INT)
                        }
                    }
                }
            })
    }

    override fun onSensorChanged(event: SensorEvent?) {
        //Get Rotation Vector Sensor Values

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        if (isSensorAvaliable && viewModel.showLeveler.value == true) {
            updateOrientationAngles()
        } else {
            binding.flLevelIndicator.visibility = View.GONE
        }
    }

    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // "orientationAngles" now has up-to-date information.

        //binding.tvAzimuth.text = "Azimuth ${Math.toDegrees(orientationAngles[0].toDouble())}"

        val movearrow = abs(Math.toDegrees(orientationAngles[2].toDouble()).roundToInt()) - abs(
            roll.roundToInt()
        ) >= 1

        val rotatedarrow = abs(Math.toDegrees(orientationAngles[1].toDouble()).roundToInt()) - abs(
            pitch.roundToInt()
        ) >= 1


        pitch = Math.toDegrees(orientationAngles[1].toDouble())
        roll = Math.toDegrees(orientationAngles[2].toDouble())
        azimuth = (orientationAngles[0] * 180 / Math.PI.toFloat()).toDouble()

        when (getString(R.string.app_name)) {
            AppConstants.KARVI -> {
                if ((roll >= -95 && roll <= -85) && (pitch >= -5 && pitch <= 5)) {
                    gyroMeterOnLevel(true)

                } else {
                    gyroMeterOffLevel()

                    if (movearrow)
                        moveArrow(roll + 90)

                    if (rotatedarrow) {
                        if (pitch > 0) {
                            rotateArrow(pitch.minus(0).roundToInt())
                        } else {
                            rotateArrow(pitch.plus(0).roundToInt())
                        }
                    }
                }
            }

            /* AppConstants.UDAAN,
             AppConstants.FLIPKART,
             AppConstants.AMAZON,
             AppConstants.LAL_10 -> {
                 //hide moving line
                 if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                     binding.tvLevelIndicator.visibility = View.GONE
                 else
                     binding.tvLevelIndicator.visibility = View.VISIBLE

                 if ((pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3)) ||
                     pitch.roundToInt() <= -82 && pitch.roundToInt() >= -88 ||
                     (pitch.roundToInt() <= -40 && pitch.roundToInt() >= -45) && abs(roll.roundToInt()) < 100
                 ) {
                     if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                         gyroMeterOnLevel(false)
                     else if (pitch.roundToInt() <= -40 && pitch.roundToInt() >= -45)
                         gyroMeterOnLevel(false)
                     else
                         gyroMeterOnLevel(true)
                 } else {
                     gyroMeterOffLevel()

                     if (movearrow) {
                         if (abs(roll.roundToInt()) < 100) {
                             moveArrow((pitch + 85).unaryMinus())
                         } else {
                             moveArrow(pitch + 85)
                         }
                     }

                     if (orientationAngles[2].roundToInt() == 1 || orientationAngles[2].roundToInt() == -1) {
                         if (orientationAngles[2].roundToInt() == 1) {
                             rotateArrow((pitch + 85).unaryMinus().roundToInt())
                         } else {
                             rotateArrow((pitch + 85).roundToInt())
                         }
                     }
                 }
             }*/
            AppConstants.SWIGGY -> {
                //hide moving line
                if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                    binding.tvLevelIndicator.visibility = View.GONE
                else
                    binding.tvLevelIndicator.visibility = View.VISIBLE

                if ((pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3)) ||
                    pitch.roundToInt() <= -82 && pitch.roundToInt() >= -88 ||
                    (pitch.roundToInt() <= -40 && pitch.roundToInt() >= -45) && abs(roll.roundToInt()) < 100
                ) {
                    isGyroOnCorrectAngle = true
                    //angle 90
                    if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3)) {
                        cameraAngle = 90
                        gyroMeterOnLevel(false)
                    }
                    //angle 45
                    else if (pitch.roundToInt() <= -40 && pitch.roundToInt() >= -45) {
                        cameraAngle = 45
                        gyroMeterOnLevel(false)
                        // angle 0
                    } else {
                        cameraAngle = 0
                        gyroMeterOnLevel(true)
                    }
                } else {
                    isGyroOnCorrectAngle = false
                    gyroMeterOffLevel()

                    if (movearrow) {
                        if (abs(roll.roundToInt()) < 100) {
                            moveArrow((pitch + 85).unaryMinus())
                        } else {
                            moveArrow(pitch + 85)
                        }
                    }

                    if (orientationAngles[2].roundToInt() == 1 || orientationAngles[2].roundToInt() == -1) {
                        if (orientationAngles[2].roundToInt() == 1) {
                            rotateArrow((pitch + 85).unaryMinus().roundToInt())
                        } else {
                            rotateArrow((pitch + 85).roundToInt())
                        }
                    }
                }
            }
            AppConstants.SPYNE_AI -> {
                when (viewModel.categoryDetails.value?.categoryName) {
                    "Automobiles", "Bikes" -> {
                        if ((roll >= -100 && roll <= -80) && (pitch >= -5 && pitch <= 5)) {
                            gyroMeterOnLevel(true)
                        } else {
                            gyroMeterOffLevel()

                            if (movearrow)
                                moveArrow(roll + 90)

                            if (rotatedarrow) {
                                if (pitch > 0) {
                                    rotateArrow(pitch.minus(0).roundToInt())
                                } else {
                                    rotateArrow(pitch.plus(0).roundToInt())
                                }
                            }
                        }
                    }

                    else -> {
                        //hide moving line
                        if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                            binding.tvLevelIndicator.visibility = View.GONE
                        else
                            binding.tvLevelIndicator.visibility = View.VISIBLE

                        if ((pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3)) ||
                            pitch.roundToInt() <= -82 && pitch.roundToInt() >= -88 ||
                            (pitch.roundToInt() <= -40 && pitch.roundToInt() >= -45) && abs(roll.roundToInt()) < 100
                        ) {
                            if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                                gyroMeterOnLevel(false)
                            else if (pitch.roundToInt() <= -40 && pitch.roundToInt() >= -45)
                                gyroMeterOnLevel(false)
                            else
                                gyroMeterOnLevel(true)
                        } else {
                            gyroMeterOffLevel()

                            if (movearrow) {
                                if (abs(roll.roundToInt()) < 100) {
                                    moveArrow((pitch + 85).unaryMinus())
                                } else {
                                    moveArrow(pitch + 85)
                                }
                            }

                            if (orientationAngles[2].roundToInt() == 1 || orientationAngles[2].roundToInt() == -1) {
                                if (orientationAngles[2].roundToInt() == 1) {
                                    rotateArrow((pitch + 85).unaryMinus().roundToInt())
                                } else {
                                    rotateArrow((pitch + 85).roundToInt())
                                }
                            }
                        }
                    }
                }
            }
            AppConstants.SWIGGYINSTAMART,
            AppConstants.UDAAN,
            AppConstants.FLIPKART,
            AppConstants.AMAZON,
            AppConstants.LAL_10 -> {
                //hide moving line
                if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                    binding.tvLevelIndicator.visibility = View.GONE
                else
                    binding.tvLevelIndicator.visibility = View.VISIBLE

                if ((pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3)) ||
                    pitch.roundToInt() <= -82 && pitch.roundToInt() >= -88
                ) {

                    if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                        gyroMeterOnLevel(false)
                    else
                        gyroMeterOnLevel(true)
                } else {
                    gyroMeterOffLevel()

                    if (movearrow) {
                        if (abs(roll.roundToInt()) < 100) {
                            moveArrow((pitch + 85).unaryMinus())
                        } else {
                            moveArrow(pitch + 85)
                        }
                    }

                    if (orientationAngles[2].roundToInt() == 1 || orientationAngles[2].roundToInt() == -1) {
                        if (orientationAngles[2].roundToInt() == 1) {
                            rotateArrow((pitch + 85).unaryMinus().roundToInt())
                        } else {
                            rotateArrow((pitch + 85).roundToInt())
                        }
                    }
                }
            }
            else -> {
                if ((roll >= -100 && roll <= -80) && (pitch >= -5 && pitch <= 5)) {
                    gyroMeterOnLevel(true)
                } else {
                    gyroMeterOffLevel()

                    if (movearrow)
                        moveArrow(roll + 90)

                    if (rotatedarrow) {
                        if (pitch > 0) {
                            rotateArrow(pitch.minus(0).roundToInt())
                        } else {
                            rotateArrow(pitch.plus(0).roundToInt())
                        }
                    }
                }
            }
        }
    }

    private fun gyroMeterOffLevel() {
        isGyroOnCorrectAngle = false
        binding.ivTopLeft?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )
        binding.ivBottomLeft?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )

        binding.ivGryroRing?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )
        binding.tvLevelIndicator?.background = ContextCompat.getDrawable(
            BaseApplication.getContext(),
            R.drawable.bg_gyro_error
        )

        binding.ivTopRight?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )
        binding.ivBottomRight?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )
    }

    private fun gyroMeterOnLevel(removeAnimation: Boolean) {
        isGyroOnCorrectAngle = true
        if (removeAnimation) {
            binding
                .tvLevelIndicator
                ?.animate()
                ?.translationY(0f)
                ?.setInterpolator(AccelerateInterpolator())?.duration = 0
        }


        binding.tvLevelIndicator?.rotation = 0f

        binding.ivTopLeft?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )
        binding.ivBottomLeft?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )

        binding.ivGryroRing?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )
        binding.tvLevelIndicator?.background = ContextCompat.getDrawable(
            BaseApplication.getContext(),
            R.drawable.bg_gyro_level
        )

        binding.ivTopRight?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )
        binding.ivBottomRight?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )
    }

    private fun rotateArrow(roundToInt: Int) {
        binding.tvLevelIndicator?.rotation = roundToInt.toFloat()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun moveArrow(newRoll: Double) {
        if (newRoll > 0 && (centerPosition + newRoll) < bottomConstraint) {
            binding
                .tvLevelIndicator
                ?.animate()
                ?.translationY(newRoll.toFloat())
                ?.setInterpolator(AccelerateInterpolator())?.duration = 0
        }

        if (newRoll < 0 && (centerPosition - newRoll) > topConstraint) {
            binding
                .tvLevelIndicator
                ?.animate()
                ?.translationY(newRoll.toFloat())
                ?.setInterpolator(AccelerateInterpolator())?.duration = 0
        }
    }

    private fun getPreviewDimensions(view: View, type: Int) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                when (type) {
                    0 -> {
                        val shootDimensions = ShootDimensions()
                        shootDimensions.previewWidth = view.width
                        shootDimensions.previewHeight = view.height

                        viewModel.shootDimensions.value = shootDimensions
                    }

                    1 -> {
                        topConstraint = view.top
                        bottomConstraint = topConstraint + view.height
                    }

                    2 -> {
                        centerPosition = view.top
                    }
                }

            }
        })
    }

    private fun addShootItem(capturedImage: String) {
        end = System.currentTimeMillis()
        val difference = (end - begin) / 1000.toFloat()
        Toast.makeText(requireContext(), "addShootIteamCalled- " + difference, Toast.LENGTH_LONG)
            .show()
        log("addShootIteamCalled- " + difference)
        viewModel.showConfirmReshootDialog.value = true

        //play shutter sound
        val sound = MediaActionSound()
        sound.play(MediaActionSound.SHUTTER_CLICK)

        if (viewModel.shootList.value == null) {
            Utilities.hideProgressDialog()
            Utilities.hideProgressDialog()
            viewModel.shootList.value = ArrayList()
        }

        var sequenceNumber: Int = 0
        if (viewModel.fromDrafts) {
            when (viewModel.categoryDetails.value?.imageType) {
                "Exterior" -> {
                    sequenceNumber = viewModel.shootNumber.value?.plus(1)!!
                }
                "Interior" -> {
                    sequenceNumber =
                        requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE, 0)
                            .plus(
                                requireActivity().intent.getIntExtra(
                                    AppConstants.INTERIOR_SIZE,
                                    0
                                )
                            )
                            .plus(viewModel.shootList.value!!.size.plus(1))

                    Log.d(
                        TAG,
                        "addShootItem: " + requireActivity().intent.getIntExtra(
                            AppConstants.EXTERIOR_SIZE,
                            0
                        )
                    )
                    Log.d(
                        TAG,
                        "addShootItem: " + requireActivity().intent.getIntExtra(
                            AppConstants.INTERIOR_SIZE,
                            0
                        )
                    )
                    Log.d(TAG, "addShootItem: " + viewModel.shootList.value!!.size.plus(1))
                    Log.d(TAG, "addShootItem: " + sequenceNumber)
                }
                "Focus Shoot" -> {
                    sequenceNumber =
                        requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE, 0)
                            .plus(
                                requireActivity().intent.getIntExtra(
                                    AppConstants.INTERIOR_SIZE,
                                    0
                                )
                            )
                            .plus(requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE, 0))
                            .plus(viewModel.shootList.value!!.size.plus(1))

                    Log.d(TAG, "addShootItem: " + sequenceNumber)
                }
                "Footwear" -> {
                    sequenceNumber = viewModel.shootNumber.value?.plus(1)!!
                }
                "Food & Beverages" -> {
                    sequenceNumber = viewModel.shootNumber.value?.plus(1)!!
                }
                "E-Commerce" -> {
                    sequenceNumber = viewModel.shootNumber.value?.plus(1)!!
                }
            }
        } else {
            sequenceNumber = viewModel.shootList.value!!.size.plus(1)
        }


        viewModel.shootList.value!!.add(
            ShootData(
                capturedImage,
                viewModel.projectId.value!!,
                viewModel.sku.value?.skuId!!,
                viewModel.categoryDetails.value?.imageType!!,
                Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY)
                    .toString(),
                sequenceNumber,
                cameraAngle
            )
        )

        viewModel.shootList.value = viewModel.shootList.value

        val properties = Properties()
        properties.apply {
            this["project_id"] = viewModel.projectId.value!!
            this["sku_id"] = viewModel.sku.value?.skuId!!
            this["image_type"] = viewModel.categoryDetails.value?.imageType!!
        }

        BaseApplication.getContext().captureEvent(Events.IMAGE_CAPTURED, properties)
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCameraBinding.inflate(inflater, container, false)

    override fun PickiTonUriReturned() {

    }

    override fun PickiTonStartListener() {

    }

    override fun PickiTonProgressUpdate(progress: Int) {

    }

    override fun PickiTonCompleteListener(
        path: String?,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        addShootItem(path!!)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> return true

            MotionEvent.ACTION_UP -> {
                // Get the MeteringPointFactory from PreviewView
                val factory = binding.viewFinder.getMeteringPointFactory()

                // Create a MeteringPoint from the tap coordinates
                val point = factory.createPoint(event.x, event.y)


                // Create a MeteringAction from the MeteringPoint, you can configure it to specify the metering mode
                val action = FocusMeteringAction.Builder(point).build()

                // Trigger the focus and metering. The method returns a ListenableFuture since the operation
                // is asynchronous. You can use it get notified when the focus is successful or if it fails.
                if (cameraControl != null) {
                    val listenable = cameraControl!!.startFocusAndMetering(action)

                    val layout =
                        LayoutInflater.from(requireContext())
                            .inflate(R.layout.item_focus, null)
                    val ivFocus: ImageView = layout.findViewById(R.id.ivFocus)
                    //val tvExposure: TextView = layout.findViewById(R.id.tvExposure)

                    val rightSeekBar: SeekBar =
                        LayoutInflater.from(requireContext())
                            .inflate(R.layout.item_exposure, null) as SeekBar

                    var seekClicked = false
                    val seekWidth = (30 * resources.displayMetrics.density).toInt()

                    val width = (70 * resources.displayMetrics.density).toInt()
                    val height = (80 * resources.displayMetrics.density).toInt()

                    val params = FrameLayout.LayoutParams(width, height)
                    var seekParams =
                        FrameLayout.LayoutParams(
                            seekWidth,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        )

                    if (cameraInfo?.exposureState?.isExposureCompensationSupported == true) {
                        val exposureState = cameraInfo?.exposureState

                        rightSeekBar.max =
                            exposureState?.exposureCompensationRange?.upper?.times(10)!!

                        rightSeekBar.incrementProgressBy(1)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            rightSeekBar.setProgress(
                                exposureState?.exposureCompensationIndex?.times(
                                    10
                                )!!, false
                            )
                        }

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            rightSeekBar.min =
                                exposureState?.exposureCompensationRange?.lower?.times(
                                    10
                                )!!
                        }

                        //rightSeekBar.min = exposureState?.exposureCompensationRange?.lower!!

                        rightSeekBar?.setOnSeekBarChangeListener(object :
                            SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                seek: SeekBar,
                                progress: Int, fromUser: Boolean
                            ) {
                                if (!seekClicked) {
                                    seekClicked = true
                                    seekParams.width =
                                        (150 * resources.displayMetrics.density).toInt()
                                    seekParams.leftMargin = params.leftMargin + width / 5
                                    rightSeekBar.layoutParams = seekParams
                                }

                                ivFocus.animate().cancel()
                                rightSeekBar.animate().cancel()

                                cameraControl!!.setExposureCompensationIndex(
                                    progress.times(0.10).roundToInt()
                                )
                                //tvExposure.text = progress.times(0.10).roundToInt().toString()
                                // write custom code for progress is changed
                            }

                            override fun onStartTrackingTouch(seek: SeekBar) {
                                // write custom code for progress is started
                            }

                            override fun onStopTrackingTouch(seek: SeekBar) {
                                startFadeAnimation(ivFocus, rightSeekBar)
                            }
                        })
                    } else {
                        rightSeekBar.visibility = View.GONE
                    }

                    binding.flTapToFocus?.removeAllViews()

                    params.leftMargin = when {
                        event.x.roundToInt() - width / 2 <= width -> 5
                        event.x.roundToInt() - width / 2 + width >= viewModel.shootDimensions.value?.previewWidth!! -> {
                            viewModel.shootDimensions.value?.previewWidth!! - width + 15
                        }
                        else -> event.x.roundToInt() - width / 2
                    }

                    params.topMargin = when {
                        event.y.roundToInt() - height / 2 <= width -> 5
                        event.y.roundToInt() - height / 2 >= viewModel.shootDimensions.value?.previewHeight!! -> {
                            viewModel.shootDimensions.value?.previewHeight!! - height
                        }
                        else -> event.y.roundToInt() - height / 2
                    }

                    ivFocus.layoutParams = params


                    seekParams.leftMargin = params.leftMargin + width
                    seekParams.topMargin = params.topMargin + height / 3
                    rightSeekBar.layoutParams = seekParams

                    binding.flTapToFocus?.addView(layout)
                    binding.flTapToFocus?.addView(rightSeekBar)

                    startFadeAnimation(ivFocus, rightSeekBar)
                }

                return true
            }
            else ->                 // Unhandled event.
                return false
        }
        return true
    }

    private fun startFadeAnimation(ivFocus: ImageView, rightSeekBar: SeekBar) {
        handler?.removeCallbacksAndMessages(null)

        handler?.postDelayed({
            ivFocus.animate().alpha(0f).setDuration(1000)
                .setInterpolator(AccelerateInterpolator()).start()
            rightSeekBar.animate().alpha(0f).setDuration(1000)
                .setInterpolator(AccelerateInterpolator()).start()
        }, 2000)
    }

    override fun onStart() {
        super.onStart()
        shoot("onStart called(camera fragment)")
    }

    override fun onPause() {
        super.onPause()
        shoot("onPause called(camera fragment)")
    }

    override fun onStop() {
        super.onStop()
        shoot("onStop called(camera fragment)")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        shoot("onSaveInstanceState called(camera fragment)")
    }
}






