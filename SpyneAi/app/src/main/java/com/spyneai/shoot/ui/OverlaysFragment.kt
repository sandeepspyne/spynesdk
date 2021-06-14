package com.spyneai.shoot.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
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
import java.util.*


class OverlaysFragment : BaseFragment<ShootViewModel,FragmentOverlaysBinding>(),NewSubCategoriesAdapter.BtnClickListener {


    lateinit var subCategoriesAdapter: NewSubCategoriesAdapter
    lateinit var progressAdapter: ShootProgressAdapter
    private var showDialog = true
    private lateinit var imageUri: Uri
    var pos = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initShootHint()

        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (showDialog && !it.isNullOrEmpty()){
                    showImageConfirmDialog(it.get(it.size - 1))
                }
            }catch (e : Exception){
                e.printStackTrace()
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
                intSubcategorySelection()
            }
        })

    }

    private fun initAngles() {
        viewModel.selectedAngles.value = 8

        binding.tvShoot?.setOnClickListener {
            AngleSelectionDialog().show(requireFragmentManager(), "AngleSelectionDialog")
        }

        //update progress list
        viewModel.selectedAngles.observe(viewLifecycleOwner, {
            binding.tvShoot?.text = "Angles 1/${viewModel.getSelectedAngles()}"

            initProgressFrames()
            getOverlays()
        })
    }

    private fun initProgressFrames() {
        //update this shoot number
        viewModel.shootNumber.value = 0

        progressAdapter = ShootProgressAdapter(requireContext(), viewModel.getShootProgressList())

        binding.rvProgress.apply {
            this?.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this?.adapter = progressAdapter
        }

        viewModel.shootNumber.observe(viewLifecycleOwner, {
            binding.tvShoot?.text = "Angles ${viewModel.shootNumber.value!! + 1}/${viewModel.getSelectedAngles()}"

            viewModel.overlaysResponse.observe(viewLifecycleOwner,{
                when(it){
                    is Resource.Sucess -> {
                        val name = it.value.data[viewModel.shootNumber.value!!].display_name
                        val overlay = it.value.data[viewModel.shootNumber.value!!].display_thumbnail


                        binding.tvAngleName?.text = name

                        Glide.with(requireContext())
                            .load(overlay)
                            .into(binding.imgOverlay!!)

                    }
                    else -> {

                    }
                }
            })

            progressAdapter.updateList(viewModel.shootNumber.value!!)
        })


    }

    private fun intSubcategorySelection() {
        subCategoriesAdapter = NewSubCategoriesAdapter(
            requireContext(),
            null,
            pos,
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

                    binding.clSubcatSelectionOverlay?.visibility = View.VISIBLE
                }
                is Resource.Loading -> {

                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
            }
        })
    }

    private fun showViews() {
        binding.apply {
            tvSkuName?.visibility = View.VISIBLE
            tvAngleName?.visibility = View.VISIBLE
            llProgress?.visibility = View.VISIBLE
            imgOverlay?.visibility = View.VISIBLE
        }
    }

    private fun getOverlays() {
        viewModel.subCategory.value?.let {
            viewModel.getOverlays(
                Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
                requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
                it.prod_sub_cat_id!!,
                viewModel.selectedAngles.value.toString()
            )

            viewModel.overlaysResponse.observe(viewLifecycleOwner,{ it ->
                when(it){
                    is Resource.Sucess -> {

                        binding.clSubcatSelectionOverlay?.visibility = View.GONE
                        showViews()

//                        binding.tvAngleName?.text = it.value.data[0].display_name
//
//                        Glide.with(requireContext())
//                            .load(it.value.data[0].display_thumbnail)
//                            .into(binding.imgOverlay!!)
                    }

                    is Resource.Loading -> {

                    }

                    is Resource.Failure -> {
                        handleApiError(it)
                    }
                }
            })
        }
    }

    override fun onBtnClick(position : Int,data: NewSubCatResponse.Data) {
        if (pos != position || !subCategoriesAdapter.selectionEnabled){

            viewModel.subCategory.value = data
            pos = position

            subCategoriesAdapter.selectionEnabled = true
            subCategoriesAdapter.notifyDataSetChanged()

            initAngles()
            initProgressFrames()
            getOverlays()
        }

    }

    fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
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