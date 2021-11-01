package com.spyneai.shoot.ui.ecomwithgrid.dialogs

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.posthog.android.Properties
import com.spyneai.base.BaseDialogFragment
import com.spyneai.captureEvent
import com.spyneai.databinding.ConfirmReshootEcomDialogBinding
import com.spyneai.posthog.Events
import com.spyneai.service.Actions
import com.spyneai.service.ImageUploadingService
import com.spyneai.service.getServiceState
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.utils.log
import kotlinx.coroutines.launch
import java.io.*


class ConfirmReshootEcomDialog :
    BaseDialogFragment<ShootViewModel, ConfirmReshootEcomDialogBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        val uri = viewModel.shootData.value?.capturedImage
        binding.ivCapturedImage.setRotation(90F)

        viewModel.end.value = System.currentTimeMillis()
        val difference = (viewModel.end.value!! - viewModel.begin.value!!)/1000.toFloat()
        log("dialog- "+difference)

        Glide.with(requireContext())
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.ivCapturedImage)

        log("Image set to dialog: " + uri)

        binding.btReshootImage.setOnClickListener {
            viewModel.reshootCapturedImage.value = true
            viewModel.isCameraButtonClickable = true
            val properties = Properties()
            properties.apply {
                this["sku_id"] = viewModel.shootData.value?.sku_id
                this["project_id"] = viewModel.shootData.value?.project_id
                this["image_type"] = viewModel.shootData.value?.image_category
            }
            requireContext().captureEvent(
                Events.RESHOOT,
                properties
            )

            //remove last item from shoot list
            viewModel.shootList.value?.removeAt(viewModel.shootList.value!!.size - 1)

            dismiss()
        }

        binding.btConfirmImage.setOnClickListener {
            viewModel.confirmCapturedImage.value = true
            viewModel.shootNumber.value = viewModel.shootNumber.value?.plus(1)

            viewModel.isStopCaptureClickable = true
            val properties = Properties()
            properties.apply {
                this["sku_id"] = viewModel.shootData.value?.sku_id
                this["project_id"] = viewModel.shootData.value?.project_id
                this["image_type"] = viewModel.shootData.value?.image_category
            }


            requireContext().captureEvent(
                Events.CONFIRMED,
                properties
            )

            viewModel.isCameraButtonClickable = true
//            viewModel.uploadImageWithWorkManager(viewModel.shootData.value!!)
            val bitmap = modifyOrientation(BitmapFactory.decodeFile(uri), uri)
            try {
                val file = File(uri)
                val os: OutputStream = BufferedOutputStream(FileOutputStream(file))
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.close()
            } catch (
                e: java.lang.Exception
            ) {
                val s = ""
            }


            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.insertImage(viewModel.shootData.value!!)
            }

            startService()
            dismiss()
        }
    }

    private fun startService() {
        var action = Actions.START
        if (getServiceState(requireContext()) == com.spyneai.service.ServiceState.STOPPED && action == Actions.STOP)
            return

        val serviceIntent = Intent(requireContext(), ImageUploadingService::class.java)
        serviceIntent.action = action.name

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            com.spyneai.service.log("Starting the service in >=26 Mode")
            ContextCompat.startForegroundService(requireContext(), serviceIntent)
            return
        } else {
            com.spyneai.service.log("Starting the service in < 26 Mode")
            requireActivity().startService(serviceIntent)
        }
    }

    @Throws(IOException::class)
    fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String?): Bitmap? {
        val ei = ExifInterface(image_absolute_path!!)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(bitmap, true, false)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(bitmap, false, true)
            else -> bitmap
        }
    }

    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap? {
        val matrix = Matrix()
        matrix.preScale((if (horizontal) -1 else 1.toFloat()) as Float,
            (if (vertical) -1 else 1.toFloat()) as Float
        )
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onResume() {
        super.onResume()
        dialog?.getWindow()?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        );
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = ConfirmReshootEcomDialogBinding.inflate(inflater, container, false)

}