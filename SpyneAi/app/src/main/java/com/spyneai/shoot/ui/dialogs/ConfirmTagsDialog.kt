package com.spyneai.shoot.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.spyneai.databinding.DialogConfirmTagsBinding
import com.spyneai.needs.AppConstants
import com.spyneai.posthog.Events
import com.spyneai.service.Actions
import com.spyneai.service.ImageUploadingService
import com.spyneai.service.getServiceState
import com.spyneai.service.log
import com.spyneai.shoot.data.ShootViewModel
import kotlinx.coroutines.launch
import android.graphics.drawable.ColorDrawable

import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintSet
import com.spyneai.databinding.ItemTagsSpinnerBinding
import kotlinx.android.synthetic.main.activity_sign_up.*


class ConfirmTagsDialog : BaseDialogFragment<ShootViewModel, DialogConfirmTagsBinding>() {

    val TAG = "ConfirmTagsDialog"

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.getWindow()
                ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        setTagsData()

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
                        checkMiscShootStatus()
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
                        selectBackground()
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
                        .into(binding.ivClicked)


                    if (viewModel.categoryDetails.value?.imageType == "Exterior"){
                        val overlay = it.value.data[viewModel.shootNumber.value!!].display_thumbnail
                        val name = it.value.data[viewModel.shootNumber.value!!].display_name
                        binding.tvName.text = name

                        if (getString(R.string.app_name) == AppConstants.KARVI)
                            binding.ivOverlay.visibility = View.GONE
                        else
                            setOverlay(binding.ivOverlay,overlay)
                    }
                }
                else -> {}
            }
        })
    }

    private fun setTagsData() {
        when(viewModel.categoryDetails.value?.imageType){
            "Exterior" ->{
                repeat(3){
                    //inflate layout
                    val layout = LayoutInflater.from(requireContext()).inflate(R.layout.item_tags_spinner,null)
                    val itemBinding = ItemTagsSpinnerBinding.bind(layout)

                    itemBinding.tvTitle.text = "Sandeep Singh"

                    var countriesList = ArrayList<String>()
                    countriesList.add("Select")
                    countriesList.add("Front Bumper")
                    countriesList.add("Right Running Board")
                    countriesList.add("Left Running Board")
                    countriesList.add("Bonnet")

                    val spinnerAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        countriesList
                    )

                    spinnerAdapter.addAll(countriesList)

                    itemBinding.tvImageCategory.adapter = spinnerAdapter

                    binding.llTagsContainer.addView(layout)
                }
            }
        }
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

                viewModel.shootDimensions.value.let {
                    var prw = it?.previewWidth
                    var prh = it?.previewHeight

                    var ow = it?.overlayWidth
                    var oh = it?.overlayHeight

                    var newW =
                        ow!!.toFloat().div(prw!!.toFloat()).times(view.width)
                    var newH =
                        oh!!.toFloat().div(prh!!.toFloat()).times(view.height)

                    var equlizerOverlayMargin = (9.5 * resources.displayMetrics.density).toInt()

                    var params = ConstraintLayout.LayoutParams(newW.toInt(), newH.toInt())
                    params.bottomToBottom = ConstraintSet.PARENT_ID;
                    params.endToEnd = ConstraintSet.PARENT_ID;
                    params.startToStart = ConstraintSet.PARENT_ID;
                    params.topToTop = ConstraintSet.PARENT_ID;
                   // params. = Gravity.CENTER
                    params.topMargin = equlizerOverlayMargin

                    binding.ivOverlay.layoutParams = params

                    Glide.with(requireContext())
                        .load(overlay)
                        .into(binding.ivOverlay)
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
                            selectBackground()
                        }
                    }
                }
                else -> { }
            }
        })


    }

    private fun checkMiscShootStatus() {
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    when {
                        it.value.miscellaneous.isNotEmpty() -> {
                            viewModel.showMiscDialog.value = true
                        }
                        else -> {
                            selectBackground()
                        }
                    }
                }
                else -> { }
            }
        })
    }

    private fun selectBackground() {
        if(getString(R.string.app_name) == AppConstants.OLA_CABS)
            viewModel.show360InteriorDialog.value = true
        else
            viewModel.selectBackground.value = true
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogConfirmTagsBinding.inflate(inflater, container, false)
}