package com.spyneai.shoot.ui.ecomwithoverlays

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import com.spyneai.shoot.adapters.CapturedImageAdapter
import com.spyneai.shoot.adapters.NewSubCategoriesAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.dialogs.AngleSelectionDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ConfirmReshootEcomDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.CreateProjectEcomDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.CreateSkuEcomDialog
import com.spyneai.shoot.utils.log
import kotlinx.android.synthetic.main.fragment_overlays.*
import java.util.ArrayList

class OverlayEcomFragment : BaseFragment<ShootViewModel, FragmentOverlayEcomBinding>(),
    NewSubCategoriesAdapter.BtnClickListener {


    private var showDialog = true
    lateinit var capturedImageList: ArrayList<String>
    var position = 1
    lateinit var subCategoriesAdapter: NewSubCategoriesAdapter
    lateinit var progressAdapter: ShootProgressAdapter
    var pos = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (viewModel.projectId.value == null){
            initProjectDialog()
            log("project and SKU dialog shown")
        }

        else {
            initSkuDialog()
            log("SKU dialog shown")
        }

        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (showDialog && !it.isNullOrEmpty()){
                    showImageConfirmDialog(it.get(it.size - 1))
                }
            }catch (e : Exception){
                e.printStackTrace()
            }
        })

        viewModel.shootNumber.observe(viewLifecycleOwner, {
            binding.tvShoot?.text = "Angles $it/${viewModel.getSelectedAngles()}"
        })

//        binding.ivEndProject.setOnClickListener {
//            if (viewModel.isStopCaptureClickable)
//                viewModel.stopShoot.value = true
//        }


        //observe new image clicked
//        viewModel.shootList.observe(viewLifecycleOwner, {
//            try {
//                if (showDialog && !it.isNullOrEmpty()) {
//                    capturedImageList = ArrayList<String>()
//                    position = it.size - 1
//                    capturedImageList.clear()
//                    for (i in 0..(it.size - 1))
//                        (capturedImageList as ArrayList).add(it[i].capturedImage)
//                    initCapturedImages()
//                    showImageConfirmDialog(it.get(it.size - 1))
//                    log("call showImageConfirmDialog")
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        })

        // set sku name
