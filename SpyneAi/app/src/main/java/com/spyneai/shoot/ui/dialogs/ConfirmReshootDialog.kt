package com.spyneai.shoot.ui.dialogs

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.databinding.DialogConfirmReshootBinding
import com.spyneai.needs.AppConstants
import com.spyneai.posthog.Events
import com.spyneai.service.Actions
import com.spyneai.service.ImageUploadingService
import com.spyneai.service.getServiceState
import com.spyneai.service.log
import com.spyneai.shoot.data.ShootViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject


class ConfirmReshootDialog : BaseDialogFragment<ShootViewModel, DialogConfirmReshootBinding>() {

    val TAG = "ConfirmReshootDialog"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        binding.btReshootImage.setOnClickListener{
            viewModel.isCameraButtonClickable = true
            val properties = HashMap<String,Any?>()
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
                properties)

            viewModel.isCameraButtonClickable = true

            if (viewModel.isReshoot){
                uploadImages()

                if (viewModel.allReshootClicked)
                    viewModel.reshootCompleted.value = true

                dismiss()
            }else {
                when(viewModel.categoryDetails.value?.imageType) {
                    "Exterior" -> {
                        uploadImages()
                        if (viewModel.allExteriorClicked){
                            checkInteriorShootStatus()
                            viewModel.isCameraButtonClickable = false
                        }

                        dismiss()
                    }

                    "Interior" -> {
                        updateTotalImages()
                        uploadImages()

                        if (viewModel.allInteriorClicked){
                            viewModel.isCameraButtonClickable = false
                            viewModel.checkMiscShootStatus(getString(R.string.app_name))
                        }

                        dismiss()
                    }

                    "Focus Shoot" -> {
                        updateTotalImages()
                        uploadImages()

                        if (viewModel.allMisc)
                            viewModel.selectBackground(getString(R.string.app_name))

                        dismiss()
                    }
                }
            }
        }

        val uri = viewModel.shootData.value?.capturedImage
        
        Glide.with(requireContext())
            .load(uri)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d(TAG, "onLoadFailed: "+e?.localizedMessage)
                    Log.d(TAG, "onLoadFailed: "+e?.stackTrace)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d(TAG, "onResourceReady: ")
                    return false
                }

            })
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .dontAnimate()
            .into(binding.ivCapturedImage)


        when (viewModel.categoryDetails.value?.imageType) {
            "Exterior" -> {
                Glide.with(requireContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .dontAnimate()
                    .into(binding.ivCaptured2)

                if (getString(R.string.app_name) == AppConstants.KARVI)
                    binding.ivCapturedOverlay.visibility = View.GONE
                else
                    setOverlay(binding.ivCaptured2,viewModel.getOverlay())
            }

            else -> {
                binding.llImperfactions.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))

                Glide.with(requireContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .dontAnimate()
                    .into(binding.iv)

                binding.llBeforeAfter.visibility = View.INVISIBLE
            }
        }
    }

    private fun uploadImages() {
        viewModel.onImageConfirmed.value = viewModel.getOnImageConfirmed()
        //viewModel.shootData.value?.meta = getMetaValue()

       GlobalScope.launch(Dispatchers.IO) {
           viewModel.
           insertImage(viewModel.shootData.value!!)
       }

        //startService()
    }

    private fun startService() {
        var action = Actions.START
        if (getServiceState(requireContext()) == com.spyneai.service.ServiceState.STOPPED && action == Actions.STOP)
            return

        val serviceIntent = Intent(requireContext(), ImageUploadingService::class.java)
        serviceIntent.putExtra(AppConstants.SERVICE_STARTED_BY, ConfirmReshootDialog::class.simpleName)
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
        viewModel.updateTotalImages(viewModel.sku?.skuId!!)
    }

    private fun setOverlay(view: View, overlay : String) {
        Glide.with(requireContext())
            .load(overlay)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate()
            .into(binding.ivCapturedOverlay)
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