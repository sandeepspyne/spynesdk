package com.spyneai.shoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.balsikandar.kotlindslsamples.dialogfragment.DialogDSLBuilder.Companion.dialog
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentOverlaysBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.adapter.ShootProgressAdapter
import com.spyneai.shoot.adapters.NewSubCategoriesAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.dialogs.AngleSelectionDialog
import com.spyneai.shoot.ui.dialogs.ConfirmReshootDialog
import com.spyneai.shoot.ui.dialogs.CreateProjectAndSkuDialog
import com.spyneai.shoot.ui.dialogs.ShootHintDialog
import kotlinx.android.synthetic.main.dialog_confirm_reshoot.view.*
import java.util.*

class OverlaysFragment : BaseFragment<ShootViewModel,FragmentOverlaysBinding>(),NewSubCategoriesAdapter.BtnClickListener {


    lateinit var subCategoriesAdapter: NewSubCategoriesAdapter
    lateinit var progressAdapter: ShootProgressAdapter
    private var showDialog = true
    var prodSubcategoryId = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initShootHint()

        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (showDialog)
                    showImageConfirmDialog(it.get(it.size - 1))
            }catch (e : Exception){

            }
        })
    }

    private fun initShootHint() {
        ShootHintDialog().show(requireFragmentManager(), "ShootHintDialog")

        viewModel.showVin.observe(viewLifecycleOwner,{
            if (it) initProjectDialog()
        })
    }

    private fun initProjectDialog(){
        CreateProjectAndSkuDialog().show(requireFragmentManager(), "CreateProjectAndSkuDialog")

        viewModel.isProjectCreated.observe(viewLifecycleOwner,{
            if (it) {
                initAngles()
                initProgressFrames()
                intSubcategorySelection()
            }
        })

    }

    private fun initAngles() {
        viewModel.selectedAngles.value = 8

        binding.tvShoot?.setOnClickListener {
            AngleSelectionDialog().show(requireFragmentManager(), "AngleSelectionDialog")
        }

        viewModel.shootNumber.observe(viewLifecycleOwner, {
            binding.tvShoot?.text = "Angles $it/${viewModel.getSelectedAngles()}"
        })
    }

    private fun initProgressFrames() {
        //update this shoot number
        viewModel.shootNumber.value = 1

        progressAdapter = ShootProgressAdapter(requireContext(), viewModel.getShootProgressList())

        binding.rvProgress.apply {
            this?.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this?.adapter = progressAdapter
        }

        //update progress list
        viewModel.selectedAngles.observe(viewLifecycleOwner, {
            binding.tvShoot?.text = "Angles 1/${viewModel.getSelectedAngles()}"
            progressAdapter.updateList(viewModel.getShootProgressList())

            //get overlays when value changed
            if(prodSubcategoryId != "")
                getOverlays(prodSubcategoryId)
        })
    }


    private fun intSubcategorySelection() {

        subCategoriesAdapter = NewSubCategoriesAdapter(
            requireContext(),
            null,
            0,
            this)

        binding.rvSubcategories.apply {
            this?.layoutManager = LinearLayoutManager(requireContext())
            this?.adapter = subCategoriesAdapter
        }

        viewModel.getSubCategories(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )

        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Sucess -> {
                    subCategoriesAdapter.subCategoriesList =
                        it.value.data as ArrayList<NewSubCatResponse.Data>
                    subCategoriesAdapter.notifyDataSetChanged()
                }
                is Resource.Loading -> {

                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
            }
        })
    }

    private fun getOverlays(prodSubcategoryId : String) {
        viewModel.getOverlays(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
            prodSubcategoryId,
            viewModel.selectedAngles.value.toString()
        )

        viewModel.overlaysResponse.observe(viewLifecycleOwner,{
            when(it){
                is Resource.Sucess -> {
                    Glide.with(requireContext())
                        .load(it.value.data[0].display_thumbnail)
                        .into(binding.imgOverlay!!)
                }

                is Resource.Loading -> {

                }

                is Resource.Failure -> {
                    handleApiError(it)
                }
            }
        })
    }

    override fun onBtnClick(data: NewSubCatResponse.Data) {
        prodSubcategoryId = data.prod_sub_cat_id
        getOverlays(prodSubcategoryId)
    }

    fun showImageConfirmDialog(shootData: ShootData) {

        ConfirmReshootDialog().show(requireFragmentManager(), "ConfirmReshootDialog")

//        dialog {
//            layoutId = R.layout.dialog_confirm_reshoot
//            setCustomView = { it: View, dialog: DialogFragment ->
//
//                it.btReshootImage.setOnClickListener {
//                    showDialog = false
//                    viewModel.shootList.value?.remove(shootData)
//                    dialog.dismiss()
//                    showDialog = true
//                }
//
//                it.btConfirmImage.setOnClickListener {
//                    viewModel.uploadImageWithWorkManager(requireContext(), shootData)
//                    dialog.dismiss()
//                }
//
//            }
//        }
    }



    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOverlaysBinding.inflate(inflater, container, false)




}