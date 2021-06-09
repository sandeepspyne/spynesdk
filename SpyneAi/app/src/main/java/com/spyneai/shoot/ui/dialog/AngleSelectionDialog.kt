package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogAngleSelectionBinding
import com.spyneai.shoot.ui.ShootViewModel

class AngleSelectionDialog : BaseDialogFragment<ShootViewModel,DialogAngleSelectionBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        showOptions()
    }

    private fun showOptions() {
        val valuesShoots = arrayOf("4 Angles", "8 Angles", "12 Angles")

        val lastSelectedAngles = viewModel.getSelectedAngles().toString()
        var newSelectedAngles =viewModel.getSelectedAngles().toString()

        when(viewModel.getSelectedAngles()){
            4 -> binding.npShoots.minValue = 0
            8 -> binding.npShoots.minValue = 1
            12 -> binding.npShoots.minValue = 2
        }

        binding.npShoots.minValue = 0
        binding.npShoots.maxValue = valuesShoots.size - 1
        binding.npShoots.displayedValues = valuesShoots

        binding.npShoots.setOnValueChangedListener { _, _, newVal ->
            newSelectedAngles = valuesShoots[newVal]

            when {
                valuesShoots[newVal] == "4 Angles" -> {
                    viewModel.selectedAngles.value = 4
                    //setProgressFrame(4)
                }
                valuesShoots[newVal] == "8 Angles" -> {
                    viewModel.selectedAngles.value = 8
                    //setProgressFrame(8)
                }
                valuesShoots[newVal] == "12 Angles" -> {
                    viewModel.selectedAngles.value = 12
                    // setProgressFrame(12)
                }
                valuesShoots[newVal] == "24 Angles" -> {
                    viewModel.selectedAngles.value = 24
                    //setProgressFrame(24)
                }
            }
        }

        binding.tvProceed.setOnClickListener {
            if (lastSelectedAngles != newSelectedAngles)
                //isSubcatgoryConfirmed = false
                    dismiss()
        }
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogAngleSelectionBinding.inflate(inflater, container, false)
}