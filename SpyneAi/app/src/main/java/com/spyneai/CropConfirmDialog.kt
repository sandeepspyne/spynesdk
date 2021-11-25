package com.spyneai

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.FragmentCropConfirmDialogBinding
import com.spyneai.posthog.Events
import com.spyneai.service.Actions
import com.spyneai.service.ImageUploadingService
import com.spyneai.service.getServiceState
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.utils.log
import kotlinx.coroutines.launch

class CropConfirmDialog : BaseDialogFragment<ShootViewModel, FragmentCropConfirmDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        val uri = viewModel.shootData.value?.capturedImage
//        binding.ivInfoCroppedImage.setRotation(90F)

        viewModel.end.value = System.currentTimeMillis()
        val difference = (viewModel.end.value!! - viewModel.begin.value!!)/1000.toFloat()
        log("dialog- "+difference)

        Glide.with(requireContext())
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.ivInfoCroppedImage)

        log("Image set to dialog: " + uri)

        binding.tvEndProject.setOnClickListener {
            viewModel.onImageConfirmed.value = true
            viewModel.isStopCaptureClickable = true
            val properties = HashMap<String,Any?>()
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


            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.insertImage(viewModel.shootData.value!!)
            }

            startService()
            viewModel.stopShoot.value = true
            dismiss()
        }

        binding.llShootAnother.setOnClickListener {
            viewModel.onImageConfirmed.value = true
            //viewModel.shootNumber.value = viewModel.shootNumber.value?.plus(1)

            viewModel.isStopCaptureClickable = true
            val properties = HashMap<String,Any?>()
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

    override fun onResume() {
        super.onResume()

        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.getWindow()?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCropConfirmDialogBinding.inflate(inflater, container, false)
}