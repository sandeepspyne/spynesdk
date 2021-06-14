package com.spyneai.shoot.ui

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.hbisoft.pickit.PickiT
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentCameraBinding
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


class CameraFragment : BaseFragment<ShootViewModel, FragmentCameraBinding>() {
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var shootList : ArrayList<ShootData>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var outputDirectory: File
    private var capturedImage = ""
    var pickiT: PickiT? = null
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private var projectId: String = "prj-27d33afa-4f50-4af0-b769-a97adf247fae"
    private var skuId: String = "sku-9c0775d2-69e4-4ecf-a134-7b61a48e15ee\n"
    private var imageCategory: String = "Exterior"
    private var authKey: String = "813a71af-a2fb-4ef8-87b3-059d01c5b9ba"
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    lateinit var photoFile: File

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        // Determine the output directory
        outputDirectory = ShootActivity.getOutputDirectory(requireContext())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        shootList = ArrayList<ShootData>()
        startCamera()
        binding.btnTakePicture?.setOnClickListener { takePhoto() }

        binding.ivUploadedImage?.setOnClickListener {
            binding.ivUploadedImage!!.visibility = View.GONE
        }

    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            val a: Activity? = activity
            if (a != null) a.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return


        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Log.d(TAG, msg)
                    try {
                        capturedImage = photoFile?.path!!.toString()
                    } catch (ex: IllegalArgumentException) {
//                        pickiT?.getPath(finalLogoUri, Build.VERSION.SDK_INT)
                    }
                    shootList.add(
                        ShootData(capturedImage, "prj-27d33afa-4f50-4af0-b769-a97adf247fae",
                            "sku-9c0775d2-69e4-4ecf-a134-7b61a48e15ee",
                            "Exterior",
                            "813a71af-a2fb-4ef8-87b3-059d01c5b9ba"))
                    viewModel.setShoot(shootList)
                }
            })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )

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


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCameraBinding.inflate(inflater, container, false)

}