//        viewModel.isSkuCreated.observe(viewLifecycleOwner, {
//            if (it) {
//                binding.tvSkuName?.text = viewModel.sku.value?.skuName
//                binding.tvSkuName.visibility = View.VISIBLE
//                log("sku name set to text view: "+viewModel.sku.value?.skuName)
//                viewModel.isSkuCreated.value = false
//            }
//        })


    }
    private fun intSubcategorySelection() {
        subCategoriesAdapter = NewSubCategoriesAdapter(
            requireContext(),
            null,
            pos,
            this
        )

        if (getResources().getConfiguration().orientation === Configuration.ORIENTATION_LANDSCAPE) {
            binding.rvSubcategories.apply {
                this?.layoutManager = LinearLayoutManager(requireContext())
                this?.adapter = subCategoriesAdapter
            }

        } else if (getResources().getConfiguration().orientation === Configuration.ORIENTATION_PORTRAIT) {
            binding.rvSubcategories.apply {
                this?.layoutManager = LinearLayoutManager(requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false)
                this?.adapter = subCategoriesAdapter
            }

        }


        viewModel.getSubCategories(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )

        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.GET_SUBCATEGORIES,
                        Properties()
                    )

                    Utilities.hideProgressDialog()
                    subCategoriesAdapter.subCategoriesList =
                        it.value.data as ArrayList<NewSubCatResponse.Data>
                    subCategoriesAdapter.notifyDataSetChanged()


                    //set default angles on sub cat response
                    initAngles()
                    initProgressFrames()
                    observeOverlays()

                    binding.clSubcatSelectionOverlay?.visibility = View.VISIBLE

                    when(viewModel.categoryDetails.value?.categoryName){
                        "Footwear" -> binding.tvSubCategory?.text = getString(R.string.footwear_subcategory)
                    }
                }
                is Resource.Loading ->  Utilities.showProgressDialog(requireContext())

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.GET_SUBCATRGORIES_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })
    }

    private fun initAngles() {
        viewModel.exterirorAngles.value = 8

        binding.tvShoot?.setOnClickListener {
            AngleSelectionDialog().show(requireFragmentManager(), "AngleSelectionDialog")
        }

        //update progress list
        viewModel.exterirorAngles.observe(viewLifecycleOwner, {
            binding.tvShoot?.text = "Angles 1/${viewModel.getSelectedAngles()}"

            initProgressFrames()
            if (viewModel.subCategory.value?.prod_cat_id != null)
                getOverlays()
        })
    }

    private fun initProgressFrames() {
        //update this shoot number
        viewModel.shootNumber.value = 0

        progressAdapter = ShootProgressAdapter(
            requireContext(),
            viewModel.getShootProgressList(viewModel.exterirorAngles.value!!))

        binding.rvProgress.apply {
            this?.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this?.adapter = progressAdapter
        }

        viewModel.shootNumber.observe(viewLifecycleOwner, {
            binding.tvShoot?.text = "Angles ${viewModel.shootNumber.value!! + 1}/${viewModel.getSelectedAngles()}"

            viewModel.overlaysResponse.observe(viewLifecycleOwner,{
                when(it){
                    is Resource.Success -> {
                        val name = it.value.data[viewModel.shootNumber.value!!].display_name
                        val overlay = it.value.data[viewModel.shootNumber.value!!].display_thumbnail

                        binding.tvAngleName?.text = name

                        val requestOptions = RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .signature(ObjectKey(overlay))

                        Glide.with(requireContext())
                            .load(overlay)
                            .addListener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    getPreviewDimensions(binding.imgOverlay!!)
                                    return false
                                }

                            })
                            .apply(requestOptions)
                            .into(binding.imgOverlay!!)

                    }
                    else -> { }
                }
            })

            progressAdapter.updateList(viewModel.shootNumber.value!!)
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


    private fun initSkuDialog() {
        CreateSkuEcomDialog().show(requireFragmentManager(), "CreateSkuEcomDialog")
        viewModel.isSkuCreated.observe(viewLifecycleOwner,{
            if (it) {
                intSubcategorySelection()
            }
        })
    }

    private fun initProjectDialog() {
        CreateProjectEcomDialog().show(requireFragmentManager(), "CreateProjectEcomDialog")
        viewModel.isSkuCreated.observe(viewLifecycleOwner,{
            if (it) {
                intSubcategorySelection()
            }
        })
    }

    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        ConfirmReshootEcomDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
    }

    override fun onBtnClick(position: Int, data: NewSubCatResponse.Data) {
        if (pos != position || !subCategoriesAdapter.selectionEnabled){

            viewModel.subCategory.value = data
            pos = position

            subCategoriesAdapter.selectionEnabled = true
            subCategoriesAdapter.notifyDataSetChanged()

            getOverlays()
        }
    }

    private fun getOverlays() {
        viewModel.subCategory.value?.let {
            viewModel.getOverlays(
                Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
                requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
                it.prod_sub_cat_id!!,
                viewModel.exterirorAngles.value.toString()
            )

            requireContext().captureEvent(
                Events.GET_OVERLAYS_INTIATED,
                Properties().putValue("angles",viewModel.exterirorAngles.value)
                    .putValue("prod_sub_cat_id", it.prod_sub_cat_id!!))

        }
    }

    private fun observeOverlays() {
        viewModel.overlaysResponse.observe(viewLifecycleOwner,{ it ->
            when(it){
                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.GET_OVERLAYS,
                        Properties().putValue("angles",it.value.data.size))

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
                    handleApiError(it)
                }
            }
        })
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