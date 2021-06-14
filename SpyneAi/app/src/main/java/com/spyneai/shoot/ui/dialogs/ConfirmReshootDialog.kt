package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.databinding.DialogConfirmReshootBinding
import com.spyneai.shoot.data.ShootViewModel

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
                dismiss()

                viewModel.showInteriorDialog.value = true

            }else{
                viewModel.shootNumber.value = viewModel.shootNumber.value!! + 1
                // viewModel.uploadImageWithWorkManager(requireContext(), viewModel.shootData.value!!)
                dismiss()
            }
        }

       viewModel.overlaysResponse.observe(viewLifecycleOwner,{
           when(it){
                is Resource.Sucess -> {
                    val uri = viewModel.shootData.value?.capturedImage
                    val overlay = it.value.data[viewModel.shootNumber.value!!].display_thumbnail

                    Glide.with(requireContext())
                        .load(uri)
                        .into(binding.ivCaptured2)

                    Glide.with(requireContext())
                        .load(uri)
                        .into(binding.ivCapturedImage)


                    setOverlay(binding.ivCaptured2,overlay)
               }
               else -> {

               }
           }
       })

    }

    private fun setOverlay(view: View, overlay : String) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                viewModel.shootDimensions.value.let {
                    var prw = it?.previewWidth
                    var prh = it?.previewHeight

                    var ow = it?.overlayWidth
                    var oh = it?.overlayHeight

                    var newW =
                        ow!!.toFloat().div(prw!!.toFloat()).times(view.width)
                    var newH =
                        oh!!.toFloat().div(prh!!.toFloat()).times(view.height)


                    var params = FrameLayout.LayoutParams(newW.toInt(), newH.toInt())
                    //params.gravity = Gravity.CENTER

                    binding.ivCapturedOverlay.layoutParams = params

                    Glide.with(requireContext())
                        .load(overlay)
                        .into(binding.ivCapturedOverlay)
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