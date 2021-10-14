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

class     OverlayEcomFragment : BaseFragment<ShootViewModel, FragmentOverlayEcomBinding>(),
    NewSubCategoriesAdapter.BtnClickListener {


    lateinit var capturedImageList: ArrayList<String>
    var position = 1
    lateinit var subCategoriesAdapter: NewSubCategoriesAdapter
    lateinit var progressAdapter: ShootProgressAdapter
    var pos = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.projectId.value == null){
            if(Utilities.getPreference(requireContext(), AppConstants.STATUS_PROJECT_NAME).toString() =="true")
                getProjectName()
            else
                initProjectDialog()
        }
        else {
            if (viewModel.fromDrafts){
                when {
                    requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0) != 0 &&
                    requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0)
                            == requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0) -> {

                            }
                    viewModel.subCatName.value != null -> {
                        intSubcategorySelection(false)
                        getOverlays()
                        binding.llCapture.visibility = View.VISIBLE
                    }
                    else -> {
                        intSubcategorySelection(true)
                    }
                }
            }else {
                initSkuDialog()
            }
            log("SKU dialog shown")
        }

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

        viewModel.overlaysResponse.observe(viewLifecycleOwner,{ it ->
            when(it){
                is Resource.Success -> {
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

        viewModel.isSubCategoryConfirmed.observe(viewLifecycleOwner,{
            //disable angle selection click
            binding.tvShoot?.isClickable = false
            if (it) binding.rvSubcategories?.visibility = View.GONE
        })
    }

    private fun intSubcategorySelection(showDialog : Boolean) {
        if (showDialog)
            Utilities.showProgressDialog(requireContext())

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
            viewModel.categoryDetails.value?.categoryId!!
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
//                    initProgressFrames()

                    if (viewModel.fromDrafts && viewModel.subCatName.value != null){
                        binding.rvSubcategories.visibility = View.GONE
                        viewModel.showLeveler.value = true
                    }else {
                        binding.clSubcatSelectionOverlay?.visibility = View.VISIBLE
                    }

                    when(viewModel.categoryDetails.value?.categoryName){
                        "Footwear" -> binding.tvSubCategory?.text = getString(R.string.footwear_subcategory)
                    }
                }

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

    private fun getProjectName(){

        viewModel.getProjectName(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())

        viewModel.getProjectNameResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {

                    Utilities.hideProgressDialog()

                    viewModel.dafault_project.value = it.value.data.dafault_project
                    viewModel.dafault_sku.value = it.value.data.dafault_sku
                    initProjectDialog()
                    log("project and SKU dialog shown")
                }

                is Resource.Loading -> {
                    Utilities.showProgressDialog(requireContext())
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    log("get project name failed")
                    requireContext().captureFailureEvent(
                        Events.CREATE_PROJECT_FAILED, Properties(),
                        it.errorMessage!!
                    )

                    Utilities.hideProgressDialog()
                    handleApiError(it) { getProjectName()}
                }
            }
        })

    }


    private fun initProgressFrames(frames: Int) {

      //  update this shoot number
//        if (viewModel.fromDrafts)
//            viewModel.shootNumber.value = requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0)
//        else
//            viewModel.shootNumber.value = 0
//
//        progressAdapter = ShootProgressAdapter(
//            requireContext(),
//            viewModel.getShootProgressList(frames, viewModel.shootNumber.value!!))
//
//        binding.rvProgress.apply {
//            this?.layoutManager =
//                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//            this?.adapter = progressAdapter
//        }
//
//        viewModel.shootNumber.observe(viewLifecycleOwner, {
//
//            binding.tvShoot?.text = "${1+ viewModel.shootNumber.value!!}/"+frames
//
//            viewModel.overlaysResponse.observe(viewLifecycleOwner,{
//                when(it){
//                    is Resource.Success -> {
//                        val name = it.value.data[viewModel.shootNumber.value!!].display_name
//                        val overlay = it.value.data[viewModel.shootNumber.value!!].display_thumbnail
//
//                        val requestOptions = RequestOptions()
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .signature(ObjectKey(overlay))
//
//                        Glide.with(requireContext())
//                            .load(overlay)
//                            .addListener(object : RequestListener<Drawable> {
//                                override fun onLoadFailed(
//                                    e: GlideException?,
//                                    model: Any?,
//                                    target: Target<Drawable>?,
//                                    isFirstResource: Boolean
//                                ): Boolean {
//                                    val properties =  Properties()
//                                    properties.put("category",viewModel.categoryDetails.value?.categoryName)
//                                    properties.put("error", e?.localizedMessage)
//
//                                    requireContext().captureEvent(
//                                        Events.OVERLAY_LOAD_FIALED,
//                                        properties
//                                    )
//
//                                    return false
//                                }
//
//                                override fun onResourceReady(
//                                    resource: Drawable?,
//                                    model: Any?,
//                                    target: Target<Drawable>?,
//                                    dataSource: DataSource?,
//                                    isFirstResource: Boolean
//                                ): Boolean {
//                                    val properties =  Properties()
//                                    properties.put("category",viewModel.categoryDetails.value?.categoryName)
//
//                                    requireContext().captureEvent(
//                                        Events.OVERLAY_LOADED,
//                                        properties
//                                    )
//
//                                    getPreviewDimensions(binding.imgOverlay!!)
//                                    return false
//                                }
//
//                            })
//                            .apply(requestOptions)
//                            .into(binding.imgOverlay!!)
//
//                    }
//                    else -> { }
//                }
//            })
//
//            progressAdapter.updateList(viewModel.shootNumber.value!!)
//
//        })

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
                intSubcategorySelection(true)
            }
        })
    }

    private fun initProjectDialog() {
        ProjectTagDialog().show(requireFragmentManager(), "ProjectTagDialog")
        viewModel.isSkuCreated.observe(viewLifecycleOwner,{
            if (it) {
                intSubcategorySelection(true)
            }
        })
    }

    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        ConfirmReshootPortraitDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
    }

    override fun onBtnClick(position: Int, data: NewSubCatResponse.Data) {
        if (pos != position || !subCategoriesAdapter.selectionEnabled){
            binding.llCapture.visibility = View.VISIBLE
            viewModel.subCategory.value = data
            pos = position

            subCategoriesAdapter.selectionEnabled = true
            subCategoriesAdapter.notifyDataSetChanged()

            viewModel.subCatName.value = data.sub_cat_name
            viewModel.showLeveler.value = true

            getOverlays()

        }
    }

    private fun getOverlays() {
        var frames = 0
        if (viewModel.subCatName.value.equals("Men Formal"))
            frames = 6
        else
            frames = 5

        viewModel.exterirorAngles.value = frames

        initProgressFrames(frames)

        viewModel.subCategory.value?.let {
            viewModel.getOverlays(
                Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
               viewModel.categoryDetails.value?.categoryId!!,
                it.prod_sub_cat_id!!,
                frames.toString()
            )

            requireContext().captureEvent(
                Events.GET_OVERLAYS_INTIATED,
                Properties().putValue("angles",frames)
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