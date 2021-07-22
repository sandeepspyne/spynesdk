package com.spyneai.threesixty.ui

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
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.animation.doOnCancel
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentRecordVideoBinding
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.ui.dialogs.ShootExitDialog
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import com.spyneai.toggleButton
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

class RecordVideoFragment : BaseFragment<ThreeSixtyViewModel,FragmentRecordVideoBinding>(),
    PickiTCallbacks,Orientation.Listener {


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
            "${requireActivity().getExternalFilesDir(Environment.DIRECTORY_DCIM)?.path}/CameraXTest/"
        }
    }

    //Declare PickiT
    var pickiT: PickiT? = null
    private var mOrientation: Orientation? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pickiT = PickiT(requireContext(), this, requireActivity())

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

                mOrientation = Orientation(requireActivity())
                binding.myLevelIndicator.visibility = View.VISIBLE
                binding.myLevelIndicator.tvDemo = binding.tvWarning
                mOrientation?.startListening(this)

                binding.btnFlash.visibility = View.VISIBLE

                binding.btnFlash.setOnClickListener { toggleFlash() }

                binding.btnRecordVideo.setOnClickListener {
                    recordVideo()
//                    if (isRecording){
//                        recordVideo()
//                    }else{
//                        startTimer()
//                    }
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
            binding.tvStart.text = "Stop"
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

    private fun startTimer() {

        binding.ivTimer.visibility = View.VISIBLE

        Glide.with(this).asGif().load(R.raw.timer_gif)
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.ivTimer.visibility = View.GONE
                    //start recording
                    recordVideo()
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
                            binding.ivTimer.visibility = View.GONE

                            //start recording
                            recordVideo()
                        }
                    })

                    return false
                }

            })
            .into(binding.ivTimer)
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
    }

    override fun onOrientationChanged(pitch: Float, roll: Float) {
        binding.myLevelIndicator.setAttitude(pitch, roll)
    }

    override fun updateViews(inLevel: Boolean) {
        if (isRecording){
            if (inLevel){
                binding.tvWarning.visibility = View.VISIBLE
            }else{
                binding.tvWarning.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
            mOrientation?.stopListening()
        super.onDestroy()
    }


    override fun onResume() {
            mOrientation?.startListening(this)
        super.onResume()
    }

    override fun onPause() {
            mOrientation?.stopListening()
        super.onPause()
    }
}