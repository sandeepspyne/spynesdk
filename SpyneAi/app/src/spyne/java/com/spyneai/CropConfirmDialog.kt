package com.spyneai

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.posthog.android.Properties
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.FragmentCropConfirmDialogBinding
import com.spyneai.databinding.FragmentCropDialogBinding
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.utils.log
import com.theartofdev.edmodo.cropper.CropImage
import java.io.File

class CropConfirmDialog : BaseDialogFragment<ShootViewModel, FragmentCropConfirmDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uri = viewModel.shootData.value?.capturedImage
        binding.ivInfoCroppedImage.setRotation(90F)

        viewModel.end.value = System.currentTimeMillis()
        val difference = (viewModel.end.value!! - viewModel.begin.value!!)/1000.toFloat()
        log("dialog- "+difference)

        Glide.with(requireContext())
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.ivInfoCroppedImage)

        log("Image set to dialog: " + uri)

        binding.llReshoot.setOnClickListener {
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


        binding.llConfirm.setOnClickListener {
            viewModel.categoryDetails.value?.imageType = "InfoImage"
            dismiss()
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