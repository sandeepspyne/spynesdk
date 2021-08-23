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

class SubcategoriesFragment : BaseFragment<ThreeSixtyViewModel,FragmentSubcategoriesBinding>(){

    lateinit var subCategoriesAdapter: NewSubCategoriesAdapter
    var pos = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       if (!viewModel.fromDrafts)
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
                viewModel.enableRecording.value = true
            }
        })
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSubcategoriesBinding.inflate(inflater, container, false)

}