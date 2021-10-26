package com.spyneai.shoot.ui.ecomwithgrid

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.posthog.android.Properties
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentGridEcomBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.CapturedImageAdapter
import com.spyneai.shoot.adapters.ClickedAdapter
import com.spyneai.shoot.data.OnOverlaySelectionListener
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.dialogs.ReclickDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ConfirmReshootEcomDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.CreateSkuEcomDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ProjectTagDialog
import com.spyneai.shoot.utils.log
import java.util.*
import kotlin.collections.ArrayList

class GridEcomFragment : BaseFragment<ShootViewModel, FragmentGridEcomBinding>(),
    OnItemClickListener, OnOverlaySelectionListener {


    var clickedAdapter : ClickedAdapter?=  null
    var position = 1


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivEndProject.setOnClickListener {
            if (viewModel.fromDrafts){
                viewModel.stopShoot.value = true
            }else {
                if (viewModel.isStopCaptureClickable)
                    viewModel.stopShoot.value = true
            }
        }

        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (!it.isNullOrEmpty()) {
                    if (viewModel.showDialog)
                        showImageConfirmDialog(it.get(it.size - 1))
                    log("call showImageConfirmDialog")
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
            }
        })

        viewModel.onImageConfirmed.observe(viewLifecycleOwner,{
            viewModel.shootList.value?.let {
                binding.tvImageCount.text = viewModel.shootList.value!!.size.toString()
                it[viewModel.currentShoot].imageClicked = true
                it[viewModel.currentShoot].isSelected = false
                //update captured images
                if (clickedAdapter == null){
                    clickedAdapter = ClickedAdapter(it,this,this)
                    binding.rvClicked.apply {
                        layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
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


    override fun onResume() {
        super.onResume()

        if (viewModel.fromDrafts){
            if (viewModel.showLeveler.value == null || viewModel.showLeveler.value == false){
                viewModel.showLeveler.value = true
                viewModel.showDialog = true
            }
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
        viewModel.overlayId = position
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentGridEcomBinding.inflate(inflater, container, false)


}