package com.spyneai.shoot.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.databinding.DialogConfirmTagsBinding
import com.spyneai.databinding.ItemTagNotesBinding
import com.spyneai.databinding.ItemTagsSpinnerBinding
import com.spyneai.needs.AppConstants
import com.spyneai.posthog.Events
import com.spyneai.service.Actions
import com.spyneai.service.ImageUploadingService
import com.spyneai.service.getServiceState
import com.spyneai.service.log
import com.spyneai.shoot.data.ShootViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject


class ConfirmTagsDialog : BaseDialogFragment<ShootViewModel, DialogConfirmTagsBinding>() {

    val TAG = "ConfirmTagsDialog"
    private val bindingMap = HashMap<String, ArrayList<ViewBinding>>()
    private lateinit var inflator: LayoutInflater

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.getWindow()
                ?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inflator = LayoutInflater.from(requireContext())

        isCancelable = false

        binding.tvImageCategory.text = viewModel.categoryDetails.value?.imageType

        setTagsData()

        binding.btReshootImage.setOnClickListener {
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

            when (viewModel.categoryDetails.value?.imageType) {
                "Exterior" -> {
                    if (tagsValid()) {
                        uploadImages()

                        dismiss()

//                        if (viewModel.shootNumber.value == viewModel.exterirorAngles.value?.minus(1)) {
//                            checkInteriorShootStatus()
//                            viewModel.isCameraButtonClickable = false
//                            dismiss()
//                        } else {
//                            viewModel.shootNumber.value = viewModel.shootList.value?.size
//                            dismiss()
//                        }
                    }

                }

                "Interior" -> {
                    if (tagsValid()) {
                        updateTotalImages()
                        uploadImages()

                        if (viewModel.interiorShootNumber.value == viewModel.interiorAngles.value?.minus(
                                1
                            )
                        ) {
                            viewModel.isCameraButtonClickable = false
                            checkMiscShootStatus()
                            dismiss()
                        } else {
                            viewModel.interiorShootNumber.value =
                                viewModel.interiorShootNumber.value!! + 1
                            dismiss()
                        }
                    }
                }

                "Focus Shoot" -> {
                    if (tagsValid()) {
                        updateTotalImages()
                        uploadImages()

                        if (viewModel.miscShootNumber.value == viewModel.miscAngles.value?.minus(1)) {
                            selectBackground()
                            dismiss()
                        } else {
                            viewModel.miscShootNumber.value = viewModel.miscShootNumber.value!! + 1
                            dismiss()
                        }
                    }
                }
            }
        }

        val uri = viewModel.shootData.value?.capturedImage

