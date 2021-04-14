package com.spyneai.videorecording

import android.Manifest
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
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.spyneai.model.beforeafter.Data
import kotlinx.android.synthetic.main.activity_otp.*
import kotlinx.android.synthetic.main.activity_record_video.*
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class RecordVideoActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CameraXTest"

        private const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
        private const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9

        private const val REQUEST_CODE_PERMISSIONS = 100
        var intent : Intent? = null;

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

        private lateinit var fragmentList: ArrayList<Fragment>

    }

    private lateinit var demoCollectionAdapter: ThreeSixtyShootDemoAdapter
    private lateinit var binding : ActivityRecordVideoBinding

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            binding.btnRecordVideo.visibility = View.VISIBLE

            binding.llSelectThreeSixtyMode.visibility = View.VISIBLE
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

        if (intent.getIntExtra("shoot_mode",0) == 1){
            binding.llSelectThreeSixtyMode.visibility = View.GONE

            setupDemo(1)
        }

        binding.llInterior.setOnClickListener{
            setupDemo(0)
        }

        binding.tvNext.setOnClickListener {
            if (binding.tvNext.text.toString() == "Next") {
                binding.tabLayout.getTabAt(1)?.select()
            } else {
                binding.clShootDemo.visibility = View.GONE
                //disable video player
                var fragment : FragmentTwoThreeSixtyShootDemo = fragmentList.get(1) as FragmentTwoThreeSixtyShootDemo
                fragment.releasePlayer()

                startTimer()
            }
        }

        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                setPermissions()

                binding.btnRecordVideo.setOnClickListener { recordVideo() }
            }
        }, 300)
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

            if (intent.getIntExtra("shoot_mode",0) == 1){
                binding.llSelectThreeSixtyMode.visibility = View.GONE

                setupDemo(1)
            }else{
                binding.llSelectThreeSixtyMode.visibility = View.VISIBLE
            }


        } else {
            permissionRequest.launch(RecordVideoActivity.permissions.toTypedArray())
        }
    }

    private fun setupDemo(shootMode : Int) {
        binding.llSelectThreeSixtyMode.visibility = View.GONE

        binding.clShootDemo.visibility = View.VISIBLE;

        fragmentList = ArrayList<Fragment>()

        var args = Bundle()
        args.putInt("shoot_mode",shootMode)

        if (shootMode == 1){
            binding.tvHint.text = "Shoot the back side of the car"
        }

        var fragmentOne =  FragmentOneThreeSixtyShootDemo()
        fragmentOne.arguments = args
        fragmentList.add(fragmentOne)

        var fragmentTwo =  FragmentTwoThreeSixtyShootDemo()
        fragmentTwo.arguments = args

        fragmentList.add(fragmentTwo)

        demoCollectionAdapter = ThreeSixtyShootDemoAdapter(this,fragmentList)

        binding.viewPager.adapter = demoCollectionAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab!!.position == 0) {
                    if (shootMode == 1){
                        binding.tvHint.text = "Shoot the back side of the car"
                    }else{
                        binding.tvHint.text = "Shoot the front side of the car"
                    }

                    binding.tvNext.text = "Next"
                } else {
                    binding.tvHint.text =
                        "Sit on the middle of the back seat, Place the phone in the centre & start moving your wrist"
                    binding.tvNext.text = "Begin Shoot"
                }
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
                // .setTargetResolution(Size(viewFinder.width,viewFinder.height))
                .setTargetAspectRatio(aspectRatio) // set the camera aspect ratio
                .setTargetRotation(rotation) // set the camera rotation
                .build()

            val videoCaptureConfig =
                VideoCapture.DEFAULT_CONFIG.config // default config for video capture
            // The Configuration of video capture
            videoCapture = VideoCapture.Builder
                .fromConfig(videoCaptureConfig)
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

                //start video recording
                recordVideo()
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
            animateRecord.start()
            localVideoCapture.startRecording(
                outputOptions, // the options needed for the final video
                ContextCompat.getMainExecutor(this), // the executor, on which the task will run
                object : VideoCapture.OnVideoSavedCallback { // the callback after recording a video
                    override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                        // Create small preview
                        outputFileResults.savedUri
                            ?.let { uri ->
                                //setGalleryThumbnail(uri)

                                val intent = Intent(
                                    this@RecordVideoActivity,
                                    TrimVideoActivity::class.java
                                );
                                intent.setData(uri);
                                startActivity(intent);
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
            animateRecord.cancel()
            localVideoCapture.stopRecording()
        }
        isRecording = !isRecording
    }

    override fun onBackPressed() = finish()

    override fun onStop() {
        super.onStop()
        camera?.cameraControl?.enableTorch(false)
    }
}