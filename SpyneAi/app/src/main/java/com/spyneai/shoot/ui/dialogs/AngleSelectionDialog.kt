package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogAngleSelectionBinding
import com.spyneai.shoot.data.ShootViewModel

class AngleSelectionDialog : BaseDialogFragment<ShootViewModel,DialogAngleSelectionBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        showOptions()
    }

    private fun showOptions() {
        val valuesShoots = when(getString(R.string.app_name)){
            "Cars 24" -> arrayOf("5 Angles")
            else -> arrayOf("8 Angles", "12 Angles","16 Angles","24 Angles","36 Angles")
        }

        val lastSelectedAngles = viewModel.getSelectedAngles()
        var newSelectedAngles = viewModel.getSelectedAngles()

        when(viewModel.getSelectedAngles()){
            8,5 -> binding.npShoots.minValue = 0
            12 -> binding.npShoots.minValue = 1
            16 -> binding.npShoots.minValue = 2
            24 -> binding.npShoots.minValue = 3
            36 -> binding.npShoots.minValue = 4
        }

        binding.npShoots.minValue = 0
        binding.npShoots.maxValue = valuesShoots.size - 1
        binding.npShoots.displayedValues = valuesShoots

        binding.npShoots.setOnValueChangedListener { _, _, newVal ->
           when(valuesShoots[newVal]) {
               "5 Angles" -> newSelectedAngles = 5
               "8 Angles" -> newSelectedAngles = 8
               "12 Angles" -> newSelectedAngles = 12
               "16 Angles" -> newSelectedAngles = 16
               "24 Angles" -> newSelectedAngles = 24
               "36 Angles" -> newSelectedAngles = 36
           }
        }

        binding.tvProceed.setOnClickListener {
            if (lastSelectedAngles != newSelectedAngles)
                viewModel.exterirorAngles.value = newSelectedAngles
                    dismiss()
        }
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogAngleSelectionBinding.inflate(inflater, container, false)
}