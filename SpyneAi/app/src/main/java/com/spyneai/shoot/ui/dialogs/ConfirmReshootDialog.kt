package com.spyneai.shoot.ui.dialogs

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.databinding.DialogConfirmReshootBinding
import com.spyneai.needs.AppConstants
import com.spyneai.posthog.Events
import com.spyneai.service.Actions
import com.spyneai.service.ImageUploadingService
import com.spyneai.service.getServiceState
import com.spyneai.service.log
import com.spyneai.shoot.data.ShootViewModel
import kotlinx.coroutines.launch


class ConfirmReshootDialog : BaseDialogFragment<ShootViewModel, DialogConfirmReshootBinding>() {

    val TAG = "ConfirmReshootDialog"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        binding.btReshootImage.setOnClickListener{
            viewModel.isCameraButtonClickable = true
            val properties = Properties()
            properties.apply {
                this["sku_id"] = viewModel.shootData.value?.sku_id
                this["project_id"] = viewModel.shootData.value?.project_id
                this["image_type"] = viewModel.shootData.value?.image_category
            }

            requireContext().captureEvent(
                Events.RESHOOT,
                properties)

//            val file = File(viewModel.shootList.value?.get(viewModel.shootList.value!!.size - 1)?.capturedImage)
//
//            if (file.exists())
//                file.delete()

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
                properties)

            viewModel.isCameraButtonClickable = true

            when(viewModel.categoryDetails.value?.imageType) {
                "Exterior" -> {
                    uploadImages()

                    if (viewModel.shootNumber.value  == viewModel.exterirorAngles.value?.minus(1)){
                        checkInteriorShootStatus()
                        viewModel.isCameraButtonClickable = false
                        dismiss()
                    }else{
                        viewModel.shootNumber.value = viewModel.shootNumber.value!! + 1
                        dismiss()
                    }
                }

                "Interior" -> {
                    updateTotalImages()
                    uploadImages()

                    if (viewModel.interiorShootNumber.value  == viewModel.interiorAngles.value?.minus(1)){
                        viewModel.isCameraButtonClickable = false
                        viewModel.checkMiscShootStatus(getString(R.string.app_name))
                        dismiss()
                    }else{
                        viewModel.interiorShootNumber.value = viewModel.interiorShootNumber.value!! + 1
                        dismiss()
                    }
                }

                "Focus Shoot" -> {
                    updateTotalImages()
                    uploadImages()

                    if (viewModel.miscShootNumber.value  == viewModel.miscAngles.value?.minus(1)){
                        viewModel.selectBackground(getString(R.string.app_name))
                        dismiss()
                    }else{
                        viewModel.miscShootNumber.value = viewModel.miscShootNumber.value!! + 1
                        dismiss()
                    }
                }
            }
        }

       viewModel.overlaysResponse.observe(viewLifecycleOwner,{
           when(it){
                is Resource.Success -> {
                    val uri = viewModel.shootData.value?.capturedImage


                    if (viewModel.categoryDetails.value?.imageType == "Exterior"){
                        val overlay = it.value.data[viewModel.shootNumber.value!!].display_thumbnail

                        Glide.with(requireContext())
                            .load(uri)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding.ivCapturedImage)

                        Glide.with(requireContext())
                            .load(uri)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding.ivCaptured2)

                        if (getString(R.string.app_name) == AppConstants.KARVI)
                            binding.ivCapturedOverlay.visibility = View.GONE
                        else
                            setOverlay(binding.ivCaptured2,overlay)

                    }else{
                        binding.llImperfactions.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))

                        Glide.with(requireContext())
                            .load(uri)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding.iv)

                       binding.llBeforeAfter.visibility = View.INVISIBLE
                    }
               }
               else -> {}
           }
       })
    }

    private fun uploadImages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.
            insertImage(viewModel.shootData.value!!)
        }

        startService()
    }

    private fun startService() {
        var action = Actions.START
        if (getServiceState(requireContext()) == com.spyneai.service.ServiceState.STOPPED && action == Actions.STOP)
            return

        val serviceIntent = Intent(requireContext(), ImageUploadingService::class.java)
        serviceIntent.action = action.name

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            log("Starting the service in >=26 Mode")
            ContextCompat.startForegroundService(requireContext(), serviceIntent)
            return
        } else {
            log("Starting the service in < 26 Mode")
            requireActivity().startService(serviceIntent)
        }
    }

    fun updateTotalImages() {
        viewModel.updateTotalImages(viewModel.sku.value?.skuId!!)
    }

    private fun setOverlay(view: View, overlay : String) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                Glide.with(requireContext())
                    .load(overlay)
                    .into(binding.ivCapturedOverlay)

                viewModel.shootDimensions.value.let {
//                    var prw = it?.previewWidth
//                    var prh = it?.previewHeight
//
//                    var ow = it?.overlayWidth
//                    var oh = it?.overlayHeight
//
//                    var newW =
//                        ow!!.toFloat().div(prw!!.toFloat()).times(view.width)
//                    var newH =
//                        oh!!.toFloat().div(prh!!.toFloat()).times(view.height)
//
//                    var equlizerOverlayMargin = (9.5 * resources.displayMetrics.density).toInt()
//
//                    var params = FrameLayout.LayoutParams(newW.toInt(), newH.toInt())
//                    params.gravity = Gravity.CENTER
//                    params.topMargin = equlizerOverlayMargin
//
//                    binding.ivCapturedOverlay.layoutParams = params


                }
            }
        })
    }

    private fun checkInteriorShootStatus() {
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    when {
                        it.value.interior.isNotEmpty() -> {
                            viewModel.showInteriorDialog.value = true
                        }
                        it.value.miscellaneous.isNotEmpty() -> {
                            viewModel.showMiscDialog.value = true
                        }
                        else -> {
                            viewModel.selectBackground(getString(R.string.app_name))
                        }
                    }
                }
                else -> { }
            }
        })


    }



    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogConfirmReshootBinding.inflate(inflater, container, false)
}