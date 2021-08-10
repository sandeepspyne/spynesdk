package com.spyneai.shoot.ui.dialogs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.posthog.android.Properties
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.databinding.DialogConfirmReshootBinding
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.utils.shoot
import kotlinx.coroutines.launch
import java.io.File

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

            val file = File(viewModel.shootList.value?.get(viewModel.shootList.value!!.size - 1)?.capturedImage)

            if (file.exists())
                file.delete()

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
                    if (viewModel.shootNumber.value == viewModel.exterirorAngles.value?.minus(1)) {
                        dismiss()
                        Log.d(TAG, "onViewCreated: "+"checkInteriorShootStatus")
                        checkInteriorShootStatus()
                    } else {
                        viewModel.shootNumber.value = viewModel.shootNumber.value!! + 1
                        dismiss()
                    }
                }

                "Interior" -> {
                    updateTotalImages()
                    uploadImages()
                    if (viewModel.interiorShootNumber.value  == viewModel.interiorAngles.value?.minus(1)){
                        viewModel.showMiscDialog.value = true
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
                        viewModel.selectBackground.value = true
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

                    Glide.with(requireContext())
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(binding.ivCapturedImage)

                    if (viewModel.categoryDetails.value?.imageType == "Exterior"){

                        val overlay = it.value.data[viewModel.shootNumber.value!!].display_thumbnail

                        Glide.with(requireContext())
                            .load(uri)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding.ivCaptured2)

                        setOverlay(binding.ivCaptured2,overlay)

                    }else{
                       binding.flAfter.visibility = View.GONE
                    }
               }
               else -> {}
           }
       })
    }

    private fun uploadImages() {
//        viewModel.uploadImageWithWorkManager(
//            viewModel.shootData.value!!
//        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.
            insertImage(viewModel.shootData.value!!)
        }
    }

    private fun updateTotalImages() {
        viewModel.updateTotalImages(viewModel.sku.value?.skuId!!)
    }

    private fun setOverlay(view: View, overlay : String) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                viewModel.shootDimensions.value.let {
                    var prw = it?.previewWidth
                    var prh = it?.previewHeight

                    var ow = it?.overlayWidth
                    var oh = it?.overlayHeight


                    Log.d(TAG, "onGlobalLayout: "+prw)
                    Log.d(TAG, "onGlobalLayout: "+prh)

                    Log.d(TAG, "onGlobalLayout: "+ow)
                    Log.d(TAG, "onGlobalLayout: "+oh)

                    Log.d(TAG, "onGlobalLayout: "+view.width)
                    Log.d(TAG, "onGlobalLayout: "+view.height)

                    var newW =
                        ow!!.toFloat().div(prw!!.toFloat()).times(view.width)
                    var newH =
                        oh!!.toFloat().div(prh!!.toFloat()).times(view.height)

                    var equlizerOverlayMargin = (9.5 * resources.displayMetrics.density).toInt()

                    var params = FrameLayout.LayoutParams(newW.toInt(), newH.toInt())
                    params.gravity = Gravity.CENTER
                    params.topMargin = equlizerOverlayMargin

                    binding.ivCapturedOverlay.layoutParams = params

                    Glide.with(requireContext())
                        .load(overlay)
                        .into(binding.ivCapturedOverlay)
                }
            }
        })
    }

    private fun checkInteriorShootStatus() {
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Log.d(TAG, "checkInteriorShootStatus: "+"sucess")
                    when {
                        it.value.interior.isNotEmpty() -> {
                            viewModel.showInteriorDialog.value = true
                            Log.d(TAG, "checkInteriorShootStatus: "+"interior")
                        }
                        it.value.miscellaneous.isNotEmpty() -> {
                            viewModel.showMiscDialog.value = true
                            Log.d(TAG, "checkInteriorShootStatus: "+"miscellaneous")
                        }
                        else -> {
                            viewModel.selectBackground.value = true
                            Log.d(TAG, "checkInteriorShootStatus: "+"selectBackground")
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "checkInteriorShootStatus: "+"failed")
                    var s = ""
                }
            }
        })


    }

    override fun onStop() {
        super.onStop()
        shoot("onStop called(confirmReshootDialog-> dismissAllowingStateLoss)")
        dismissAllowingStateLoss()
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogConfirmReshootBinding.inflate(inflater, container, false)
}