package com.spyneai.shoot.ui.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.analyzer.LuminosityAnalyzer
import com.spyneai.base.BaseFragment
import com.spyneai.camera2.ShootDimensions
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.databinding.FragmentCameraBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.dialogs.SubCategoryConfirmationDialog
import com.spyneai.shoot.utils.ThreadExecutor
import com.spyneai.shoot.utils.log
import kotlinx.android.synthetic.main.activity_camera.*
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

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
        private const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9
    }

    private var pitch = 0.0
    var roll = 0.0

    private var centerPosition = 0
    private var topConstraint = 0
    private var bottomConstraint = 0

    private lateinit var mSensorManager: SensorManager
    private var mAccelerometer: Sensor? = null
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)


    var gravity = FloatArray(3)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)


        Handler(Looper.getMainLooper()).postDelayed({
            startCamera()
        }, 300)

        cameraExecutor = Executors.newSingleThreadExecutor()
        // Determine the output directory
        pickIt = PickiT(requireContext(), this, requireActivity())

        viewModel.startInteriorShots.observe(viewLifecycleOwner, {
            if (it) binding.tvSkipShoot?.visibility = View.VISIBLE
        })

        viewModel.startMiscShots.observe(viewLifecycleOwner, {
            if (it) binding.tvSkipShoot?.visibility = View.VISIBLE
        })

        viewModel.hideLeveler.observe(viewLifecycleOwner, {
            if (it) binding.flLevelIndicator.visibility = View.GONE
        })

        viewModel.isSubCategoryConfirmed.observe(viewLifecycleOwner, {
            if (it) binding.flLevelIndicator.visibility = View.VISIBLE
        })

        binding.tvSkipShoot?.setOnClickListener {
            when (viewModel.categoryDetails.value?.imageType) {
                "Interior" -> {
                    if (viewModel.interiorShootNumber.value == viewModel.interiorAngles.value?.minus(
                            1
                        )
                    ) {
                        viewModel.showMiscDialog.value = true
                    } else {
                        viewModel.interiorShootNumber.value =
                            viewModel.interiorShootNumber.value!! + 1
                    }
                }

                "Focus Shoot" -> {
                    if (viewModel.miscShootNumber.value == viewModel.miscAngles.value?.minus(1)) {
                        viewModel.selectBackground.value = true
                    } else {
                        viewModel.miscShootNumber.value = viewModel.miscShootNumber.value!! + 1
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        getPreviewDimensions(binding.ivGryroRing!!, 1)
        getPreviewDimensions(binding.tvCenter!!, 2)

        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            mSensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            mSensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onDestroy() {
        mSensorManager.unregisterListener(this)
        super.onDestroy()
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.cameraCaptureButton?.setOnClickListener {
            if ((viewModel.isSubCategoryConfirmed.value == null || viewModel.isSubCategoryConfirmed.value == false) &&
                (viewModel.categoryDetails.value?.categoryName == "Automobiles" ||
                        viewModel.categoryDetails.value?.categoryName == "Bikes")
            ) {
                SubCategoryConfirmationDialog().show(
                    requireFragmentManager(),
                    "SubCategoryConfirmationDialog"
                )
            } else {
                if (viewModel.isCameraButtonClickable) {
                    takePhoto()
                    log("shoot image button clicked")
                    viewModel.isCameraButtonClickable = false
                }
            }
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
            val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
            // The ratio for the output image and preview
            val aspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            // The display rotation
            val rotation = viewFinder.display.rotation

            val localCameraProvider = cameraProvider
                ?: throw IllegalStateException("Camera initialization failed.")

            // Preview
            val preview = Preview.Builder()
//                .setTargetAspectRatio(aspectRatio) // set the camera aspect ratio
                .setTargetRotation(rotation) // set the camera rotation
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            //for exact image cropping
            val viewPort = binding.viewFinder?.viewPort

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(flashMode)
                .setTargetAspectRatio(aspectRatio) // set the capture aspect ratio
                .setTargetRotation(rotation) // set the capture rotation
                .build()

            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageCapture!!)
                .setViewPort(viewPort!!)
                .build()

            // The Configuration of image analyzing
            imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(aspectRatio) // set the analyzer aspect ratio
                .setTargetRotation(rotation) // set the analyzer rotation
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // in our analysis, we care about the latest image
                .build()
                .apply {
                    // Use a worker thread for image analysis to prevent glitches
                    val analyzerThread = HandlerThread("LuminosityAnalysis").apply { start() }
                    setAnalyzer(
                        ThreadExecutor(Handler(analyzerThread.looper)),
                        LuminosityAnalyzer()
                    )
                }


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


            // Unbind use cases before rebinding
            cameraProvider.unbindAll()
            try {
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    useCaseGroup
                )

                if (viewModel.shootDimensions.value == null ||
                    viewModel.shootDimensions.value?.previewHeight == 0
                ) {
                    getPreviewDimensions(binding.viewFinder!!, 0)
                    getPreviewDimensions(binding.llCapture!!, 3)
                }
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))

    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Shut down our background executor
        cameraExecutor.shutdown()

    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }


    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

//        val photoFile = File(
//            outputDirectory,
//            SimpleDateFormat(FILENAME_FORMAT, Locale.US
//            ).format(System.currentTimeMillis()) + ".jpg")
//
//        // Create output options object which contains file + metadata
//        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

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

        var filename = viewModel.sku.value?.skuName + "_" + viewModel.sku.value?.skuId + "_"

        filename += if (viewModel.shootList.value == null)
            viewModel.categoryDetails.value?.imageType!! + "_1"
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
                else -> {
                    System.currentTimeMillis().toString()
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
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    viewModel.isCameraButtonClickable = true
                    log("Photo capture failed: " + exc.message)
                    requireContext().captureFailureEvent(
                        Events.IMAGE_CAPRURE_FAILED,
                        Properties(),
                        exc.localizedMessage
                    )
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // This function is called if capture is successfully completed
                    if (output.savedUri == null) {
                        if (file != null)
                            addShootItem(file.path)
                    } else {
                        try {
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
        log("onSensorChanged called")

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER && event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD){
            viewModel.isSensorAvaliable.value = true
        }else{
            viewModel.isSensorAvaliable.value = false
        }


        if (viewModel.isSubCategoryConfirmed.value == true)
            updateOrientationAngles()
    }

    fun updateOrientationAngles() {
        if(viewModel.isSensorAvaliable.value == false)
            binding.flLevelIndicator.visibility = View.GONE
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        log("AccelerometerReading(0): " + accelerometerReading.get(0))
        log("AccelerometerReading(1): " + accelerometerReading.get(1))
        log("AccelerometerReading(2): " + accelerometerReading.get(2))
        log("MagnetometerReading(0): " + magnetometerReading.get(0))
        log("MagnetometerReading(1): " + magnetometerReading.get(1))
        log("MagnetometerReading(2): " + magnetometerReading.get(2))

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // "orientationAngles" now has up-to-date information.

        //binding.tvAzimuth.text = "Azimuth ${Math.toDegrees(orientationAngles[0].toDouble())}"

        val diff = Math.toDegrees(orientationAngles[2].toDouble()) - roll

        val movearrow = abs(
            Math.toDegrees(orientationAngles[2].toDouble()).roundToInt()
        ) - abs(roll.roundToInt()) >= 1
        val rotatedarrow = abs(
            Math.toDegrees(orientationAngles[1].toDouble()).roundToInt()
        ) - abs(pitch.roundToInt()) >= 1

        pitch = Math.toDegrees(orientationAngles[1].toDouble())
        roll = Math.toDegrees(orientationAngles[2].toDouble())


        if ((roll >= -100 && roll <= -80) && (pitch >= -5 && pitch <= 5)) {

            binding
                .tvLevelIndicator
                ?.animate()
                ?.translationY(0f)
                ?.setInterpolator(AccelerateInterpolator())?.duration = 0

            binding.tvLevelIndicator?.rotation = 0f

            binding.ivTopLeft?.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gyro_in_level
                )
            )
            binding.ivBottomLeft?.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gyro_in_level
                )
            )

            binding.ivGryroRing?.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gyro_in_level
                )
            )
            binding.tvLevelIndicator?.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_gyro_level)

            binding.ivTopRight?.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gyro_in_level
                )
            )
            binding.ivBottomRight?.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gyro_in_level
                )
            )

        } else {
            binding.ivTopLeft?.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gyro_error_level
                )
            )
            binding.ivBottomLeft?.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gyro_error_level
                )
            )

            binding.ivGryroRing?.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gyro_error_level
                )
            )
            binding.tvLevelIndicator?.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_gyro_error)

            binding.ivTopRight?.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gyro_error_level
                )
            )
            binding.ivBottomRight?.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gyro_error_level
                )
            )

            if (movearrow)
                moveArrow(roll)

            if (rotatedarrow) {
                if (pitch > 0) {
                    rotateArrow(pitch.minus(5).roundToInt())
                } else {
                    rotateArrow(pitch.plus(5).roundToInt())
                }
            }

        }
    }

    private fun rotateArrow(roundToInt: Int) {
        binding.tvLevelIndicator?.rotation = roundToInt.toFloat()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun moveArrow(roll: Double) {
        var newRoll = roll + 90

        if (newRoll > 0 && (centerPosition + newRoll) < bottomConstraint) {

            newRoll -= 10
            binding
                .tvLevelIndicator
                ?.animate()
                ?.translationY(newRoll.toFloat())
                ?.setInterpolator(AccelerateInterpolator())?.duration = 0
        }

        if (newRoll < 0 && (centerPosition - newRoll) > topConstraint) {

            newRoll += 10

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
        if (viewModel.shootList.value == null)
            viewModel.shootList.value = ArrayList()

        viewModel.shootList.value!!.add(
            ShootData(
                capturedImage,
                viewModel.sku.value?.projectId!!,
                viewModel.sku.value?.skuId!!,
                viewModel.categoryDetails.value?.imageType!!,
                Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
                viewModel.shootList.value!!.size.plus(1)
            )
        )

        viewModel.shootList.value = viewModel.shootList.value

        val properties = Properties()
        properties.apply {
            this["project_id"] = viewModel.sku.value?.projectId!!
            this["sku_id"] = viewModel.sku.value?.skuId!!
            this["image_type"] = viewModel.categoryDetails.value?.imageType!!
        }

        requireContext().captureEvent(Events.IMAGE_CAPTURED, properties)
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

}
