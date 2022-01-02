package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.databinding.DialogInteriorHintBinding
import com.spyneai.needs.AppConstants
import com.spyneai.setLocale
import com.spyneai.shoot.data.ShootViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class InteriorHintDialog : BaseDialogFragment<ShootViewModel, DialogInteriorHintBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireContext().setLocale()

        dialog?.setCancelable(false)
        refreshText()

        changePhotos()

        binding.tvSkip.setOnClickListener {
            viewModel.showMiscDialog.value = true
            viewModel.iniProgressFrame.value = false
            viewModel.startInteriorShots.value = true
          checkMiscShootStatus()
            dismiss()
        }

        binding.tvShootNowInterior.setOnClickListener {
            viewModel.iniProgressFrame.value = false
            viewModel.startInteriorShots.value = true
            dismiss()
        }
    }

    private fun changePhotos() {
        when(getString(R.string.app_name)){
            AppConstants.AUTO_FOTO -> {
                val subCategoriesResponse = (viewModel.subCategoriesResponse.value as Resource.Success).value

                if (!subCategoriesResponse.interior.isNullOrEmpty() && subCategoriesResponse.interior.size >= 4){
                    setImage(binding.ivOne,subCategoriesResponse.interior[0].display_thumbnail)
                    setImage(binding.ivTwo,subCategoriesResponse.interior[1].display_thumbnail)
                    setImage(binding.ivThree,subCategoriesResponse.interior[2].display_thumbnail)
                    setImage(binding.ivFour,subCategoriesResponse.interior[3].display_thumbnail)
                }
            }else -> {
                setImageWithDrawable(binding.ivOne,R.mipmap.interior_demo1)
                setImageWithDrawable(binding.ivTwo,R.mipmap.idemo2)
                setImageWithDrawable(binding.ivThree,R.mipmap.idemo3)
                setImageWithDrawable(binding.ivFour,R.mipmap.idemo4)
            }
        }
    }

    private fun setImageWithDrawable(ivOne: ImageView, interior: Int) {
        Glide.with(requireContext())
            .load(interior)
            .into(ivOne)
    }

    private fun setImage(ivOne: ImageView, interior: String) {
        val s = ""
        Glide.with(requireContext())
            .load(interior)
            .into(ivOne)
    }
    fun refreshText(){
        requireContext().setLocale()
        binding.tvSkuNameDialog.text = getString(R.string.we_recommend)
        binding.tvSkip.text = getString(R.string.skip)
        binding.tvShootNowInterior.text = getString(R.string.shoot_now)
    }

    private fun checkMiscShootStatus() {
        val response = (viewModel.subCategoriesResponse.value as Resource.Success).value

        GlobalScope.launch(Dispatchers.IO) {
            val MiscList = viewModel.getMiscList()
            if (!MiscList.isNullOrEmpty()){
                response.miscellaneous = MiscList
                GlobalScope.launch(Dispatchers.Main) {
                    viewModel.showMiscDialog.value = true
                }
                return@launch
            }

            GlobalScope.launch(Dispatchers.Main) {
                viewModel.selectBackground(getString(R.string.app_name))
            }

        }
    }



    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogInteriorHintBinding.inflate(inflater, container, false)
}