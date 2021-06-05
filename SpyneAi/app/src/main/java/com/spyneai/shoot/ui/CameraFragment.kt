package com.spyneai.shoot.ui

import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.pm.ActivityInfo
import android.os.*
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.extensions.HdrImageCaptureExtender
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.robertlevonyan.demo.camerax.analyzer.LuminosityAnalyzer
import com.spyneai.databinding.FragmentCameraBinding
import com.spyneai.shoot.utils.ThreadExecutor
import com.spyneai.shoot.utils.mainExecutor
import java.io.File
import java.util.concurrent.ExecutionException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class CameraFragment : Fragment() {

    private var fragmentCameraBinding: FragmentCameraBinding? = null
    private val binding get() = fragmentCameraBinding!!

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null

    // Selector showing which camera is selected (front or back)
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
        private const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)

        startCamera()

        binding.btnTakePicture?.setOnClickListener { captureImage() }
        binding.ivCapturedImage?.setOnClickListener {
            binding.ivCapturedImage!!.visibility = View.GONE
        }

        return binding.root
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            val a: Activity? = activity
            if (a != null) a.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    //The folder location where all the files will be stored
    protected val outputDirectory: String by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${Environment.DIRECTORY_DCIM}/Spyne/"
        } else {
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/Spyne/"
        }
    }

    private fun captureImage() {
        val localImageCapture =
            imageCapture ?: throw IllegalStateException("Camera initialization failed.")

        // Setup image capture metadata
        val metadata = ImageCapture.Metadata().apply {
            // Mirror image when using the front camera
            isReversedHorizontal = lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA
        }

        // Options fot the output image file
        val outputOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis())
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
            val file = File(outputDirectory, "${System.currentTimeMillis()}.jpg")

            ImageCapture.OutputFileOptions.Builder(file)
        }.setMetadata(metadata).build()

        localImageCapture.takePicture(
            outputOptions, // the options needed for the final image
            requireContext().mainExecutor(), // the executor, on which the task will run
            object :
                ImageCapture.OnImageSavedCallback { // the callback, about the result of capture process
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // This function is called if capture is successfully completed
                    outputFileResults.savedUri
                        ?.let { uri ->
                            binding.ivCapturedImage!!.setImageURI(uri)
                            binding.ivCapturedImage!!.visibility = View.VISIBLE
                        }
                }

                override fun onError(exception: ImageCaptureException) {
                    // This function is called if there is an errors during capture process
                    val msg = "Photo capture failed: ${exception.message}"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.e(TAG, msg)
                    exception.printStackTrace()
                }
            }
        )
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun startCamera() {
        // This is the CameraX PreviewView where the camera will be rendered
        val viewFinder = binding.viewFinder

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
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
            val metrics = DisplayMetrics().also { viewFinder?.display?.getRealMetrics(it) }
            // The ratio for the output image and preview
            val aspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            // The display rotation
            val rotation = viewFinder?.display?.rotation

            val localCameraProvider = cameraProvider
                ?: throw IllegalStateException("Camera initialization failed.")

            // The Configuration of camera preview
            preview = Preview.Builder()
                .setTargetAspectRatio(aspectRatio) // set the camera aspect ratio
              //  .setTargetRotation(rotation!!) // set the camera rotation
                .build()

            // The Configuration of image capture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // setting to have pictures with highest quality possible (may be slow)
                .setTargetAspectRatio(aspectRatio) // set the capture aspect ratio
              //  .setTargetRotation(rotation) // set the capture rotation
                .also {
                    // Create a Vendor Extension for HDR
                    val hdrImageCapture = HdrImageCaptureExtender.create(it)

                    // Check if the extension is available on the device
//                    if (!hdrImageCapture.isExtensionAvailable(CameraSelector.DEFAULT_BACK_CAMERA)) {
//                        // If not, hide the HDR button
//                        binding.btnHdr.visibility = View.GONE
//                    } else if (hasHdr) {
//                        // If yes, turn on if the HDR is turned on by the user
//                        hdrImageCapture.enableExtension(lensFacing)
//                    }
                }
                .build()

            // The Configuration of image analyzing
            imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(aspectRatio) // set the analyzer aspect ratio
               // .setTargetRotation(rotation) // set the analyzer rotation
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

            localCameraProvider.unbindAll() // unbind the use-cases before rebinding them

            try {
                // Bind all use cases to the camera with lifecycle
                localCameraProvider.bindToLifecycle(
                    viewLifecycleOwner, // current lifecycle owner
                    lensFacing, // always back facing
                    preview, // camera preview use case
                    imageCapture, // image capture use case
                    imageAnalyzer, // image analyzer use case
                )

                // Attach the viewfinder's surface provider to preview use case
                preview?.setSurfaceProvider(viewFinder?.surfaceProvider)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind use cases", e)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

}