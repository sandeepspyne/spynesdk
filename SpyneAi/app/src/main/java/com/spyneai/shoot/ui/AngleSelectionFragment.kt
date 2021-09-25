package com.spyneai.shoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentAngleSelectionBinding
import com.spyneai.databinding.FragmentCreateProjectBinding
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.ui.dialogs.AngleSelectionDialog

class AngleSelectionFragment : BaseFragment<ShootViewModel, FragmentAngleSelectionBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AngleSelectionDialog().show(requireActivity().supportFragmentManager, "AngleSelectionDialog")
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAngleSelectionBinding.inflate(inflater, container, false)
}