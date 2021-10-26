package com.spyneai.shoot.ui.ecomwithoverlays

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentOverlayEcomBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapter.ShootProgressAdapter
import com.spyneai.shoot.adapters.NewSubCategoriesAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.CreateSkuEcomDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ProjectTagDialog
import com.spyneai.shoot.utils.log
import kotlinx.android.synthetic.main.fragment_overlays.*
import java.util.*

class OverlayEcomFragment : BaseFragment<ShootViewModel, FragmentOverlayEcomBinding>(){


    var position = 1
    var pos = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        if (viewModel.projectId.value == null){
//            if(Utilities.getPreference(requireContext(), AppConstants.STATUS_PROJECT_NAME).toString() =="true")
//                getProjectName()
//            else
//                initProjectDialog()
//        }
//        else {
//            if (viewModel.fromDrafts){
//                when {
//                    requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0) != 0 &&
//                    requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0)
//                            == requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0) -> {
//
//                            }
//                    viewModel.subCatName.value != null -> {
//                        intSubcategorySelection(false)
//                        getOverlays()
//                        binding.llCapture.visibility = View.VISIBLE
//                    }
//                    else -> {
//                        intSubcategorySelection(true)
//                    }
//                }
//            }else {
//                initSkuDialog()
//            }
//            log("SKU dialog shown")
//        }

        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (viewModel.showDialog && !it.isNullOrEmpty()){
                    showImageConfirmDialog(it.get(it.size - 1))
                }
            }catch (e : Exception){
                e.printStackTrace()
            }
        })

        viewModel.isSkuCreated.observe(viewLifecycleOwner,{
           getOverlays()
        })

        observeOverlays()

        viewModel.isSubCategoryConfirmed.observe(viewLifecycleOwner,{
            //disable angle selection click
            binding.tvShoot?.isClickable = false
            if (it) binding.rvSubcategories?.visibility = View.GONE
        })
    }

    private fun observeOverlays() {
        viewModel.overlaysResponse.observe(viewLifecycleOwner,{ it ->
            when(it){
                is Resource.Success -> {
                    viewModel.displayName = it.value.data[0].display_name
                    viewModel.displayThumbanil = it.value.data[0].display_thumbnail

                    requireContext().captureEvent(
                        Events.GET_OVERLAYS,
                        Properties().putValue("angles",it.value.data.size))

                    if (viewModel.fromDrafts){
                        binding.tvShoot?.text = "${requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0).plus(1)}/${it.value.data.size}"
                    }else {
                        binding.tvShoot?.text = "1/${it.value.data.size}"

                    }

                    Utilities.hideProgressDialog()
                    binding.clSubcatSelectionOverlay?.visibility = View.GONE
                    showViews()
                }

                is Resource.Loading -> Utilities.showProgressDialog(requireContext())

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(Events.GET_OVERLAYS_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it) {getOverlays()}
                }
            }
        })
    }


    private fun getPreviewDimensions(view : View) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val shootDimensions = viewModel.shootDimensions.value
                shootDimensions?.overlayWidth = view.width
                shootDimensions?.overlayHeight = view.height

                viewModel.shootDimensions.value = shootDimensions
            }
        })
    }



    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        ConfirmReshootPortraitDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
    }


    private fun getOverlays() {
//        var frames = 0
//        if (viewModel.subCatName.value.equals("Men Formal"))
//            frames = 6
//        else
//            frames = 5
//
//        viewModel.exterirorAngles.value = frames


        viewModel.subCategory.value?.let {
            viewModel.getOverlays(
                Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
               viewModel.categoryDetails.value?.categoryId!!,
                it.prod_sub_cat_id!!,
                viewModel.exterirorAngles.value.toString()
            )

            requireContext().captureEvent(
                Events.GET_OVERLAYS_INTIATED,
                Properties().putValue("angles",viewModel.exterirorAngles.value.toString())
                    .putValue("prod_sub_cat_id", it.prod_sub_cat_id!!))

        }
    }



    private fun showViews() {
        binding.apply {
            tvSkuName?.visibility = View.VISIBLE
            tvAngleName?.visibility = View.VISIBLE
            llProgress?.visibility = View.VISIBLE
            imgOverlay?.visibility = View.VISIBLE
            tvSkuName?.text = viewModel.sku.value?.skuName
        }
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOverlayEcomBinding.inflate(inflater, container, false)

}