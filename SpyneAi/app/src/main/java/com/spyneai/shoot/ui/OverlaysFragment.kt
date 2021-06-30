package com.spyneai.shoot.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentOverlaysBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapter.ShootProgressAdapter
import com.spyneai.shoot.adapters.InteriorAdapter
import com.spyneai.shoot.adapters.MiscAdapter
import com.spyneai.shoot.adapters.NewSubCategoriesAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.dialogs.*
import java.util.*


class OverlaysFragment : BaseFragment<ShootViewModel,FragmentOverlaysBinding>(),NewSubCategoriesAdapter.BtnClickListener {

    lateinit var subCategoriesAdapter: NewSubCategoriesAdapter
    lateinit var progressAdapter: ShootProgressAdapter
    lateinit var interiorAdapter : InteriorAdapter
    lateinit var miscAdapter : MiscAdapter
    private var showDialog = true
    var pos = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initShootHint()

        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (showDialog && !it.isNullOrEmpty()){
                    showImageConfirmDialog(it.get(it.size - 1))
                }
            }catch (e : Exception){
                e.printStackTrace()
            }
        })

        viewModel.showInteriorDialog.observe(viewLifecycleOwner,{
            if (it) {
                binding.imgOverlay?.visibility = View.GONE
                initInteriorShots()

                viewModel.startMiscShots.observe(viewLifecycleOwner,{

                })
            }
        })

        viewModel.isSubCategoryConfirmed.observe(viewLifecycleOwner,{
            if (it) binding.rvSubcategories?.visibility = View.INVISIBLE
        })
    }

    private fun initShootHint() {
        requireContext().captureEvent(Events.SHOW_HINT, Properties())
        ShootHintDialog().show(requireFragmentManager(), "ShootHintDialog")

        viewModel.showVin.observe(viewLifecycleOwner,{
            if (it) initProjectDialog()
        })
    }

    private fun initProjectDialog(){
        CreateProjectAndSkuDialog().show(requireFragmentManager(), "CreateProjectAndSkuDialog")

        viewModel.isProjectCreated.observe(viewLifecycleOwner,{
            if (it) {
                intSubcategorySelection()
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

                        Glide.with(requireContext())
                            .load(overlay)
                            .addListener(object : RequestListener<Drawable>{
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
                            .into(binding.imgOverlay!!)

                    }
                    else -> { }
                }
            })

            progressAdapter.updateList(viewModel.shootNumber.value!!)
        })

    }

    private fun intSubcategorySelection() {
        subCategoriesAdapter = NewSubCategoriesAdapter(
            requireContext(),
            null,
            pos,
            this
        )

        binding.rvSubcategories.apply {
            this?.layoutManager = LinearLayoutManager(requireContext())
            this?.adapter = subCategoriesAdapter
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
                        Properties())

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
                        "Bikes" -> binding.tvSubCategory?.text = getString(R.string.bike_subcategory)
                    }
                }
                is Resource.Loading ->  Utilities.showProgressDialog(requireContext())

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(Events.GET_SUBCATRGORIES_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it)
                }
            }
        })


        viewModel.shootNumber.observe(viewLifecycleOwner, {
            binding.tvShoot?.text = "Angles $it/${viewModel.getSelectedAngles()}"
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

    private fun initInteriorShots() {
        binding.tvShoot?.isClickable = false
        InteriorHintDialog().show(requireFragmentManager(), "InteriorHintDialog")

        viewModel.showMiscDialog.observe(viewLifecycleOwner,{
            if (it) initMiscShots()
        })

        viewModel.startInteriorShots.observe(viewLifecycleOwner,{
            if (it) startInteriorShots()
        })
    }

    private fun startInteriorShots() {
        binding.rvSubcategories?.visibility = View.VISIBLE

        viewModel.subCategoriesResponse.observe(viewLifecycleOwner,{
            when(it){
                is Resource.Success -> {

                    val interiorList = it.value.interior as ArrayList<NewSubCatResponse.Interior>

                    //set interior angles value
                    viewModel.interiorShootNumber.value = 0
                    viewModel.interiorAngles.value = interiorList.size

                    interiorAdapter = InteriorAdapter(requireContext(),interiorList)

                    binding.rvSubcategories.apply {
                        this?.adapter = interiorAdapter
                    }

                    //change image type
                    viewModel.categoryDetails.value?.imageType = "Interior"
                }
                else -> {}
            }
        })

        viewModel.interiorShootNumber.observe(viewLifecycleOwner,{
            binding.tvAngleName?.text = interiorAdapter.interiorList[viewModel.interiorShootNumber.value!!].display_name
            binding.tvShoot?.text = "Angles ${viewModel.interiorShootNumber.value!! + 1}/${viewModel.interiorAngles.value}"

            if (viewModel.interiorShootNumber.value!! != 0)
                interiorAdapter.interiorList[viewModel.interiorShootNumber.value!! - 1].isSelected = false

            interiorAdapter.interiorList[viewModel.interiorShootNumber.value!!].isSelected = true
            interiorAdapter.notifyDataSetChanged()
            binding.rvSubcategories?.scrollToPosition(viewModel.interiorShootNumber.value!!)

            if (viewModel.interiorShootNumber.value!! == 0)
                progressAdapter.updateList(viewModel.getShootProgressList(viewModel.interiorAngles.value!!))
            else
                progressAdapter.updateList(viewModel.interiorShootNumber.value!!)


        })
    }

    private fun initMiscShots() {
        MiscShotsDialog().show(requireFragmentManager(), "MiscShotsDialog")

        viewModel.startMiscShots.observe(viewLifecycleOwner,{
            if (it) startMiscShots()
        })
    }

    private fun startMiscShots() {
        binding.rvSubcategories?.visibility = View.VISIBLE

        viewModel.subCategoriesResponse.observe(viewLifecycleOwner,{
            when(it){
                is Resource.Success -> {

                    val miscList = it.value.miscellaneous as ArrayList<NewSubCatResponse.Miscellaneous>

                    //set interior angles value
                    viewModel.miscShootNumber.value = 0
                    viewModel.miscAngles.value = miscList.size

                    miscAdapter = MiscAdapter(requireContext(),miscList)

                    binding.rvSubcategories.apply {
                        this?.adapter = miscAdapter
                    }

                    //change image type
                    viewModel.categoryDetails.value?.imageType = "Focus Shoot"
                }
                else -> {}
            }
        })

        viewModel.miscShootNumber.observe(viewLifecycleOwner,{
            binding.tvAngleName?.text = miscAdapter.miscList[viewModel.miscShootNumber.value!!].display_name
            binding.tvShoot?.text = "Angles ${viewModel.miscShootNumber.value!! + 1}/${viewModel.miscAngles.value}"

            if (viewModel.miscShootNumber.value!! != 0)
                miscAdapter.miscList[viewModel.miscShootNumber.value!! - 1].isSelected = false

            miscAdapter.miscList[viewModel.miscShootNumber.value!!].isSelected = true
            miscAdapter.notifyDataSetChanged()
            binding.rvSubcategories?.scrollToPosition(viewModel.miscShootNumber.value!!)

            if (viewModel.miscShootNumber.value!! == 0)
                progressAdapter.updateList(viewModel.getShootProgressList(viewModel.miscAngles.value!!))
            else
                progressAdapter.updateList(viewModel.miscShootNumber.value!!)
        })
    }

    override fun onBtnClick(position : Int,data: NewSubCatResponse.Data) {
        if (pos != position || !subCategoriesAdapter.selectionEnabled){

            viewModel.subCategory.value = data
            pos = position

            subCategoriesAdapter.selectionEnabled = true
            subCategoriesAdapter.notifyDataSetChanged()

            getOverlays()
        }

    }

    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        ConfirmReshootDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
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


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOverlaysBinding.inflate(inflater, container, false)




}