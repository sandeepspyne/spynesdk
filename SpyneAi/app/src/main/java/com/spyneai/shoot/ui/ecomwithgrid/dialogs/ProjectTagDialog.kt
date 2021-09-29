package com.spyneai.shoot.ui.ecomwithgrid.dialogs

import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import androidx.core.widget.addTextChangedListener
import androidx.viewbinding.ViewBinding
import com.google.android.material.chip.Chip
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.data.model.LayoutHolder
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.ItemProjectChipedittextBinding
import com.spyneai.databinding.ItemProjectEdittextBinding
import com.spyneai.databinding.ProjectTagDialogBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.Project
import com.spyneai.shoot.data.model.Sku
import com.spyneai.shoot.utils.log
import kotlinx.android.synthetic.main.item_project_chipedittext.view.*
import org.json.JSONArray
import org.json.JSONObject

class ProjectTagDialog : BaseDialogFragment<ShootViewModel, ProjectTagDialogBinding>() {

    private val bindingList = ArrayList<ViewBinding>()
    private lateinit var inflator: LayoutInflater
    private val data = JSONObject()
    private var shortAnimationDuration: Int = 0

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

        binding.ivClose.setOnClickListener {
            requireActivity().finish()
        }

        binding.llContainer.visibility = View.GONE
        binding.btnProceed.visibility = View.GONE

        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        binding.etProjectName.setText(viewModel.dafault_project.value)
        binding.etSkuName.setText(viewModel.dafault_sku.value)


        setTagsData()

