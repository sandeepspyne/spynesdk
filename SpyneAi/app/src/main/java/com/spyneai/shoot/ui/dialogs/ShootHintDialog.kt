package com.spyneai.shoot.ui.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogShootHintBinding
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.utils.shoot

class ShootHintDialog : BaseDialogFragment<ShootViewModel, DialogShootHintBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        when(viewModel.categoryDetails.value?.categoryName){
            "Bikes" ->  Glide.with(this).asGif().load(R.raw.bikes_intro).into(binding.ivBeforeShootGif)
            else ->   Glide.with(this).asGif().load(R.raw.before_shoot).into(binding.ivBeforeShootGif)
        }

        binding.btContinue.setOnClickListener {
            viewModel.showVin.value = true
            viewModel.isHintShowen.value = true
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissAllowingStateLoss()
    }

    override fun onDestroy() {
        super.onDestroy()
        shoot("onDestroy called(shootHintDialog)")
        dismissAllowingStateLoss()
        dismiss()
    }

    override fun onStop() {
        super.onStop()
        shoot("onStop called(shootHintDialog)")
//        dismissAllowingStateLoss()
    }


    override fun getViewModel() = ShootViewModel::class.java


    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogShootHintBinding.inflate(inflater, container, false)
}