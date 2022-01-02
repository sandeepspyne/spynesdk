package com.spyneai.threesixty.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.captureEvent
import com.spyneai.databinding.FragmentSubcategoriesBinding
import com.spyneai.posthog.Events
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import com.spyneai.threesixty.ui.dialogs.ThreeSixtyExteriorGifDialog
import com.spyneai.threesixty.ui.dialogs.ThreeSixtyProjectAndSkuDialog

class SubcategoriesFragment : BaseFragment<ThreeSixtyViewModel,FragmentSubcategoriesBinding>(){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       if (!viewModel.fromDrafts && viewModel.videoDetails.projectId == null)
           initShootHint()
    }

    private fun initShootHint() {
        requireContext().captureEvent(Events.SHOW_360_HINT, HashMap<String,Any?>())
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