        binding.btnProceed.setOnClickListener {
            when {
                binding.etProjectName.text.toString().isEmpty() -> {
                    binding.etProjectName.error =
                        "Please enter project name"
                }
                binding.etProjectName.text.toString()
                    .contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex()) -> {
                    binding.etProjectName.error = "Special characters not allowed"
                }
                binding.etSkuName.text.toString().isEmpty() -> {
                    binding.etSkuName.error = "Please enter product name"
                }
                binding.etSkuName.text.toString()
                    .contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex()) -> {
                    binding.etSkuName.error = "Special characters not allowed"
                }
                else -> {
                    if (isValid()) {
                        createProject()
                    }

                }
            }
        }

        if (!viewModel.fromDrafts) {
            observeCreateProject()
            observeCreateSku()
        }
    }

    private fun setTagsData() {
        val data = LayoutHolder.data
        if (data.isNullOrEmpty()) {
            return
        }

        val layout = data!![LayoutHolder.categoryPosition].dynamic_layout.project_dialog

        layout.forEach {
            when (it.field_name) {
                "Restaurant ID", "Restaurant Name" -> {
                    val layout = inflator.inflate(R.layout.item_project_edittext, null)
                    val itemBinding = ItemProjectEdittextBinding.bind(layout)
//                    itemBinding.et.hint = it.hint
                    itemBinding.llProjectName.hint = it.hint
                    val dip = 10f
                    val r: Resources = resources
                    val px = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dip,
                        r.displayMetrics
                    )
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.topMargin = px.toInt()



                    layout.layoutParams = params

                    binding.llContainer.addView(layout)
                    bindingList.add(itemBinding)
                }
                "Child Restaurant ID" -> {
                    val layout = inflator.inflate(R.layout.item_project_chipedittext, null)
                    val itemBinding = ItemProjectChipedittextBinding.bind(layout)
//                    itemBinding.et.hint = it.hint
                    itemBinding.llProjectName.hint = it.hint

                    itemBinding.et.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            if (s != null && s.isEmpty()) {
                                return
                            }
                            if (s?.last() == ',' || s?.last() == ' ') {
                                val childRID = s.toString().substringBefore(",")
                                addChip(itemBinding, childRID)
                                itemBinding.et.text!!.clear()

//                            mainTagAutoCompleteTextView.text = null
                                // mainTagAutoCompleteTextView.removeTextChangedListener(this)
                            }
                        }
                    })

                    val dip = 10f
                    val r: Resources = resources
                    val px = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dip,
                        r.displayMetrics
                    )
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.topMargin = px.toInt()



                    layout.layoutParams = params

                    binding.llContainer.addView(layout)
                    bindingList.add(itemBinding)
                }
            }
        }
    }

    private fun addChip(binding: ItemProjectChipedittextBinding, text: String) {
        binding.scrollView.visibility = View.VISIBLE
        val chip = Chip(requireContext())
        chip.text = text
        chip.isChipIconVisible = false
        chip.isCloseIconVisible = true
        chip.isClickable = true
        if (chip.text.length > 1)
            binding.chipGroup.addView(chip as View)
        chip.setOnCloseIconClickListener {
            binding.chipGroup.removeView(chip as View)
            if (binding.chipGroup.childCount == 0)
                binding.scrollView.visibility = View.GONE
        }
    }

    private fun isValid(): Boolean {
        var isValid = true

        try {
            if (LayoutHolder.data!![LayoutHolder.categoryPosition].dynamic_layout.project_dialog.isNullOrEmpty())
                return isValid
        } catch (e: Exception) {
            return isValid
        }


        val layout = LayoutHolder.data!![0].dynamic_layout.project_dialog

        bindingList.forEachIndexed { index, it ->
            when (it) {
                is ItemProjectEdittextBinding -> {
                    if (layout[index].is_required) {
                        if (it.et.text.toString().isEmpty()) {
                            requiredError(it.et, layout[index].field_name)
                            return !isValid
                        } else {
                            if (!layout[index].all_caps) {
                                var text = it.et.text.toString()
                                text = text.lowercase()
                                data.put(layout[index].field_id, text)
                            } else {
                                data.put(layout[index].field_id, it.et.text.toString())
                            }

                        }
                    } else {
                        data.put(layout[index].field_id, it.et.text.toString())
                    }
                }
                is ItemProjectChipedittextBinding -> {
                    if (layout[index].is_required) {
                        if (it.et.text.toString().isEmpty()) {
                            requiredError(it.et, layout[index].field_name)
                            return !isValid
                        } else if (it.chipGroup.childCount == 0) {
                            if (!layout[index].all_caps) {
                                var text = it.et.text.toString()
                                text = text.lowercase()
                                data.put(layout[index].field_id, text)
                            } else {
                                data.put(layout[index].field_id, it.et.text.toString())
                            }
                        } else {

                            var array = JSONArray()

                            it.chipGroup.forEachIndexed { index, view ->
                                val chip = it.chipGroup.getChildAt(index) as Chip
                                array.put(chip.text)
                            }
                            data.put(layout[index].field_id, array)
                        }
                    } else if (it.chipGroup.childCount == 0) {
                        if (!layout[index].all_caps) {
                            var text = it.et.text.toString()
                            text = text.lowercase()
                            data.put(layout[index].field_id, text)
                        } else {
                            data.put(layout[index].field_id, it.et.text.toString())
                        }
                    } else {
                        var array = JSONArray()

                        it.chipGroup.forEachIndexed { index, view ->
                            val chip = it.chipGroup.getChildAt(index) as Chip
                            array.put(chip.text)
                        }
                        data.put(layout[index].field_id, array)
                    }
                }
                else -> {

                }
            }
        }

        return isValid
    }

    private fun createProject() {
        Utilities.showProgressDialog(requireContext())

        viewModel.createProject(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            removeWhiteSpace(binding.etProjectName.text.toString()),
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
            data
        )
    }

    private fun observeCreateProject() {
        viewModel.createProjectRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.CREATE_PROJECT,
                        Properties().putValue(
                            "project_name",
                            removeWhiteSpace(binding.etProjectName.text.toString())
                        )
                    )

                    //save project to local db
                    val project = Project()
                    project.projectName = removeWhiteSpace(binding.etProjectName.text.toString())
                    project.createdOn = System.currentTimeMillis()
                    project.categoryId = viewModel.categoryDetails.value?.categoryId
                    project.categoryName = viewModel.categoryDetails.value?.categoryName
                    project.projectId = it.value.project_id
                    viewModel.insertProject(project)

                    //notify project created
                    viewModel.isProjectCreated.value = true
                    val sku = Sku()
                    log("project id created")
                    log("project id: " + it.value.project_id)
                    sku.projectId = it.value.project_id
                    viewModel.projectId.value = it.value.project_id
                    Utilities.savePrefrence(
                        requireContext(),
                        AppConstants.PROJECT_ID,
                        it.value.project_id
                    )
                    sku.skuName = removeWhiteSpace(binding.etSkuName.text.toString())
                    viewModel.sku.value = sku

                    log("create sku started")
                    createSku(
                        it.value.project_id,
                        removeWhiteSpace(binding.etSkuName.text.toString()),
                        false
                    )
                }

                is Resource.Failure -> {
                    log("create project id failed")
                    requireContext().captureFailureEvent(
                        Events.CREATE_PROJECT_FAILED, Properties(),
                        it.errorMessage!!
                    )

                    Utilities.hideProgressDialog()
                    handleApiError(it) { createProject() }
                }
            }
        })
    }

    private fun createSku(projectId: String, skuName: String, showDialog: Boolean) {
        if (showDialog)
            Utilities.showProgressDialog(requireContext())

        viewModel.createSku(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            projectId,
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
            "",
            skuName,
            0
        )
    }

    private fun observeCreateSku() {
        viewModel.createSkuRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    requireContext().captureEvent(
                        Events.CREATE_SKU,
                        Properties().putValue("sku_name", viewModel.sku.value?.skuName.toString())
                            .putValue("project_id", viewModel.sku.value?.projectId)
                            .putValue("prod_sub_cat_id", "")
                    )

                    //notify project created
                    val sku = viewModel.sku.value
                    sku?.skuId = it.value.sku_id
                    sku?.projectId = viewModel.sku.value?.projectId
                    sku?.createdOn = System.currentTimeMillis()
                    sku?.totalImages = viewModel.exterirorAngles.value
                    sku?.categoryName = viewModel.categoryDetails.value?.categoryName
                    sku?.categoryId = viewModel.categoryDetails.value?.categoryId
                    sku?.subcategoryName = viewModel.subCategory.value?.sub_cat_name
                    sku?.subcategoryId = viewModel.subCategory.value?.prod_sub_cat_id
                    sku?.exteriorAngles = viewModel.exterirorAngles.value

                    log("sku id created")
                    log("sku id: " + it.value.sku_id)
                    sku?.skuName = removeWhiteSpace(binding.etSkuName.text.toString())
                    viewModel.sku.value = sku
                    viewModel.isSkuCreated.value = true
                    //viewModel.isSubCategoryConfirmed.value = true
                    viewModel.showLeveler.value = true

                    //add sku to local database
                    viewModel.insertSku(sku!!)

                    dismiss()
                }


                is Resource.Failure -> {
                    log("create sku id failed")
                    Utilities.hideProgressDialog()
                    requireContext().captureFailureEvent(
                        Events.CREATE_SKU_FAILED, Properties(),
                        it.errorMessage!!
                    )

                    handleApiError(it) {
                        createSku(
                            viewModel.sku.value?.projectId!!,
                            removeWhiteSpace(binding.etSkuName.text.toString()),
                            true
                        )
                    }
                }
            }
        })
    }


    private fun requiredError(editText: EditText, fieldName: String) {
        editText.error = "please enter " + fieldName
    }

    private fun removeWhiteSpace(toString: String) = toString.replace("\\s".toRegex(), "")


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
    ) = ProjectTagDialogBinding.inflate(inflater, container, false)
}