        Glide.with(requireContext())
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.ivClicked)

        when (viewModel.categoryDetails.value?.imageType) {
            "Exterior" -> {
               // binding.tvName.text = viewModel.getName()

//                if (getString(R.string.app_name) == AppConstants.KARVI)
//                    binding.ivOverlay.visibility = View.GONE
//                else
//                    setOverlay(binding.ivOverlay, viewModel.getOverlay())
            }

            "Interior" -> {
                val subCatResponse =
                    (viewModel.subCategoriesResponse.value as Resource.Success).value
                val name =
                    subCatResponse.interior[viewModel.interiorShootNumber.value!!].display_name
                binding.tvName.text = name
            }

            "Focus Shoot" -> {
                val subCatResponse =
                    (viewModel.subCategoriesResponse.value as Resource.Success).value
                val name =
                    subCatResponse.miscellaneous[viewModel.miscShootNumber.value!!].display_name
                binding.tvName.text = name
            }
        }
    }

    private fun setTagsData() {
        val response = (viewModel.subCategoriesResponse.value as Resource.Success).value

        when (viewModel.categoryDetails.value?.imageType) {
            "Exterior" -> {
                val tags = response.tags.exterior

                tags?.forEach {
                    addBinding(
                        viewModel.categoryDetails.value!!.imageType!!,
                        it.fieldType,
                        getItemBinding(it.fieldType, it.fieldName, it.enumValues)
                    )
                }
            }

            "Interior" -> {
                val tags = response.tags.exterior

                tags?.forEach {
                    addBinding(
                        viewModel.categoryDetails.value!!.imageType!!,
                        it.fieldType,
                        getItemBinding(it.fieldType, it.fieldName, it.enumValues)
                    )
                }
            }


            "Focus Shoot" -> {
                val tags = response.tags.focusShoot

                tags?.forEach {
                    addBinding(
                        viewModel.categoryDetails.value!!.imageType!!,
                        it.fieldType,
                        getItemBinding(it.fieldType, it.fieldName, it.enumValues)
                    )
                }
            }
        }

    }

    private fun getItemBinding(
        fieldType: String,
        fieldName: String,
        list: List<String>
    ): ViewBinding {
        var tempBinding: ViewBinding? = null

        when (fieldType) {
            "dropdown" -> {
                val layout = inflator.inflate(R.layout.item_tags_spinner, null)
                val itemBinding = ItemTagsSpinnerBinding.bind(layout)

                itemBinding.tvTitle.text = fieldName

                val dataList = ArrayList<String>()
                dataList.add("Select")

                list.forEach {
                    dataList.add(it)
                }

                val spinnerAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.item_tags_spinner_text,
                    dataList
                )

                itemBinding.spinner.adapter = spinnerAdapter

                tempBinding = itemBinding
                binding.llTagsContainer.addView(layout)
            }

            "multiText" -> {
                val layout = inflator.inflate(R.layout.item_tag_notes, null)
                val itemBinding = ItemTagNotesBinding.bind(layout)

                tempBinding = itemBinding
                binding.llTagsContainer.addView(layout)
            }
        }

        return tempBinding!!
    }

    private fun addBinding(category: String, filedType: String, binding: ViewBinding) {
        if (bindingMap[category] == null) {
            bindingMap[category] = ArrayList()
        }

        val list = bindingMap[category]

        list?.add(binding)
    }

    private fun uploadImages() {
        viewModel.onImageConfirmed.value = viewModel.getOnImageConfirmed()
        viewModel.shootData.value?.meta = getMetaValue()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.insertImage(viewModel.shootData.value!!)
        }

        startService()
    }

    private fun getMetaValue(): String {
        val json = JSONObject()

        val bindingList = bindingMap[viewModel.categoryDetails.value?.imageType]


        bindingList?.forEachIndexed { index, viewBinding ->
            when (viewBinding) {
                is ItemTagsSpinnerBinding -> {
                    if (viewBinding.spinner.selectedItemPosition != 0)
                        json.put(
                            getTagKey(index),
                            viewBinding.spinner.selectedItem.toString()
                        )
                }

                is ItemTagNotesBinding -> {
                    if (viewBinding.tvNotes.text.toString().isNotEmpty())
                        json.put(
                            getTagKey(index),
                            viewBinding.etNotes.text.toString()
                        )
                }
            }
        }

        return json.toString()
    }

    private fun getTagKey(index: Int): String {

        val response = (viewModel.subCategoriesResponse.value as Resource.Success).value

        when (viewModel.categoryDetails.value?.imageType) {
            "Exterior" -> {
                return response.tags.exterior[index].fieldId
            }
            "Interior" -> {
                return response.tags.interior[index].fieldId
            }
            "Focus Shoot" -> {
                return response.tags.focusShoot[index].fieldId
            }
            else -> {
            }
        }

        return ""
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

    private fun setOverlay(view: View, overlay: String) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                Glide.with(requireContext())
                    .load(overlay)
                    .into(binding.ivOverlay)

//                viewModel.shootDimensions.value.let {
//                    var prw = it?.previewWidth
//                    var prh = it?.previewHeight
//
//                    var ow = it?.overlayWidth
//                    var oh = it?.overlayHeight
//
//
//                    var newW =
//                        ow!!.toFloat().div(prw!!.toFloat()).times(view.width)
//                    var newH =
//                        oh!!.toFloat().div(prh!!.toFloat()).times(view.height)
//
////                    Log.d(TAG, "onGlobalLayout: "+it?.previewWidth)
////                    Log.d(TAG, "onGlobalLayout: "+it?.previewHeight)
////                    Log.d(TAG, "onGlobalLayout: "+it?.overlayWidth)
////                    Log.d(TAG, "onGlobalLayout: "+it?.overlayHeight)
////                    Log.d(TAG, "onGlobalLayout: "+view.width)
////                    Log.d(TAG, "onGlobalLayout: "+view.height)
////                    Log.d(TAG, "onGlobalLayout: "+overlay)
////                    Log.d(TAG, "onGlobalLayout: "+newW)
////                    Log.d(TAG, "onGlobalLayout: "+newH)
//
//                    var equlizerOverlayMargin = (9.5 * resources.displayMetrics.density).toInt()
//
//                    var params = FrameLayout.LayoutParams(newW.toInt(), newH.toInt())
//                    params.gravity = Gravity.CENTER
//                    params.topMargin = equlizerOverlayMargin
//
//                    binding.ivOverlay.layoutParams = params
//
//
//
//                }
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
                else -> {
                }
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
                else -> {
                }
            }
        })
    }

    private fun selectBackground() {
        if (getString(R.string.app_name) == AppConstants.OLA_CABS)
            viewModel.show360InteriorDialog.value = true
        else
            viewModel.selectBackground.value = true
    }

    private fun tagsValid(): Boolean {
        val response = (viewModel.subCategoriesResponse.value as Resource.Success).value
        var isValidTag = true
        when (viewModel.categoryDetails.value?.imageType) {
            "Exterior" -> {
                val bindingList = bindingMap[viewModel.categoryDetails.value?.imageType]

                bindingList?.forEachIndexed { index, viewBinding ->
                    when (viewBinding) {
                        is ItemTagsSpinnerBinding -> {
                            if (response.tags.exterior[index].isRequired) {
                                if (viewBinding.spinner.selectedItemPosition == 0) {
                                    showErrorToast(response.tags.exterior.get(index).fieldName)
                                    isValidTag = false
                                    return isValidTag
                                }
                            }
                        }

                        is ItemTagNotesBinding -> {
                            if (response.tags.exterior[index].isRequired) {
                                if (viewBinding.etNotes.text.toString().isEmpty()) {
                                    notesError(viewBinding.etNotes)
                                    isValidTag = false
                                }
                            }
                        }
                    }
                }

            }

            "Interior" -> {
                val bindingList = bindingMap[viewModel.categoryDetails.value?.imageType]

                bindingList?.forEachIndexed { index, viewBinding ->
                    when (viewBinding) {
                        is ItemTagsSpinnerBinding -> {
                            if (response.tags.interior[index].isRequired) {
                                if (viewBinding.spinner.selectedItemPosition == 0) {
                                    showErrorToast(response.tags.exterior.get(index).fieldName)
                                    isValidTag = false
                                    return isValidTag
                                }
                            }
                        }

                        is ItemTagNotesBinding -> {
                            if (response.tags.interior[index].isRequired) {
                                if (viewBinding.etNotes.text.toString().isEmpty()) {
                                    notesError(viewBinding.etNotes)
                                    isValidTag = false
                                }
                            }
                        }
                    }
                }
            }

            "Focus Shoot" -> {
                val bindingList = bindingMap.get(viewModel.categoryDetails.value?.imageType)

                val s = ""

                bindingList?.forEachIndexed { index, viewBinding ->
                    when (viewBinding) {
                        is ItemTagsSpinnerBinding -> {
                            val s = ""
                            if (response.tags.focusShoot[index].isRequired) {
                                if (viewBinding.spinner.selectedItemPosition == 0) {
                                    showErrorToast(response.tags.focusShoot.get(index).fieldName)
                                    isValidTag = false
                                    return isValidTag
                                }
                            }
                        }

                        is ItemTagNotesBinding -> {
                            val s = ""
                            if (response.tags.focusShoot[index].isRequired) {
                                if (viewBinding.etNotes.text.toString().isEmpty()) {
                                    notesError(viewBinding.etNotes)
                                    isValidTag = false
                                }
                            }
                        }
                    }
                }
            }
        }


        return isValidTag
    }

    private fun notesError(editText: EditText) {
        editText.error = "Please enter notes"
        Toast.makeText(requireContext(), "Please enter notes", Toast.LENGTH_LONG).show()
    }

    private fun showErrorToast(fieldName: String) {
        val text = when (fieldName) {
            "Imperfection Location" -> "Please select imperfection location"
            "Imperfection Type" -> "Please select imperfection type"
            "Imperfection Severity" -> "Please select imperfection severity"
            else -> "notes"
        }

        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogConfirmTagsBinding.inflate(inflater, container, false)
}