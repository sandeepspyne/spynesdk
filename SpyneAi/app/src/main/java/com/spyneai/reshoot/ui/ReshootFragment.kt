package com.spyneai.reshoot.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.databinding.FragmentOverlaysV2Binding
import com.spyneai.databinding.FragmentReshootBinding
import com.spyneai.needs.AppConstants
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.reshoot.ReshootAdapter
import com.spyneai.reshoot.data.SelectedImagesHelper
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.dialogs.ConfirmReshootDialog
import com.spyneai.shoot.ui.dialogs.ConfirmTagsDialog
import com.spyneai.shoot.ui.dialogs.ReclickDialog
import com.spyneai.shoot.utils.shoot

class ReshootFragment : BaseFragment<ShootViewModel,FragmentReshootBinding>(), OnItemClickListener {

    var reshootAdapter : ReshootAdapter? = null
    val TAG = "ReshootFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //set recycler view
        reshootAdapter = ReshootAdapter(SelectedImagesHelper.selectedImages,this)

        binding.rvImages.apply {
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter = reshootAdapter
        }

        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (viewModel.showConfirmReshootDialog.value == true && !it.isNullOrEmpty()) {
                    shoot("confirm reshoot dialog called")
                    shoot("shootList sine(no. of images)- " + it.size)
                    val element = viewModel.getCurrentShoot()
                    showImageConfirmDialog(element!!)
                }
            } catch (e: Exception) {
                Log.d(TAG, "onViewCreated: "+e.localizedMessage)
                e.printStackTrace()
            }
        })

        viewModel.onImageConfirmed.observe(viewLifecycleOwner,{
            if (viewModel.shootList.value != null){
                val list = reshootAdapter?.listItems as List<ImagesOfSkuRes.Data>

                val position = viewModel.sequence

                list[position].isSelected = false
                list[position].imageClicked = true
                list[position].imagePath = viewModel.getCurrentShoot()!!.capturedImage
                reshootAdapter?.notifyItemChanged(position)

                Log.d(TAG, "onViewCreated: "+position)
                Log.d(TAG, "onViewCreated: "+list[position].imagePath)

                if (position != list.size.minus(1)){
                    list[position.plus(1)].isSelected = true
                    viewModel.sequence = position.plus(1)

                    reshootAdapter?.notifyItemChanged(position.plus(1))
                    binding.rvImages.scrollToPosition(position)

                }else {
                    val element = list.firstOrNull {
                        !it.isSelected
                    }

                    if (element != null){
                        element?.isSelected = true
                        //viewModel.sequence = element?.sequenceNumber!!
                        reshootAdapter?.notifyItemChanged(viewModel.sequence)
                        binding.rvImages.scrollToPosition(viewModel.sequence)

                        Log.d(TAG, "onItemClick: "+viewModel.sequence)
                    }
                }


            }
        })

        viewModel.isCameraButtonClickable = true
    }

    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        when(getString(R.string.app_name)){
            AppConstants.OLA_CABS -> {
                ConfirmTagsDialog().show(
                    requireActivity().supportFragmentManager,
                    "ConfirmTagsDialog")
            }else -> {
            ConfirmReshootDialog().show(
                requireActivity().supportFragmentManager,
                "ConfirmReshootDialog"
            )
        }
        }


    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when(data){
            is ImagesOfSkuRes.Data -> {
                viewModel.sequence = position
                val list = reshootAdapter?.listItems as List<ImagesOfSkuRes.Data>

                val element = list.firstOrNull {
                    it.isSelected
                }

                if (element != null && data != element){
                    //loadOverlay(data.angle_name,data.display_thumbnail)
                    //viewModel.selectedOverlay = data

                    data.isSelected = true
                    element.isSelected = false
                    reshootAdapter?.notifyItemChanged(position)
                    reshootAdapter?.notifyItemChanged(list.indexOf(element))
                    binding.rvImages.scrollToPosition(position)
                }
            }
        }
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentReshootBinding.inflate(inflater, container, false)


}