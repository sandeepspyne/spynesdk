import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.ExifInterface
import android.media.MediaActionSound
import android.os.*
import android.provider.MediaStore
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import android.view.Surface.ROTATION_90
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.spyneai.*
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.camera2.ShootDimensions
import com.spyneai.databinding.FragmentCameraBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.utils.log
import com.spyneai.shoot.utils.shoot
import kotlinx.android.synthetic.main.activity_credit_plans.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.*
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
    SensorEventListener {
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
    var angle = 0
    var upcomingAngle = 0

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
        private const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9
    }

    private var pitch = 0.0
    var roll = 0.0
    var azimuth = 0.0

    private lateinit var mSensorManager: SensorManager
    private var mAccelerometer: Sensor? = null
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null
    private var handler: Handler? = null

    private var filename = ""

    var gravity = FloatArray(3)
    val TAG = "Camera Fragment"


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
        // Determine the output direcrotory
        pickIt = PickiT(requireContext(), this, requireActivity())

        viewModel.startInteriorShots.observe(viewLifecycleOwner, {
            if (it) {
                binding.switchShowOverlay?.isChecked = false
                binding.switchShowGyro?.isChecked = false
                binding.switchShowOverlay?.isClickable = false
                binding.switchShowGyro?.isClickable = false
                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
                binding.tvSkipShoot?.text = getString(R.string.miscshoots)
                binding.llSkip?.visibility = View.VISIBLE
            }
        })

        viewModel.imageTypeInfo.observe(viewLifecycleOwner, {
            if (it) {
                startCamera()
            }
        })

        viewModel.startMiscShots.observe(viewLifecycleOwner, {
            if (it) {
                binding.switchShowOverlay?.isChecked = false
                binding.switchShowGyro?.isChecked = false
                binding.switchShowOverlay?.isClickable = false
                binding.switchShowGyro?.isClickable = false
                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
                if (getString(R.string.app_name) == AppConstants.OLA_CABS) {
                    binding.tvSkipShoot?.text = getString(R.string.three_sixty_int)
                } else {
                    binding.tvSkipShoot?.text = getString(R.string.end_shoot_karvi)
                }
                binding.llSkip?.visibility = View.VISIBLE
            }

        })

        viewModel.showLeveler.observe(viewLifecycleOwner, {
            if (it && isSensorAvaliable) {
                binding.flLevelIndicator.start(viewModel.categoryDetails.value?.categoryName!!)
            } else binding.flLevelIndicator.visibility = View.GONE
        })

