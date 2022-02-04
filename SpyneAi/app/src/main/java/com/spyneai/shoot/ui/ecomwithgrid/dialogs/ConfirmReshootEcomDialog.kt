package com.spyneai.shoot.ui.ecomwithgrid.dialogs

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

import com.spyneai.base.BaseDialogFragment
import com.spyneai.captureEvent
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.databinding.ConfirmReshootEcomDialogBinding
import com.spyneai.loadSmartly
import com.spyneai.needs.AppConstants
import com.spyneai.posthog.Events
import com.spyneai.service.Actions
import com.spyneai.service.ImageUploadingService
import com.spyneai.service.getServiceState
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.utils.log
import com.spyneai.startUploadServiceWithCheck
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.*


class ConfirmReshootEcomDialog :
    BaseDialogFragment<ShootViewModel, ConfirmReshootEcomDialogBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        val uri = viewModel.shootData.value?.capturedImage
//        binding.ivCapturedImage.setRotation(90F)

        viewModel.end.value = System.currentTimeMillis()
        val difference = (viewModel.end.value!! - viewModel.begin.value!!)/1000.toFloat()
        log("dialog- "+difference)

        requireContext().loadSmartly(uri,binding.ivCapturedImage)

//        Glide.with(requireContext())
//            .load(uri)
//            .diskCacheStrategy(DiskCacheStrategy.NONE)
//            .skipMemoryCache(true)
//            .into(binding.ivCapturedImage)

        binding.btReshootImage.setOnClickListener {
            viewModel.reshootCapturedImage.value = true
            viewModel.isCameraButtonClickable = true
            val properties = HashMap<String,Any?>()
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
            if (!viewModel.isReclick){
                viewModel.shootList.value?.let { list ->
                    val currentElement = list.firstOrNull {
                        it.overlayId == viewModel.overlayId
                    }

                    currentElement?.let {
                        list.remove(it)
                    }
                }
            }

            dismiss()
        }

        binding.btConfirmImage.setOnClickListener {
            viewModel.onImageConfirmed.value = true
            //viewModel.shootNumber.value = viewModel.shootNumber.value?.plus(1)

            viewModel.isStopCaptureClickable = true
            val properties = HashMap<String,Any?>()
            val cameraSetting = viewModel.getCameraSetting()

            properties.apply {
                put("image_data", JSONObject().apply {
                    put("sku_id",viewModel.shootData.value?.sku_id)
                    put("project_id",viewModel.shootData.value?.project_id)
                    put("image_type",viewModel.shootData.value?.image_category)
                    put("sequence",viewModel.shootData.value?.sequence)
                    put("name",viewModel.shootData.value?.name)
                    put("angle",viewModel.shootData.value?.angle)
                    put("overlay_id",viewModel.shootData.value?.overlayId)
                    put("debug_data",viewModel.shootData.value?.debugData)
                }.toString())
                put("camera_setting", JSONObject().apply {
                    put("is_overlay_active",cameraSetting.isOverlayActive)
                    put("is_grid_active",cameraSetting.isGridActive)
                    put("is_gyro_active",cameraSetting.isGryroActive)
                })
            }

            requireContext().captureEvent(
                Events.CONFIRMED,
                properties
            )

            viewModel.isCameraButtonClickable = true

            if (viewModel.isReshoot){
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.insertImage(viewModel.shootData.value!!)
                }

                requireContext().startUploadServiceWithCheck()

                if (viewModel.allReshootClicked)
                    viewModel.reshootCompleted.value = true

            }else {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.insertImage(viewModel.shootData.value!!)
                }

                requireContext().startUploadServiceWithCheck()
            }

            dismiss()
        }
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