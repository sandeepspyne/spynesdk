package com.spyneai.shoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.InfoDialog
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

class DraftGridFragment : BaseFragment<ShootViewModel, FragmentGridEcomBinding>(),
    OnItemClickListener, OnOverlaySelectionListener {

    var clickedAdapter : ClickedAdapter?=  null
    var position = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            if (viewModel.isStopCaptureClickable)
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
                            clickedAdapter?.notifyItemInserted(it.size - 1)
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
            viewModel.showLeveler.value = true
            viewModel.showDialog = true
        }
    }

    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        ConfirmReshootEcomDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when(data){
            is ShootData -> {
                if (data.imageClicked){
                    val bundle = Bundle()
                    bundle.putInt("overlay_id",data.overlayId)
                    bundle.putInt("position",position)
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