package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.setLocale
import com.spyneai.base.BaseDialogFragment
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.databinding.DialogAngleSelectionBinding
import com.spyneai.needs.AppConstants
import com.spyneai.setLocale
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.utils.shoot

class AngleSelectionDialog : BaseDialogFragment<ShootViewModel,DialogAngleSelectionBinding>() {




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshTexts()

        showOptions()
    }

    private fun showOptions() {
        val angles: String = getString(R.string.angles)
        val valuesShoots = when(getString(R.string.app_name)){
            AppConstants.CARS24_INDIA,AppConstants.CARS24 -> arrayOf("5 "+angles)
            AppConstants.SELL_ANY_CAR -> arrayOf("4 "+angles, "36 "+angles)
            else -> arrayOf("8 "+angles, "12 "+angles,"16 "+angles,"24 "+angles,"36 "+angles)
        }

        val lastSelectedAngles = viewModel.getSelectedAngles()
        var newSelectedAngles = viewModel.getSelectedAngles()


        when(getString(R.string.app_name)){
            AppConstants.SELL_ANY_CAR->{
                when(viewModel.getSelectedAngles()){
                    4 -> binding.npShoots.minValue = 0
                    36 -> binding.npShoots.minValue = 1
                }
            } else -> {
            when(viewModel.getSelectedAngles()){
                8,5 -> binding.npShoots.minValue = 0
                12 -> binding.npShoots.minValue = 1
                16 -> binding.npShoots.minValue = 2
                24 -> binding.npShoots.minValue = 3
                36 -> binding.npShoots.minValue = 4
            }
        }
        }

        binding.npShoots.minValue = 0
        binding.npShoots.maxValue = valuesShoots.size - 1
        binding.npShoots.displayedValues = valuesShoots

        binding.npShoots.setOnValueChangedListener { _, _, newVal ->
           when(valuesShoots[newVal]) {
               "4 "+angles -> newSelectedAngles = 4
               "5 "+angles -> newSelectedAngles = 5
               "8 "+angles -> newSelectedAngles = 8
               "12 "+angles -> newSelectedAngles = 12
               "16 "+angles -> newSelectedAngles = 16
               "24 "+angles -> newSelectedAngles = 24
               "36 "+angles -> newSelectedAngles = 36
           }
        }

        binding.tvProceed.setOnClickListener {
            if (lastSelectedAngles != newSelectedAngles)
                //isSubcatgoryConfirmed = false
                    shoot("angle selected- "+newSelectedAngles)
                viewModel.exterirorAngles.value = newSelectedAngles
                    dismiss()
        }
    }
    private fun refreshTexts() {
        requireContext().setLocale()
                binding.tvChooseShot.text = getString(R.string.choose_shoots)
                binding.tvMoreAngle.text = getString(R.string.more_angles)
            binding.tvProceed.text = getString(R.string.proceed)



    }

    override fun onStop() {
        super.onStop()
        shoot("onStop called(angleSlectionDialog-> dismissAllowingStateLoss)")
        dismissAllowingStateLoss()
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogAngleSelectionBinding.inflate(inflater, container, false)
}