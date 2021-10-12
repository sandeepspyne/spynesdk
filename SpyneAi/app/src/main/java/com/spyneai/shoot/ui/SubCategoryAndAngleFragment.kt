package com.spyneai.shoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentSelectSubcategoryAndAngleBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.SubcatAndAngleAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.ui.dialogs.AngleSelectionDialog
import com.spyneai.shoot.utils.shoot

class SubCategoryAndAngleFragment : BaseFragment<ShootViewModel,FragmentSelectSubcategoryAndAngleBinding>(),
    OnItemClickListener {


    var subcatAndAngleAdapter : SubcatAndAngleAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getSubCategories.observe(viewLifecycleOwner,{
            getSubcategories()
        })

        observeSubcategories()
    }

    private fun getSubcategories() {
//        Utilities.showProgressDialog(requireContext())

        binding.shimmer.startShimmer()

        viewModel.getSubCategories(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )


//
//        viewModel.isSubCategorySelected.observe(viewLifecycleOwner, {
//            if (viewModel.isSubCategorySelected.value == true){
//                //set default angles on sub cat response
//                shoot("initangles, initProgressFrames, and and observe overlays called")
//                initAngles()
////                    if (viewModel.startInteriorShots.value == null){
//                initProgressFrames()
//                observeOverlays()
////                    }
//            }
//        })

//        viewModel.shootNumber.observe(viewLifecycleOwner, {
//            binding.tvShoot?.text = "Angles $it/${viewModel.getSelectedAngles()}"
//        })
    }

    fun observeSubcategories() {
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.GET_SUBCATEGORIES,
                        Properties()
                    )

                    binding.apply {
                        shimmer.stopShimmer()
                        shimmer.visibility = View.INVISIBLE
                    }

                    subcatAndAngleAdapter = SubcatAndAngleAdapter(it.value.data,this)

                    binding.rv.apply {
                        layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
                        adapter = subcatAndAngleAdapter
                    }

//                    initAngles()
//                    observeOverlays()

                }
                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.GET_SUBCATRGORIES_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    binding.shimmer.stopShimmer()

                    handleApiError(it) { getSubcategories() }
                }
            }
        })
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectSubcategoryAndAngleBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when(data){
            is NewSubCatResponse.Data -> {
                viewModel.subCategory.value = data
                selectAngles()
            }
        }
    }

    private fun selectAngles() {
       // viewModel.selectAngles.value = true
        binding.clRoot.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.transparent))

        binding.apply {
            ivArrow.visibility = View.GONE
            tvDescription.visibility = View.INVISIBLE
            rv.visibility = View.INVISIBLE
        }

        AngleSelectionDialog().show(requireActivity().supportFragmentManager, "AngleSelectionDialog")

    }
}