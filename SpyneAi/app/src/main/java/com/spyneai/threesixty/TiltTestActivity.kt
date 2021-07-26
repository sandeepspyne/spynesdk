package com.spyneai.threesixty

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.spyneai.R
import com.spyneai.camera2.ShootDimensions
import com.spyneai.databinding.ActivityTiltTestBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable.start
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.properties.Delegates

class TiltTestActivity : AppCompatActivity() , SensorEventListener {

    private var mAccelerometer: Sensor? = null
    private var pitch = 0.0
    var roll = 0.0
    private  var tilt:kotlin.Double = 0.0
    private  var azimuth:kotlin.Double = 0.0

    private var x_angle: Double = 0.0
    private var y_angle: Double = 0.0
    private var z_angle: Double = 0.0
    private val rollPositiveMin = 40
    private val rollPositiveMax = 45
    private val rollNegativeMin = -80
    private val rollNegativeMax = -100

    private var ringHeight = 0
    private var centerPosition = 0
    private var topConstraint = 0
    private var bottomConstraint = 0



    var handler = Handler()

    private lateinit var mSensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)


    var gravity = FloatArray(3)

    private lateinit var binding : ActivityTiltTestBinding

    companion object {
        private const val TAG = "CameraXTest"

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
    }

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            startCamera()
        } else {
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_LONG)
        }
    }

    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var videoCapture: VideoCapture? = null

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

    // Selector showing is flash enabled or not
    private var isTorchOn = false

    // Selector showing is recording currently active
    private var isRecording = false

    // The Folder location where all the files will be stored
    private val outputDirectory: String by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${Environment.DIRECTORY_DCIM}/CameraXTest/"
        } else {
            "${getExternalFilesDir(Environment.DIRECTORY_DCIM)?.path}/CameraXTest/"
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTiltTestBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        binding.btnFlash.setOnClickListener { toggleFlash() }
        binding.ivBack.setOnClickListener { onBackPressed() }


        setPermissions()
    }

    override fun onResume() {
        super.onResume()

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
//        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
//            mSensorManager.registerListener(
//                this,
//                accelerometer,
//                500
//            )
//        }
//        mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
//            mSensorManager.registerListener(
//                this,
//                magneticField,
//                500
//            )
//        }

        getPreviewDimensions(binding.ivGryroRing,true)
        getPreviewDimensions(binding.tvCenter,false)

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

    private fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun setPermissions() {
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            permissionRequest.launch(permissions.toTypedArray())
        }
    }


    public fun onDemoVideoClosed() {
        startCamera()
    }

    /**
     * Unbinds all the lifecycles from CameraX, then creates new with new parameters
     * */
    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        // This is the Texture View where the camera will be rendered
        val viewFinder = binding.viewFinder

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
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
                .setTargetResolution(Size(480, 360))
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

                // binding.tvLine.visibility = View.VISIBLE
                // binding.ivDemo.visibility = View.VISIBLE

            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind use cases", e)
            }
        }, ContextCompat.getMainExecutor(this))
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


    override fun onDestroy() {
        mSensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBackPressed() {
        mSensorManager.unregisterListener(this)
        super.onBackPressed()
    }


    override fun onPause() {
        mSensorManager.unregisterListener(this)
        super.onPause()
    }


    override fun onSensorChanged(event: SensorEvent?) {
        //Get Rotation Vector Sensor Values


        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        updateOrientationAngles()


//        moveArrow(roll.roundToInt())
//
//        if ((roll >= -145 && roll <=-130) || roll >= 40 && roll <= 50){
//            binding.tvLine.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
//            binding.ivArrow.setColorFilter(ContextCompat.getColor(this, R.color.green))
//        }else{
//            // Log.d("TAG", "onSensorChanged: "+"false "+tilt)
//            binding.tvLine.setBackgroundColor(ContextCompat.getColor(this, R.color.errorcolor))
//            binding.ivArrow.setColorFilter(ContextCompat.getColor(this, R.color.errorcolor))
//
//        }

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

        Log.d(TAG, "updateOrientationAngles: "+abs(roll.roundToInt()))
        Log.d(TAG, "updateOrientationAngles: "+abs(Math.toDegrees(orientationAngles[2].toDouble()).roundToInt()))
        Log.d(TAG, "updateOrientationAngles: "+diff)
        Log.d(TAG, "updateOrientationAngles: ---------------------------------------------")


        val movearrow = abs(Math.toDegrees(orientationAngles[2].toDouble()).roundToInt()) -  abs(roll.roundToInt()) >= 1
        val rotatedarrow = abs(Math.toDegrees(orientationAngles[1].toDouble()).roundToInt()) -  abs(pitch.roundToInt()) >= 1

        pitch = Math.toDegrees(orientationAngles[1].toDouble())
        roll = Math.toDegrees(orientationAngles[2].toDouble())


        if ((roll >= -100 && roll <=-80) && (pitch >= -10 && pitch <= 10)){

            binding
                .tvLevelIndicator
                .animate()
                .translationY(0f)
                .setInterpolator(AccelerateInterpolator()).duration = 0

            binding.tvLevelIndicator.rotation = 0f

            binding.ivTopLeft.setColorFilter(ContextCompat.getColor(this, R.color.gyro_in_level))
            binding.ivBottomLeft.setColorFilter(ContextCompat.getColor(this, R.color.gyro_in_level))

            binding.ivGryroRing.setColorFilter(ContextCompat.getColor(this, R.color.gyro_in_level))
            binding.tvLevelIndicator.background = ContextCompat.getDrawable(this, R.drawable.bg_gyro_level)

            binding.ivTopRight.setColorFilter(ContextCompat.getColor(this, R.color.gyro_in_level))
            binding.ivBottomRight.setColorFilter(ContextCompat.getColor(this, R.color.gyro_in_level))
        }else{

            binding.ivTopLeft.setColorFilter(ContextCompat.getColor(this, R.color.gyro_error_level))
            binding.ivBottomLeft.setColorFilter(ContextCompat.getColor(this, R.color.gyro_error_level))

            binding.ivGryroRing.setColorFilter(ContextCompat.getColor(this, R.color.gyro_error_level))
            binding.tvLevelIndicator.background = ContextCompat.getDrawable(this, R.drawable.bg_gyro_error)

            binding.ivTopRight.setColorFilter(ContextCompat.getColor(this, R.color.gyro_error_level))
            binding.ivBottomRight.setColorFilter(ContextCompat.getColor(this, R.color.gyro_error_level))

            if (movearrow)
                moveArrow(roll)

            if (rotatedarrow){
                if (pitch > 0){
                    rotateArrow(pitch.minus(10).roundToInt())
                }else{
                    rotateArrow(pitch.plus(10).roundToInt())
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
        Log.d(TAG, "moveArrow: "+newRoll)
        Log.d(TAG, "moveArrow: ------------------------------")

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



//        ObjectAnimator.ofFloat(binding.tvLevelIndicator, "translationY", roll.toFloat()).apply {
//            duration = 5
//            start()
//        }
    }


    private fun getPreviewDimensions(view: View,isRing : Boolean) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                Log.d(TAG, "onGlobalLayout: " + view.left)
                Log.d(TAG, "onGlobalLayout: " + view.top)
                Log.d(TAG, "onGlobalLayout: " + view.height)
                Log.d(TAG, "onGlobalLayout: " + view.width)
                Log.d(TAG, "onGlobalLayout: -------------------------------")

                if (isRing) {
                    topConstraint = view.top
                    bottomConstraint = topConstraint + view.height
                } else {
                    centerPosition = view.top
                }

            }
        })
    }



    fun Context.dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
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

    fun ImageButton.toggleButton(
        flag: Boolean,
        rotationAngle: Float,
        @DrawableRes firstIcon: Int,
        @DrawableRes secondIcon: Int,
        action: (Boolean) -> Unit
    ) {
        if (flag) {
            if (rotationY == 0f) rotationY = rotationAngle
            animate().rotationY(0f).apply {
                setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        action(!flag)
                    }
                })
            }.duration = 200
            GlobalScope.launch(Dispatchers.Main) {
                delay(100)
                setImageResource(firstIcon)
            }
        } else {
            if (rotationY == rotationAngle) rotationY = 0f
            animate().rotationY(rotationAngle).apply {
                setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        action(!flag)
                    }
                })
            }.duration = 200
            GlobalScope.launch(Dispatchers.Main) {
                delay(100)
                setImageResource(secondIcon)
            }
        }
    }

}


