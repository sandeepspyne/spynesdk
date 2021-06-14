package com.spyneai.shoot.ui.dialogs

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.hbisoft.pickit.PickiTCallbacks
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.databinding.DialogConfirmReshootBinding
import com.spyneai.databinding.DialogShootHintBinding
import com.spyneai.shoot.data.ShootViewModel
import java.io.File

class ConfirmReshootDialog : BaseDialogFragment<ShootViewModel, DialogConfirmReshootBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btReshootImage.setOnClickListener{
            dismiss()
        }


        binding.btConfirmImage.setOnClickListener {

            if (viewModel.shootNumber.value  == viewModel.selectedAngles.value?.minus(1)){
                Toast.makeText(requireContext(),"DONE!!!!!",Toast.LENGTH_LONG).show()
                // viewModel.uploadImageWithWorkManager(requireContext(), viewModel.shootData.value!!)
            }else{
                viewModel.shootNumber.value = viewModel.shootNumber.value!! + 1
                // viewModel.uploadImageWithWorkManager(requireContext(), viewModel.shootData.value!!)
            }

            dismiss()
        }

       viewModel.overlaysResponse.observe(viewLifecycleOwner,{
           when(it){
                is Resource.Sucess -> {
                    val uri = viewModel.shootData.value?.capturedImage
                    val overlay = it.value.data[viewModel.shootNumber.value!!]

                    Glide.with(requireContext())
                        .load(uri)
                        .into(binding.ivCapturedImage)

                    Glide.with(requireContext())
                        .load(uri)
                        .into(binding.ivCaptured2)

                    Glide.with(requireContext())
                        .load(overlay)
                        .into(binding.ivCapturedOverlay)

               }
               else -> {

               }
           }
       })

    }



    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogConfirmReshootBinding.inflate(inflater, container, false)
}