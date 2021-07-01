package com.spyneai.shoot.ui.ecom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.posthog.android.Properties
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.BaseFragment
import com.spyneai.captureEvent
import com.spyneai.databinding.ConfirmReshootEcomDialogBinding
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel

class ConfirmReshootEcomDialog :
    BaseDialogFragment<ShootViewModel, ConfirmReshootEcomDialogBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        val uri = viewModel.shootData.value?.capturedImage

        Glide.with(requireContext())
            .load(uri)
            .into(binding.ivCapturedImage)

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
            viewModel.uploadImageWithWorkManager(requireContext(), viewModel.shootData.value!!)
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