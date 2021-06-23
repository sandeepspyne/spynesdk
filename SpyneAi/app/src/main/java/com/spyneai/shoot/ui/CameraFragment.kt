package com.spyneai.shoot.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.posthog.android.Properties
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
import com.spyneai.shoot.ui.dialogs.InteriorHintDialog
import com.spyneai.shoot.ui.dialogs.SubCategoryConfirmationDialog
import kotlinx.android.synthetic.main.activity_camera.viewFinder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


class CameraFragment : BaseFragment<ShootViewModel, FragmentCameraBinding>(),PickiTCallbacks {
    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var outputDirectory: File
    var pickIt : PickiT? = null
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private var flashMode: Int = ImageCapture.FLASH_MODE_OFF

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0 // aspect ratio 4x3
        private const val RATIO_16_9_VALUE = 16.0 / 9.0 // aspect ratio 16x9
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        // Determine the output directory
        outputDirectory = ShootActivity.getOutputDirectory(requireContext())
        pickIt = PickiT(requireContext(),this,requireActivity())

        viewModel.startInteriorShots.observe(viewLifecycleOwner,{
            if (it) binding.tvSkipShoot?.visibility = View.VISIBLE
        })

        viewModel.startMiscShots.observe(viewLifecycleOwner,{
            if (it) binding.tvSkipShoot?.visibility = View.VISIBLE
        })

        binding.tvSkipShoot?.setOnClickListener {
            when(viewModel.categoryDetails.value?.imageType){
                "Interior" -> {
                    if (viewModel.interiorShootNumber.value  == viewModel.interiorAngles.value?.minus(1)){
                        viewModel.showMiscDialog.value = true
                    }else{
                        viewModel.interiorShootNumber.value = viewModel.interiorShootNumber.value!! + 1
                    }
                }

                "Focus Shoot" -> {
                    if (viewModel.miscShootNumber.value  == viewModel.miscAngles.value?.minus(1)){
                        viewModel.selectBackground.value = true
                    }else{
                        viewModel.miscShootNumber.value = viewModel.miscShootNumber.value!! + 1
                    }
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        startCamera()

        binding.cameraCaptureButton?.setOnClickListener {
            if (viewModel.isSubCategoryConfirmed.value == null || viewModel.isSubCategoryConfirmed.value == false){
                SubCategoryConfirmationDialog().show(requireFragmentManager(), "SubCategoryConfirmationDialog")
            }else{
                takePhoto()
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

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            //for exact image cropping
            val viewPort = binding.viewFinder?.viewPort

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(flashMode).build()

            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageCapture!!)
                .setViewPort(viewPort!!)
                .build()


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, useCaseGroup
                )

                if (viewModel.shootDimensions.value == null ||
                    viewModel.shootDimensions.value?.previewHeight == 0){
                    getPreviewDimensions(binding.viewFinder!!)
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
                    requireContext().captureFailureEvent(Events.IMAGE_CAPRURE_FAILED,Properties(),exc.localizedMessage)
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Log.d(TAG, msg)
                    try {
                       addShootItem(photoFile?.path!!.toString())
                    } catch (ex: IllegalArgumentException) {
                        pickIt?.getPath(savedUri, Build.VERSION.SDK_INT)
                    }

                }
            })
    }

    private fun getPreviewDimensions(view : View) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val shootDimensions = ShootDimensions()
                shootDimensions.previewWidth = view.width
                shootDimensions.previewHeight = view.height


                viewModel.shootDimensions.value = shootDimensions
            }
        })
    }

    private fun addShootItem(capturedImage : String) {
        if (viewModel.shootList.value == null)
            viewModel.shootList.value = ArrayList()

        viewModel.shootList.value!!.add(ShootData(capturedImage,
            viewModel.sku.value?.projectId!!,
            viewModel.sku.value?.skuId!!,
            viewModel.categoryDetails.value?.imageType!!,
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString()))

        viewModel.shootList.value = viewModel.shootList.value

        val properties = Properties()
        properties.apply {
            this["project_id"] = viewModel.sku.value?.projectId!!
            this["sku_id"] = viewModel.sku.value?.skuId!!
            this["image_type"] = viewModel.categoryDetails.value?.imageType!!
        }

        requireContext().captureEvent(Events.IMAGE_CAPTURED,properties)
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
