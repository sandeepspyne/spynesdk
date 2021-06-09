package com.spyneai.shoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.adapter.SubCategoriesAdapter
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentOverlaysBinding
import com.spyneai.shoot.adapter.ShootProgressAdapter
import com.spyneai.shoot.adapters.NewSubCategoriesAdapter
import com.spyneai.shoot.ui.dialogs.AngleSelectionDialog
import java.util.*

class OverlaysFragment : BaseFragment<ShootViewModel,FragmentOverlaysBinding>() {


    lateinit var subCategoriesAdapter: NewSubCategoriesAdapter
    lateinit var progressAdapter : ShootProgressAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAngles()
        initSubcategories()
        initProgressFrames()

        viewModel.getSubCategories("3c436435-238a-4bdc-adb8-d6182fddeb43", "cat_d8R14zUNE")
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when(it){
                is Resource.Sucess -> {
                    subCategoriesAdapter.subCategoriesList = it.value.data as ArrayList<NewSubCatResponse.Data>
                    subCategoriesAdapter.notifyDataSetChanged()
                }
                is Resource.Loading -> {

                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
            }
        })

        viewModel.shootNumber.observe(viewLifecycleOwner,{
            binding.tvShoot?.text = "Angles $it/${viewModel.getSelectedAngles()}"
        })
    }

    private fun initProgressFrames() {
        //update this shoot number
        viewModel.shootNumber.value = 1

        progressAdapter = ShootProgressAdapter(requireContext(),viewModel.getShootProgressList())

        binding.rvProgress.apply {
            this!!.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
            adapter = progressAdapter
        }

        //update progress list
        viewModel.selectedAngles.observe(viewLifecycleOwner,{
            binding.tvShoot?.text = "Angles 1/${viewModel.getSelectedAngles()}"
            progressAdapter.updateList(viewModel.getShootProgressList())
        })
    }

    private fun initAngles() {
        viewModel.selectedAngles.value = 8

        binding.tvShoot?.setOnClickListener {
            AngleSelectionDialog().show(requireFragmentManager(),"AngleSelectionDialog")
        }
    }

    private fun initSubcategories() {
        subCategoriesAdapter = NewSubCategoriesAdapter(requireContext()
            ,null,0, object : NewSubCategoriesAdapter.BtnClickListener{
                override fun onBtnClick(
                    position: Int,
                    subcategoryName: String,
                    subcategoryImage: String
                ) {

                }
            })

        binding.rvSubcategories?.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSubcategories?.adapter = subCategoriesAdapter
    }

    override fun getViewModel() =  ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOverlaysBinding.inflate(inflater, container, false)
}