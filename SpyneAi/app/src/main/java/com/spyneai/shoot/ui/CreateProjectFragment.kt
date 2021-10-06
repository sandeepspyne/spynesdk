package com.spyneai.shoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.posthog.android.Properties
import com.spyneai.base.BaseFragment
import com.spyneai.captureEvent
import com.spyneai.databinding.FragmentCreateProjectBinding
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.ui.dialogs.CreateProjectAndSkuDialog
import com.spyneai.shoot.ui.dialogs.ShootHintDialog

class CreateProjectFragment : BaseFragment<ShootViewModel, FragmentCreateProjectBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.showHint.observe(viewLifecycleOwner,{
                initShootHint()
        })

        viewModel.showVin.observe(viewLifecycleOwner,{
            initProjectDialog()
        })
    }

    private fun initShootHint() {
        requireContext().captureEvent(Events.SHOW_HINT, Properties())
        ShootHintDialog().show(requireActivity().supportFragmentManager, "ShootHintDialog")
    }

    private fun initProjectDialog() {
        CreateProjectAndSkuDialog().show(
            requireActivity().supportFragmentManager,
            "CreateProjectAndSkuDialog"
        )
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCreateProjectBinding.inflate(inflater, container, false)
}