//            viewModel.hideLeveler.observe(viewLifecycleOwner, {
//                if (it) {
//
//                }
//            })
        viewModel.showGrid.observe(viewLifecycleOwner, {
            if (it) {
                binding.groupGridLines?.visibility = View.VISIBLE
            } else binding.groupGridLines?.visibility = View.INVISIBLE
        })

        if (getString(R.string.app_name) == AppConstants.KARVI) {
            binding.tvSkipShoot?.setTextColor(
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

        binding.cameraCaptureButton.setOnClickListener {
            onCaptureClick()
        }

        binding.tvSkipShoot?.setOnClickListener {
            viewModel.skipImage(getString(R.string.app_name))
        }

        viewModel.onVolumeKeyPressed.observe(viewLifecycleOwner, {
            when (viewModel.categoryDetails.value?.categoryName) {
                "Automobiles", "Footwear" -> {
                    if (viewModel.subCategory.value?.prod_sub_cat_id != null)
                        onCaptureClick()
                }

                "E-Commerce", "Photo Box" -> {
                    if (viewModel.sku?.skuId != null)
                        onCaptureClick()
                }
            }
        })

        //camera setting
        if (viewModel.categoryDetails.value?.categoryId == AppConstants.CARS_CATEGORY_ID ||
                viewModel.categoryDetails.value?.categoryId == AppConstants.BIKES_CATEGORY_ID){
            viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
            viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
            viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive

            binding.llSetting.setOnClickListener {
                if (viewModel.categoryDetails.value?.imageType == "Info" ||
                    viewModel.categoryDetails.value?.imageType == "Misc" ||
                    viewModel.categoryDetails.value?.imageType == "Interior" ||
                    viewModel.categoryDetails.value?.imageType == "Focus Shoot"
                ) {
                    binding.switchShowOverlay?.isChecked = false
                    binding.switchShowGyro?.isChecked = false
                    binding.switchShowOverlay?.isClickable = false
                    binding.switchShowGyro?.isClickable = false
                } else {
                    binding.switchShowOverlay?.isClickable = true
                    binding.switchShowGyro?.isClickable = true

                }

                if (viewModel.categoryDetails.value?.categoryId == AppConstants.FOOD_AND_BEV_CATEGORY_ID ||
                    viewModel.categoryDetails.value?.categoryId == AppConstants.ECOM_CATEGORY_ID ||
                    viewModel.categoryDetails.value?.categoryId == AppConstants.PHOTO_BOX_CATEGORY_ID ||
                    getString(R.string.app_name) == AppConstants.SWIGGY
                ) {
                    binding.switchShowOverlay?.isChecked = false
                    binding.switchShowOverlay?.isClickable = false
                }
                if (getString(R.string.app_name) == AppConstants.SPYNE_AI && viewModel.categoryDetails.value?.imageType == "Info") {
                    binding.switchShowOverlay?.isChecked = false
                    binding.switchShowOverlay?.isClickable = false
                    binding.switchShowGyro?.isChecked = false
                    binding.switchShowGyro?.isClickable = false
                }
                if (binding.ivCross?.visibility == GONE) {
                    binding.ivCross?.visibility = VISIBLE
                    binding.llShowOverlay?.visibility = VISIBLE
                    binding.llShowGrid?.visibility = VISIBLE
                    binding.llShowGyro?.visibility = VISIBLE
                } else {
                    binding.ivCross?.visibility = GONE
                    binding.llShowOverlay?.visibility = GONE
                    binding.llShowGrid?.visibility = GONE
                    binding.llShowGyro?.visibility = GONE
                }
            }

            binding.switchShowGyro?.isChecked = viewModel.getCameraSetting().isGryroActive

            binding.switchShowGyro?.setOnCheckedChangeListener { _, isChecked ->
                Utilities.saveBool(
                    requireContext(),
                    viewModel.categoryDetails.value?.categoryId + AppConstants.SETTING_STATUS_GYRO,
                    isChecked
                )
                if (isChecked)
                    viewModel.showLeveler.value = isChecked
                else viewModel.showLeveler.value = false
            }

            binding.switchShowOverlay?.isChecked = viewModel.getCameraSetting().isOverlayActive

            binding.switchShowOverlay?.setOnCheckedChangeListener { _, isChecked ->
                Utilities.saveBool(
                    requireContext(),
                    viewModel.categoryDetails.value?.categoryId + AppConstants.SETTING_STATUS_OVERLAY,
                    isChecked
                )
                if (isChecked)
                    viewModel.showOverlay.value = isChecked
                else
                    viewModel.showOverlay.value = false
            }

            binding.switchShowGrid?.isChecked = viewModel.getCameraSetting().isGridActive

            binding.switchShowGrid?.setOnCheckedChangeListener { _, isChecked ->
                Utilities.saveBool(
                    requireContext(),
                    viewModel.categoryDetails.value?.categoryId + AppConstants.SETTING_STATUS_GRID,
                    isChecked
                )
                if (isChecked)
                    viewModel.showGrid.value = isChecked
                else
                    viewModel.showGrid.value = false
            }
        }else {
            binding.llSetting.visibility = GONE
        }


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
        super.onDestroy()
    }

    private fun onCaptureClick() {
//        captureImage()
        if (binding.flLevelIndicator.visibility == View.VISIBLE) {
            if (binding.flLevelIndicator.isGyroOnCorrectAngle) {
                captureImage()
            } else {
                showGryroToast()
            }
        } else {
            captureImage()
        }
    }

    private fun showGryroToast() {
        val text = getString(R.string.level_gryometer)
        val centeredText: Spannable = SpannableString(text)
        centeredText.setSpan(
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
            0, text.length - 1,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )

        Toast.makeText(requireContext(), centeredText, Toast.LENGTH_LONG).show()
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
                Log.d(TAG, "startCamera: " + e.message)
                Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT)
                    .show()
                return@addListener
            } catch (e: ExecutionException) {
                Log.d(TAG, "startCamera: " + e.message)
                Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT)
                    .show()
                return@addListener
            }

            // The display information
            //val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }
            // The ratio for the output image and preview
            var height = 0
            var width = 0
            val displayMetrics = DisplayMetrics()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
            val preview = when (viewModel.categoryDetails.value?.categoryId) {
                AppConstants.CARS_CATEGORY_ID,
                AppConstants.BIKES_CATEGORY_ID -> {
                    if (getString(R.string.app_name) == AppConstants.KARVI) {
                        Preview.Builder()
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
                AppConstants.FOOTWEAR_CATEGORY_ID,
                AppConstants.MENS_FASHION_CATEGORY_ID,
                AppConstants.WOMENS_FASHION_CATEGORY_ID,
                AppConstants.CAPS_CATEGORY_ID,
                AppConstants.FASHION_CATEGORY_ID,
                AppConstants.ACCESSORIES_CATEGORY_ID,
                AppConstants.HEALTH_AND_BEAUTY_CATEGORY_ID,
                AppConstants.ECOM_CATEGORY_ID,
                AppConstants.PHOTO_BOX_CATEGORY_ID -> {
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

            imageCapture = when (viewModel.categoryDetails.value?.categoryId) {
                AppConstants.CARS_CATEGORY_ID,
                AppConstants.BIKES_CATEGORY_ID -> {
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
                AppConstants.FOOTWEAR_CATEGORY_ID,
                AppConstants.MENS_FASHION_CATEGORY_ID,
                AppConstants.WOMENS_FASHION_CATEGORY_ID,
                AppConstants.CAPS_CATEGORY_ID,
                AppConstants.FASHION_CATEGORY_ID,
                AppConstants.FOOD_AND_BEV_CATEGORY_ID,
                AppConstants.ACCESSORIES_CATEGORY_ID,
                AppConstants.HEALTH_AND_BEAUTY_CATEGORY_ID,
                AppConstants.PHOTO_BOX_CATEGORY_ID,
                AppConstants.ECOM_CATEGORY_ID -> {

                    if (viewModel.categoryDetails.value?.imageType == "Info") {
                        ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .setFlashMode(flashMode)
                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                            //                            .setTargetRotation(ROTATION_90)
                            .build()

                    } else {
                        ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .setFlashMode(flashMode)
                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                            .setTargetRotation(ROTATION_90)
                            .build()
                    }
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

                when (viewModel.categoryDetails.value?.categoryId) {
                    AppConstants.CARS_CATEGORY_ID,
                    AppConstants.BIKES_CATEGORY_ID -> {
                    }
                    else -> {
                        var currentZoomRatio = cameraInfo?.zoomState?.value?.zoomRatio ?: 0F
                        cameraControl?.setZoomRatio(currentZoomRatio * 1.3F)
                    }
                }
                if (viewModel.shootDimensions.value == null ||
                    viewModel.shootDimensions.value?.previewHeight == 0
                ) {
                    getPreviewDimensions(binding.viewFinder!!, 0)
                }
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                val properties = HashMap<String, Any?>()
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

        val s = ""

        Log.d(TAG, "takePhoto: " + filename)

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

                    try {
                        Toast.makeText(
                            requireContext(),
                            "Photo capture failed: " + exc.message,
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {

                    }

                    Utilities.hideProgressDialog()

                    BaseApplication.getContext().captureFailureEvent(
                        Events.IMAGE_CAPRURE_FAILED,
                        HashMap<String, Any?>(),
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
            System.arraycopy(
                event.values,
                0,
                accelerometerReading,
                0,
                accelerometerReading.size
            )
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

        val rotatedarrow =
            abs(Math.toDegrees(orientationAngles[1].toDouble()).roundToInt()) - abs(
                pitch.roundToInt()
            ) >= 1


        pitch = Math.toDegrees(orientationAngles[1].toDouble())
        roll = Math.toDegrees(orientationAngles[2].toDouble())
        azimuth = (orientationAngles[0] * 180 / Math.PI.toFloat()).toDouble()

        binding.tvPitch?.text = "Pitch : " + pitch.roundToInt().toString()
        binding.tvRoll?.text = "Roll : " + roll.roundToInt().toString()

        binding.flLevelIndicator.updateGryoView(
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

                        binding.flTapToFocus.init(
                            binding.viewFinder,
                            cameraControl!!,
                            cameraInfo!!,
                            shootDimensions
                        )
                    }
                }

            }
        })
    }


    private fun addShootItem(capturedImage: String) {
        Log.d(TAG, "addShootItem: " + filename)
        end = System.currentTimeMillis()
        val difference = (end - begin) / 1000.toFloat()
        log("addShootIteamCalled- " + difference)
        viewModel.showConfirmReshootDialog.value = true

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

        Log.d(TAG, "addShootItem: " + sequenceNumber)
        val debugData = JSONObject()
        debugData.put("roll", roll.roundToInt().unaryPlus())
        debugData.put("pitch", pitch.roundToInt().unaryPlus())

        val shootData = ShootData(
            capturedImage,
            getUuid(),
            viewModel.sku?.uuid!!,
            viewModel.categoryDetails.value?.imageType!!,
            Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString(),
            viewModel.overlayId,
            sequenceNumber,
            binding.flLevelIndicator.cameraAngle,
            filename,
            debugData.toString()
        )

        val item = viewModel.shootList.value!!.firstOrNull {
            it.overlayId == viewModel.overlayId
        }

        if (item != null) {
            item.capturedImage = capturedImage
            item.angle = binding.flLevelIndicator.cameraAngle
            item.name = filename
            viewModel.isReclick = true
        } else {
            viewModel.isReclick = false
            viewModel.shootList.value!!.add(shootData)
        }

        viewModel.shootList.value = viewModel.shootList.value

        val properties = HashMap<String, Any?>()
//        properties.apply {
//            this["project_id"] = viewModel.projectId.value!!
//            this["sku_id"] = viewModel.sku?.skuId!!
//            this["image_type"] = viewModel.categoryDetails.value?.imageType!!
//        }

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





