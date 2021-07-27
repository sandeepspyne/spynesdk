package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogCreateProjectAndSkuBinding
import com.spyneai.databinding.DialogShootHintBinding
import com.spyneai.shoot.data.ShootViewModel

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
            dismiss()
        }
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogShootHintBinding.inflate(inflater, container, false)
}