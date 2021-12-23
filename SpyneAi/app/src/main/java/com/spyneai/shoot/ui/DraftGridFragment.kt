package com.spyneai.shoot.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.rotationMatrix
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.InfoDialog
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.FragmentGridEcomBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.adapters.ClickedAdapter
import com.spyneai.shoot.data.OnOverlaySelectionListener
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.dialogs.ReclickDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ConfirmReshootEcomDialog
import com.theartofdev.edmodo.cropper.CropImage
import java.io.File

class DraftGridFragment : BaseFragment<ShootViewModel, FragmentGridEcomBinding>(),
    OnItemClickListener, OnOverlaySelectionListener {

    var clickedAdapter : ClickedAdapter?=  null
    var position = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.showOverlay.value=false
        viewModel.showGrid.observe(viewLifecycleOwner, {
            if (it) {
                binding.groupGridLines?.visibility= View.VISIBLE
            }else binding.groupGridLines?.visibility = View.INVISIBLE
        })

        if (Utilities.getPreference(requireContext(), AppConstants.ENTERPRISE_ID)
            == AppConstants.FLIPKART_ENTERPRISE_ID){
            binding.apply {
                ivNext.visibility = View.VISIBLE
                ivEnd.visibility = View.GONE
            }
        }else{
            binding.apply {
                ivNext.visibility = View.GONE
                ivEnd.visibility = View.VISIBLE
            }
        }

        binding.ivEnd.setOnClickListener {
            if (viewModel.isStopCaptureClickable || (viewModel.fromDrafts && !viewModel.shootList.value.isNullOrEmpty()))
                viewModel.stopShoot.value = true
        }

        binding.ivNext.setOnClickListener {
            InfoDialog().show(
                requireActivity().supportFragmentManager,
                "InfoDialog"
            )
        }


        viewModel.hideLeveler.observe(viewLifecycleOwner,{
            if (viewModel.categoryDetails.value?.imageType == "Info"){
                binding.apply {
                    ivNext.visibility = View.GONE
                    ivEnd.visibility = View.VISIBLE
                }
            }
        })

        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (viewModel.showConfirmReshootDialog.value == true && !it.isNullOrEmpty()) {
                    val element = viewModel.getCurrentShoot()
                    showImageConfirmDialog(element!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        // set sku name
        viewModel.isSkuCreated.observe(viewLifecycleOwner, {
            if (it) {
                binding.tvSkuName?.text = viewModel.sku.value?.skuName
                binding.tvSkuName.visibility = View.VISIBLE
                viewModel.isSkuCreated.value = false
            }else {
                val s = ""
            }
        })

        setClickedImages()

        viewModel.onImageConfirmed.observe(viewLifecycleOwner,{
            setClickedImages()
        })

        viewModel.updateSelectItem.observe(viewLifecycleOwner,{
            if (it){
                val list = clickedAdapter?.listItems as ArrayList<ShootData>
                //update previous selected item if have any
                list.firstOrNull {
                    it.isSelected
                }?.let {
                    it.isSelected = false
                    clickedAdapter?.notifyItemChanged(list.indexOf(it))
                }

                list[viewModel.currentShoot].isSelected = true
                clickedAdapter?.notifyItemChanged(viewModel.currentShoot)
                viewModel.updateSelectItem.value = false
            }
        })
    }

    private fun setClickedImages() {
        viewModel.shootList.value?.let {
            if (it.isNotEmpty()){
                binding.tvImageCount.text = viewModel.shootList.value!!.size.toString()
                it[viewModel.currentShoot].imageClicked = true
                it[viewModel.currentShoot].isSelected = false
                //update captured images
                if (clickedAdapter == null){
                    clickedAdapter = ClickedAdapter(it,this,this)
                    binding.rvClicked.apply {
                        layoutManager = LinearLayoutManager(requireContext(),
                            LinearLayoutManager.HORIZONTAL,false)
                        adapter = clickedAdapter
                    }
                }else{
                    try {
                        if (viewModel.isReclick){
                            clickedAdapter?.notifyItemChanged(viewModel.currentShoot)
                        }else{
                            clickedAdapter?.notifyDataSetChanged()
                        }
                    }catch (e : Exception){
                        val s = ""
                    }
                }
                viewModel.overlayId = it.size
                viewModel.currentShoot = it.size
                binding.rvClicked.scrollToPosition(it.size.minus(1))
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (viewModel.showLeveler.value == null || viewModel.showLeveler.value == false){
            when(viewModel.categoryDetails.value?.categoryId){
                AppConstants.ECOM_CATEGORY_ID,
                AppConstants.FOOD_AND_BEV_CATEGORY_ID,
                AppConstants.PHOTO_BOX_CATEGORY_ID-> {
//                    viewModel.showLeveler.value = true
                    viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                    viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                    viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
                }
            }
            viewModel.showDialog = true
        }
    }

    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        if(viewModel.categoryDetails.value?.imageType=="Info"){
            CropImage.activity(Uri.fromFile(File(shootData.capturedImage)))
                .start(requireActivity())
        }else
        ConfirmReshootEcomDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when(data){
            is ShootData -> {
                if (data.imageClicked){
                    val bundle = Bundle()
                    bundle.putInt("overlay_id",data.overlayId)
                    bundle.putInt("position",position)
                    bundle.putString("image_category",data.image_category)
                    val reclickDialog = ReclickDialog()
                    reclickDialog.arguments = bundle
                    reclickDialog.show(requireActivity().supportFragmentManager,"ReclickDialog")
                }
            }
        }
    }

    override fun onOverlaySelected(view: View, position: Int, data: Any?) {
        // viewModel.overlayId = position
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentGridEcomBinding.inflate(inflater, container, false)


}