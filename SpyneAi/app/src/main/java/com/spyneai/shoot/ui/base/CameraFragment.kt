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
import com.spyneai.shoot.data.model.Sku
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
//    private var centerPosition = 0
//    private var topConstraint = 0
//    private var bottomConstraint = 0

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
                binding.gyroView.start(viewModel.categoryDetails.value?.categoryName!!)
            }
        })

        viewModel.hideLeveler.observe(viewLifecycleOwner, {
            if (it) {
                binding.gyroView.visibility = View.GONE
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
                android.graphics.PorterDuff.Mode.MULTIPLY
            );
        }

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

        binding.cameraCaptureButton.setOnClickListener {
            onCaptureClick()
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


    private fun onCaptureClick() {
        captureImage()
    }


    private fun getProjectDetails() {
        val createProjectRes = (viewModel.createProjectRes.value as Resource.Success).value

        if (viewModel.fromVideo) {
            updateSku(
                createProjectRes.project_id,
                requireActivity().intent.getStringExtra(AppConstants.SKU_ID)!!,
                requireActivity().intent.getStringExtra(AppConstants.SKU_NAME)!!,
                viewModel.subCategory.value?.prod_sub_cat_id!!
            )
        }
    }

    private fun updateSku(
        projectId: String,
        skuId: String,
        skuName: String,
        prod_sub_cat_id: String
    ) {
        Utilities.showProgressDialog(requireContext())

        viewModel.isCameraButtonClickable = false

        viewModel.updateVideoSku(
            requireActivity().intent.getStringExtra(AppConstants.SKU_ID)!!,
            viewModel.subCategory.value?.prod_sub_cat_id!!,
            viewModel.exterirorAngles.value!!
        )

        viewModel.updateVideoSkuRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    BaseApplication.getContext().captureEvent(
                        Events.VIDEO_SKU_UPDATED,
                        Properties().putValue("sku_name", viewModel.sku.value?.skuName.toString())
                            .putValue("project_id", projectId)
                            .putValue("prod_sub_cat_id", prod_sub_cat_id)
                            .putValue("angles", viewModel.exterirorAngles.value!!)
                    )

                    val sku = Sku()
                    sku?.skuId = skuId
                    sku?.skuName = skuName
                    sku?.totalImages = viewModel.exterirorAngles.value
                    sku?.subcategoryName = viewModel.subCategory.value?.sub_cat_name
                    sku?.subcategoryId = prod_sub_cat_id
                    sku?.exteriorAngles = viewModel.exterirorAngles.value

                    viewModel.sku.value = sku
                    viewModel.isSubCategoryConfirmed.value = true

                    //add sku to local database
                    viewModel.updateVideoSkuLocally(sku!!)

                    viewModel.isCameraButtonClickable = true
                    captureImage()
                }


                is Resource.Failure -> {
                    viewModel.isCameraButtonClickable = true
                    BaseApplication.getContext().captureFailureEvent(
                        Events.VIDEO_SKU_UPDATE_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it) {
                        updateSku(
                            projectId,
                            skuId,
                            skuName,
                            prod_sub_cat_id
                        )
                    }
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

        filename = viewModel.getFileName(
            requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE, 0),
            requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE, 0)
        )

        Log.d(TAG, "takePhoto: "+filename)

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
                        log("onImageSaved- " + difference)
                        addShootItem(file.path)
                    } else {
                        try {
                            mid = System.currentTimeMillis()
                            val difference = (mid - begin) / 1000.toFloat()
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
            binding.gyroView.visibility = View.GONE
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

        binding.gyroView.updateGryoView(
            getString(R.string.app_name),
            roll,
            pitch,
            movearrow,
            rotatedarrow
        )
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
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
                }

            }
        })
    }

    private fun addShootItem(capturedImage: String) {
        end = System.currentTimeMillis()
        val difference = (end - begin) / 1000.toFloat()
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

        var sequenceNumber = viewModel.getSequenceNumber(
            requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE, 0),
            requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE, 0),
            requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE, 0)
        )

        val shootData = ShootData(
            capturedImage,
            viewModel.projectId.value!!,
            viewModel.sku.value?.skuId!!,
            viewModel.categoryDetails.value?.imageType!!,
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString(),
            sequenceNumber,
            cameraAngle
        )

        val item = viewModel.shootList.value!!.firstOrNull {
            it.sequence == sequenceNumber
        }

        if (item != null){
            item.capturedImage = capturedImage
            item.angle = cameraAngle
        }else {
            viewModel.shootList.value!!.add(shootData)
        }

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






