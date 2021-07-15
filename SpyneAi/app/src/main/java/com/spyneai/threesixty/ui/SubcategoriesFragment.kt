package com.spyneai.threesixty.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentRecordVideoBinding
import com.spyneai.databinding.FragmentSubcategoriesBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.NewSubCategoriesAdapter
import com.spyneai.shoot.ui.dialogs.CreateProjectAndSkuDialog
import com.spyneai.shoot.ui.dialogs.ShootHintDialog
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import com.spyneai.threesixty.ui.dialogs.ThreeSixtyExteriorGifDialog
import com.spyneai.threesixty.ui.dialogs.ThreeSixtyProjectAndSkuDialog
import java.util.ArrayList

class SubcategoriesFragment : BaseFragment<ThreeSixtyViewModel,FragmentSubcategoriesBinding>(),NewSubCategoriesAdapter.BtnClickListener {

    lateinit var subCategoriesAdapter: NewSubCategoriesAdapter
    var pos = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initShootHint()
    }

    private fun initShootHint() {
        requireContext().captureEvent(Events.SHOW_360_HINT, Properties())
        ThreeSixtyExteriorGifDialog().show(
            requireActivity().supportFragmentManager,
            "ThreeSixtyExteriorGifDialog")

        viewModel.isDemoClicked.observe(viewLifecycleOwner,{
            if (it) initProjectDialog()
        })
    }

    private fun initProjectDialog(){
        ThreeSixtyProjectAndSkuDialog().show(
            requireActivity().supportFragmentManager,
            "ThreeSixtyProjectAndSkuDialog"
        )

        viewModel.isProjectCreated.observe(viewLifecycleOwner,{
            if (it) {
                intSubcategorySelection()
            }
        })
    }

    private fun intSubcategorySelection() {
        subCategoriesAdapter = NewSubCategoriesAdapter(
            requireContext(),
            null,
            pos,
            this
        )

        binding.rvSubcategories.apply {
            this?.layoutManager = LinearLayoutManager(requireContext())
            this?.adapter = subCategoriesAdapter
        }

        viewModel.getSubCategories(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )

        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.GET_360_SUBCATEGORIES,
                        Properties())

                    Utilities.hideProgressDialog()
                    subCategoriesAdapter.subCategoriesList =
                        it.value.data as ArrayList<NewSubCatResponse.Data>
                    subCategoriesAdapter.notifyDataSetChanged()

                    binding.clSubcatSelectionOverlay?.visibility = View.VISIBLE
                }
                is Resource.Loading ->  Utilities.showProgressDialog(requireContext())

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(Events.GET_360_SUBCATRGORIES_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })
    }

    override fun onBtnClick(position: Int, data: NewSubCatResponse.Data) {
        if (pos != position || !subCategoriesAdapter.selectionEnabled){

            viewModel.subCategory.value = data
            pos = position

            subCategoriesAdapter.selectionEnabled = true
            subCategoriesAdapter.notifyDataSetChanged()

            binding.clSubcatSelectionOverlay.visibility = View.GONE
            viewModel.enableRecording.value = true
        }
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSubcategoriesBinding.inflate(inflater, container, false)

}