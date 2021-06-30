package com.spyneai.shoot.ui.ecom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentOverlaysEcomBinding
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.dialogs.ConfirmReshootDialog
import com.spyneai.shoot.ui.dialogs.CreateProjectAndSkuDialog

class OverlaysEcomFragment : BaseFragment<ShootViewModel, FragmentOverlaysEcomBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //observe new image clicked
//        viewModel.shootList.observe(viewLifecycleOwner, {
//            try {
//                if (showDialog && !it.isNullOrEmpty()){
//                    showImageConfirmDialog(it.get(it.size - 1))
//                }
//            }catch (e : Exception){
//                e.printStackTrace()
//            }
//        })


//        private fun initProjectDialog(){
//            CreateProjectAndSkuDialog().show(requireFragmentManager(), "CreateProjectAndSkuDialog")
//
//            viewModel.isProjectCreated.observe(viewLifecycleOwner,{
//                if (it) {
//                    intSubcategorySelection()
//                }
//            })
//
//        }
//
//        private fun showImageConfirmDialog(shootData: ShootData) {
//            viewModel.shootData.value = shootData
//            ConfirmReshootDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
//        }
//
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOverlaysEcomBinding.inflate(inflater, container, false)

}