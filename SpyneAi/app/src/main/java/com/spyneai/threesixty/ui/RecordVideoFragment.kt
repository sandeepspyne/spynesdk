package com.spyneai.threesixty.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.animation.doOnCancel
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.camera2.ShootDimensions
import com.spyneai.databinding.FragmentRecordVideoBinding
import com.spyneai.needs.AppConstants
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import com.spyneai.toggleButton
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class RecordVideoFragment : BaseFragment<ThreeSixtyViewModel,FragmentRecordVideoBinding>(),
    PickiTCallbacks, SensorEventListener,View.OnTouchListener {


    companion object {
        private const val TAG = "RecordVideoFragment"

        private const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
        private const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9

        private const val REQUEST_CODE_PERMISSIONS = 100
        var intent : Intent? = null

        // The permissions we need for the app to work properly
        private val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACCESS_MEDIA_LOCATION)
            }
        }

        private var stopTimer = false
        private lateinit var fragmentList: ArrayList<Fragment>
    }

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Permissions not granted", Toast.LENGTH_LONG)
        }
    }

    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var videoCapture: VideoCapture? = null

    private var cameraControl : CameraControl? = null
    private var cameraInfo : CameraInfo? = null
    private var handler : Handler? = null


    // Selector showing which camera is selected (front or back)
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA

    // Selector showing which flash mode is selected (on, off or auto)
    private var flashMode by Delegates.observable(ImageCapture.FLASH_MODE_OFF) { _, _, new ->
        binding.btnFlash.setImageResource(
            when (new) {
                ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on
                ImageCapture.FLASH_MODE_AUTO -> R.drawable.ic_flash_auto
                else -> R.drawable.ic_flash_off
            }
        )
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

    // Selector showing is flash enabled or not
    private var isTorchOn = false

    // Selector showing is recording currently active
    private var isRecording = false
    private val animateRecord by lazy {
        ObjectAnimator.ofFloat(binding.btnRecordVideo, View.ALPHA, 1f, 0.5f).apply {
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            doOnCancel { binding.btnRecordVideo.alpha = 1f }
        }
    }


    // The Folder location where all the files will be stored
    private val outputDirectory: String by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${Environment.DIRECTORY_DCIM}/Spyne/"
        } else {
            "${requireActivity().getExternalFilesDir(Environment.DIRECTORY_DCIM)?.path}/Spyne/"
        }
    }

    //Declare PickiT
    var pickiT: PickiT? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler()
        pickiT = PickiT(requireContext(), this, requireActivity())

        mSensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)


        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                setPermissions()
            }
        }, 300)

        viewModel.enableRecording.observe(viewLifecycleOwner,{
            if (it) {
                binding.btnRecordVideo.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bg_record_button_enabled))

                binding.flLevelIndicator.visibility = View.VISIBLE

                binding.btnFlash.visibility = View.VISIBLE

                binding.btnFlash.setOnClickListener { toggleFlash() }

                binding.btnRecordVideo.setOnClickListener {
                    recordVideo()
                }
            }
        })
    }

    private fun setPermissions() {
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            permissionRequest.launch(permissions.toTypedArray())
        }
    }

    protected fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onSensorChanged(event: SensorEvent?) {
        //Get Rotation Vector Sensor Values

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        if (viewModel.enableRecording.value == true)
            updateOrientationAngles()

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

        val diff = Math.toDegrees(orientationAngles[2].toDouble()) - roll

        val movearrow = abs(Math.toDegrees(orientationAngles[2].toDouble()).roundToInt()) -  abs(roll.roundToInt()) >= 1
        val rotatedarrow = abs(Math.toDegrees(orientationAngles[1].toDouble()).roundToInt()) -  abs(pitch.roundToInt()) >= 1

        pitch = Math.toDegrees(orientationAngles[1].toDouble())
        roll = Math.toDegrees(orientationAngles[2].toDouble())


        if ((roll >= -100 && roll <=-80) && (pitch >= -5 && pitch <= 5)){

            binding
                .tvLevelIndicator
                .animate()
                .translationY(0f)
                .setInterpolator(AccelerateInterpolator()).duration = 0

            binding.tvLevelIndicator.rotation = 0f

            binding.ivTopLeft.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gyro_in_level))
            binding.ivBottomLeft.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gyro_in_level))

            binding.ivGryroRing.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gyro_in_level))
            binding.tvLevelIndicator.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_gyro_level)

            binding.ivTopRight.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gyro_in_level))
            binding.ivBottomRight.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gyro_in_level))

            binding.tvWarning.visibility = View.GONE
        }else{

            binding.tvWarning.visibility = View.VISIBLE
            binding.ivTopLeft.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gyro_error_level))
            binding.ivBottomLeft.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gyro_error_level))

            binding.ivGryroRing.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gyro_error_level))
            binding.tvLevelIndicator.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_gyro_error)

            binding.ivTopRight.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gyro_error_level))
            binding.ivBottomRight.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gyro_error_level))

            if (movearrow)
                moveArrow(roll)

            if (rotatedarrow){
                if (pitch > 0){
                    rotateArrow(pitch.minus(5).roundToInt())
                }else{
                    rotateArrow(pitch.plus(5).roundToInt())
                }
            }
        }
    }

    private fun rotateArrow(roundToInt: Int) {
        binding.tvLevelIndicator.rotation = roundToInt.toFloat()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun moveArrow(roll: Double) {
        var newRoll = roll + 90

        if (newRoll > 0 && (centerPosition + newRoll) < bottomConstraint){

            newRoll -= 10
            binding
                .tvLevelIndicator
                .animate()
                .translationY(newRoll.toFloat())
                .setInterpolator(AccelerateInterpolator()).duration = 0
        }

        if (newRoll < 0 && (centerPosition - newRoll) > topConstraint) {

            newRoll += 10

            binding
                .tvLevelIndicator
                .animate()
                .translationY(newRoll.toFloat())
                .setInterpolator(AccelerateInterpolator()).duration = 0
        }
    }

    /**
     * Unbinds all the lifecycles from CameraX, then creates new with new parameters
     * */
    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        // This is the Texture View where the camera will be rendered
        val viewFinder = binding.viewFinder

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // The display information
            val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
            // The ratio for the output image and preview
            val aspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            // The display rotation
            val rotation = viewFinder.display.rotation

            val localCameraProvider = cameraProvider
                ?: throw IllegalStateException("Camera initialization failed.")

            // The Configuration of camera preview
            preview = Preview.Builder()
                // .setTargetResolution(Size(640,480))
                .setTargetAspectRatio(aspectRatio) // set the camera aspect ratio
                .setTargetRotation(rotation) // set the camera rotation
                .build()

            val videoCaptureConfig =
                VideoCapture.DEFAULT_CONFIG.config // default config for video capture
            // The Configuration of video capture
            videoCapture = VideoCapture.Builder()
                //.fromConfig(videoCaptureConfig)
                .setTargetResolution(Size(480,360))
                .build()

            localCameraProvider.unbindAll() // unbind the use-cases before rebinding them

            try {
                // Bind all use cases to the camera with lifecycle
                camera = localCameraProvider.bindToLifecycle(
                    this, // current lifecycle owner
                    lensFacing, // either front or back facing
                    preview, // camera preview use case
                    videoCapture, // video capture use case
                )

                // Attach the viewfinder's surface provider to preview use case
                preview?.setSurfaceProvider(viewFinder.surfaceProvider)


                cameraControl = camera!!.cameraControl

                cameraInfo = camera!!.cameraInfo

                binding.viewFinder.setOnTouchListener(this)

                if (viewModel.shootDimensions.value == null ||
                    viewModel.shootDimensions.value?.previewHeight == 0
                ) {
                    getPreviewDimensions(binding.viewFinder, false,true)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind use cases", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     *  Detecting the most suitable aspect ratio for current dimensions
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }



    @SuppressLint("RestrictedApi")
    private fun recordVideo() {
        val localVideoCapture = videoCapture ?: throw IllegalStateException("Camera initialization failed.")

        // Options fot the output video file
        val outputOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis())
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, outputDirectory)
            }

            requireActivity().contentResolver.run {
                val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

                VideoCapture.OutputFileOptions.Builder(this, contentUri, contentValues)
            }
        } else {
            File(outputDirectory).mkdirs()
            val file = File("$outputDirectory/${System.currentTimeMillis()}.mp4")

            VideoCapture.OutputFileOptions.Builder(file)
        }.build()

        if (!isRecording) {
            binding.tvStart.visibility = View.GONE

            binding.btnRecordVideo.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_stop_video))
            //animateRecord.start()

            //start record timer && enable button click && flash button
            stopTimer = false
            binding.tvTimer.visibility = View.VISIBLE
            startRecordTime(0)

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }

            localVideoCapture.startRecording(
                outputOptions, // the options needed for the final video
                ContextCompat.getMainExecutor(requireContext()), // the executor, on which the task will run
                object : VideoCapture.OnVideoSavedCallback { // the callback after recording a video
                    override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                        // Create small preview
                        outputFileResults.savedUri
                            ?.let { uri ->
                                //setGalleryThumbnail(uri)

                                try {
                                    var file = uri.toFile()
                                    startNextActivity(file.path)
                                }catch (ex : IllegalArgumentException){
                                    pickiT?.getPath(uri, Build.VERSION.SDK_INT)
                                }
                            }

                    }

                    override fun onError(
                        videoCaptureError: Int,
                        message: String,
                        cause: Throwable?
                    ) {
                        // This function is called if there is an error during recording process
                        //animateRecord.cancel()
                        val msg = "Video capture failed: $message"
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, msg)
                        cause?.printStackTrace()

                        binding.tvStart.text = "Start"
                    }
                })
        } else {
            //animateRecord.cancel()
            //stop recording timer
            stopTimer = true
            binding.tvTimer.visibility = View.GONE
            binding.tvTimer.text = "00:00"
            localVideoCapture.stopRecording()
        }
        isRecording = !isRecording
    }

    private fun startRecordTime(millisUntilFinished: Long) {
        binding.tvTimer.setText(
            "" + String.format(
                "%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(
                    millisUntilFinished
                ),
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                millisUntilFinished
                            )
                        )
            )
        );

        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                if (!stopTimer)
                    startRecordTime(millisUntilFinished + 1000)
            }
        }, 1000)

    }

    private fun toggleFlash() = binding.btnFlash.toggleButton(
        flag = flashMode == ImageCapture.FLASH_MODE_ON,
        rotationAngle = 360f,
        firstIcon = R.drawable.ic_flash_off,
        secondIcon = R.drawable.ic_flash_on
    ) { flag ->
        isTorchOn = flag
        flashMode = if (flag) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        camera?.cameraControl?.enableTorch(flag)
    }


    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentRecordVideoBinding.inflate(inflater, container, false)

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
        startNextActivity(path.toString())
    }

    private fun startNextActivity(videoPath: String) {
        stopTimer = true
        binding.tvTimer.visibility = View.GONE
        binding.tvTimer.text = "00:00"


        val trimIntent = Intent(
            requireContext(),
            TrimActivity::class.java
        )

        trimIntent.putExtra("src_path",videoPath)
        trimIntent.putExtra("sku_id",viewModel.videoDetails.skuId)
        trimIntent.putExtra("sku_name",viewModel.videoDetails.skuName)
        trimIntent.putExtra("project_id",viewModel.videoDetails.projectId)
        trimIntent.putExtra(AppConstants.CATEGORY_NAME,viewModel.videoDetails.categoryName)
        trimIntent.putExtra(AppConstants.CATEGORY_ID,viewModel.videoDetails.categoryId)
        trimIntent.putExtra("frames",viewModel.videoDetails.frames)
        trimIntent.putExtra("shoot_mode",intent?.getIntExtra("shoot_mode",0))

        startActivity(trimIntent)

        binding.tvStart.text = "Start"
        binding.btnRecordVideo.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.bg_record_button_enabled))
        isRecording = !isRecording
    }

    override fun onResume() {
        super.onResume()

        getPreviewDimensions(binding.ivGryroRing,true,false)
        getPreviewDimensions(binding.tvCenter,false,false)

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

    private fun getPreviewDimensions(view: View,isRing : Boolean,isPreview : Boolean) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                when {
                    isRing -> {
                        topConstraint = view.top
                        bottomConstraint = topConstraint + view.height
                    }

                    isPreview -> {
                        val shootDimensions = ShootDimensions()
                        shootDimensions.previewWidth = view.width
                        shootDimensions.previewHeight = view.height

                        viewModel.shootDimensions.value = shootDimensions
                    }

                    else -> {
                        centerPosition = view.top
                    }
                }
            }
        })
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
                        LayoutInflater.from(requireContext()).inflate(R.layout.item_focus, null)
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
                        FrameLayout.LayoutParams(seekWidth, FrameLayout.LayoutParams.WRAP_CONTENT)

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

}