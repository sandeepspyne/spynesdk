package com.spyneai.videorecording

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.*
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewStructure
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.animation.doOnCancel
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.spyneai.R
import com.spyneai.databinding.ActivityRecordVideoBinding
import com.spyneai.videorecording.adapter.ThreeSixtyShootDemoAdapter
import com.spyneai.videorecording.fragments.FragmentOneThreeSixtyShootDemo
import com.spyneai.videorecording.fragments.FragmentTwoThreeSixtyShootDemo
import kotlinx.android.synthetic.main.activity_otp.*
import kotlinx.android.synthetic.main.activity_record_video.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates


class RecordVideoActivity : AppCompatActivity() {

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

        private var stopTimer = false
        private lateinit var fragmentList: ArrayList<Fragment>

    }

    private lateinit var threeSixtyDemoAdapter: ThreeSixtyShootDemoAdapter
    private lateinit var binding : ActivityRecordVideoBinding

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            binding.btnRecordVideo.visibility = View.VISIBLE

            setupDemo(intent.getIntExtra("shoot_mode",0))

            //startCamera();
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
            "${Environment.DIRECTORY_DCIM}/CameraXTest/"
        } else {
            "${getExternalFilesDir(Environment.DIRECTORY_DCIM)?.path}/CameraXTest/"
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_record_video)


        binding.btnFlash.setOnClickListener { toggleFlash() }

        binding.tvNext.setOnClickListener {
            if (binding.tvNext.text.toString() == "Next") {
                binding.tabLayout.getTabAt(1)?.select()
            } else if (binding.tvNext.text.toString() == "Got it"){
                binding.tabLayout.getTabAt(2)?.select()
            }
            else {
                binding.clShootDemo.visibility = View.GONE
                //disable video player
                var fragment : FragmentTwoThreeSixtyShootDemo = fragmentList.get(1) as FragmentTwoThreeSixtyShootDemo
                var fragmentTwo : FragmentTwoThreeSixtyShootDemo = fragmentList.get(2) as FragmentTwoThreeSixtyShootDemo

                fragment.releasePlayer()
                fragmentTwo.releasePlayer()

                startTimer()
            }
        }

        binding.ivBack.setOnClickListener { finish() }

        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                setPermissions()
            }
        }, 300)

    }

    private fun startRecordTime(millisUntilFinished: Long) {
        tv_timer.setText(
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

    private fun startTimer() {
        view_pager.visibility = View.GONE
        iv_timer.visibility = View.VISIBLE

        Glide.with(this).asGif().load(R.raw.timer_gif)
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    iv_timer.visibility = View.GONE

                    startCamera()
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<GifDrawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d(TAG, "onResourceReady: first")
                    resource?.setLoopCount(1);
                    resource?.registerAnimationCallback(object :
                        Animatable2Compat.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable) {
                            // Animation is done. update the UI or whatever you want
                            Log.d(TAG, "onResourceReady: end")
                            iv_timer.visibility = View.GONE
                            startCamera()
                        }
                    })

                    return false
                }

            })
            .into(iv_timer);
    }

    private fun setPermissions() {
        // Request camera permissions
        if (allPermissionsGranted()) {
            binding.btnRecordVideo.visibility = View.VISIBLE;

            setupDemo(intent.getIntExtra("shoot_mode",0))

        } else {
            permissionRequest.launch(permissions.toTypedArray())
        }
    }

    private fun setupDemo(shootMode : Int) {

       binding.clShootDemo.visibility = View.VISIBLE

        fragmentList = ArrayList<Fragment>()

        var args = Bundle()
        args.putInt("shoot_mode",shootMode)

        var fragmentOne =  FragmentOneThreeSixtyShootDemo()
        fragmentOne.arguments = args
        fragmentList.add(fragmentOne)

        var fragmentTwo =  FragmentTwoThreeSixtyShootDemo()
        fragmentTwo.arguments = args

        fragmentList.add(fragmentTwo)

        var fragmentThree =  FragmentTwoThreeSixtyShootDemo()
        fragmentThree.arguments = args

        fragmentList.add(fragmentThree)

        threeSixtyDemoAdapter = ThreeSixtyShootDemoAdapter(this,fragmentList)

        binding.viewPager.adapter = threeSixtyDemoAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
        }.attach()

        var next = ""
        var hint = ""

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                when(tab!!.position){
                    0 -> {
                        next = "Next"
                        hint = if (shootMode == 0)  "Shoot the front side of the car" else "Shoot the back side of the car"
                    } 1 -> {
                        next = "Got it"
                        hint = if (shootMode == 0)  "Sit on the middle of the front seat, Place the phone in the centre & start moving your wrist" else "Sit on the middle of the back seat, Place the phone in the centre & start moving your wrist"
                    } 2 -> {
                        next = "Begin Shoot"
                        hint = "Start moving your wrist and keep your hands steady. You can trim the video after shoot"
                    }
                }

                binding.tvHint.text = hint
                binding.tvNext.text = next
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    protected fun allPermissionsGranted() = RecordVideoActivity.permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
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

                binding.clHint.visibility = View.VISIBLE
                binding.btnRecordVideo.setOnClickListener { recordVideo() }

            } catch (e: Exception) {
                Log.e(RecordVideoTestActivity.TAG, "Failed to bind use cases", e)
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
        if (abs(previewRatio - RecordVideoActivity.RATIO_4_3_VALUE) <= abs(previewRatio - RecordVideoActivity.RATIO_16_9_VALUE)) {
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

            this.contentResolver.run {
                val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

                VideoCapture.OutputFileOptions.Builder(this, contentUri, contentValues)
            }
        } else {
            File(outputDirectory).mkdirs()
            val file = File("$outputDirectory/${System.currentTimeMillis()}.mp4")

            VideoCapture.OutputFileOptions.Builder(file)
        }.build()

        if (!isRecording) {
            //animateRecord.start()

                //start record timer && enable button click && flash button
                    stopTimer = false
            binding.tvTimer.visibility = View.VISIBLE
                    startRecordTime(0)
            binding.btnRecordVideo.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.bg_record_button_enabled))
           // binding.btnRecordVideo.setOnClickListener { recordVideo() }
            binding.btnFlash.visibility = View.VISIBLE



            localVideoCapture.startRecording(
                outputOptions, // the options needed for the final video
                ContextCompat.getMainExecutor(this), // the executor, on which the task will run
                object : VideoCapture.OnVideoSavedCallback { // the callback after recording a video
                    override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                        // Create small preview
                        outputFileResults.savedUri
                            ?.let { uri ->
                                //setGalleryThumbnail(uri)

                                val trimIntent = Intent(
                                    this@RecordVideoActivity,
                                    TrimVideoActivity::class.java
                                )

                                var skuId = if(intent.getIntExtra("shoot_mode",0) == 0) System.currentTimeMillis().toString() else intent.getStringExtra("sku_id")

                                trimIntent.putExtra("sku_id",skuId)
                                trimIntent.putExtra("shoot_mode",intent.getIntExtra("shoot_mode",0))
                                trimIntent.setData(uri);
                                startActivity(trimIntent);
                                Log.d(RecordVideoTestActivity.TAG, "Video saved in $uri")
                            }

                    }

                    override fun onError(
                        videoCaptureError: Int,
                        message: String,
                        cause: Throwable?
                    ) {
                        // This function is called if there is an error during recording process
                        animateRecord.cancel()
                        val msg = "Video capture failed: $message"
                        Toast.makeText(this@RecordVideoActivity, msg, Toast.LENGTH_SHORT).show()
                        Log.e(RecordVideoTestActivity.TAG, msg)
                        cause?.printStackTrace()
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



    override fun onBackPressed() = finish()

    override fun onStop() {
        super.onStop()
        camera?.cameraControl?.enableTorch(false)
    }

    fun ImageButton.toggleButton(
        flag: Boolean, rotationAngle: Float, @DrawableRes firstIcon: Int, @DrawableRes secondIcon: Int,
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