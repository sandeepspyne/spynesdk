package com.spyneai.shoot.ui.dialogs

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.hbisoft.pickit.PickiTCallbacks
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.databinding.DialogConfirmReshootBinding
import com.spyneai.databinding.DialogShootHintBinding
import com.spyneai.shoot.data.ShootViewModel
import java.io.File

class ConfirmReshootDialog: BaseDialogFragment<ShootViewModel, DialogConfirmReshootBinding>(),PickiTCallbacks {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


       viewModel.overlaysResponse.observe(viewLifecycleOwner,{
           when(it){
                is Resource.Sucess -> {
                    val uri = viewModel.shootList.value?.get(viewModel.shootNumber.value!!)
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

    override fun PickiTonUriReturned() {

    }

    override fun PickiTonStartListener() {

    }

    override fun PickiTonProgressUpdate(progress: Int) {

    }

    override fun PickiTonCompleteListener(
        path: String?,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        Reason: String?
    ) {

    }
}