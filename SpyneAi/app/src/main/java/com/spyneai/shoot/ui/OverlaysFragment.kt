package com.spyneai.shoot.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.snackbar.Snackbar
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
import com.spyneai.shoot.utils.shoot
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import com.iceteck.silicompressorr.videocompression.MediaController.mContext
import com.spyneai.shoot.utils.log


class OverlaysFragment : BaseFragment<ShootViewModel, FragmentOverlaysBinding>(),
    NewSubCategoriesAdapter.BtnClickListener {

    lateinit var subCategoriesAdapter: NewSubCategoriesAdapter
    var progressAdapter: ShootProgressAdapter? = null
    var interiorAdapter: InteriorAdapter? = null
    var miscAdapter: MiscAdapter? = null
    private var showDialog = true
    var pos = 0
    var snackbar : Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shoot("onCreate called(overlay fragment)")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
        shoot("onCreateView called(overlay fragment)")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shoot("onViewCreated called(overlay fragment)")

      if (viewModel.showVin.value == null) {
            shoot("shoot hint called")
            initShootHint()
        }

        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (viewModel.showConfirmReshootDialog.value == true && !it.isNullOrEmpty()) {
                    shoot("confirm reshoot dialog called")
                    shoot("shootList sine(no. of images)- " + it.size)
                    showImageConfirmDialog(it.get(it.size - 1))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        viewModel.showInteriorDialog.observe(viewLifecycleOwner, {
            if (it) {
                binding.imgOverlay?.visibility = View.GONE
                if (viewModel.startInteriorShots.value == null)
                    initInteriorShots()
            }
        })

        viewModel.isSubCategoryConfirmed.observe(viewLifecycleOwner, {
            //disable angle selection click
            binding.tvShoot?.isClickable = false
            if (it) {
                binding.rvSubcategories?.visibility = View.INVISIBLE
            }
        })

        observeIsProjectCreated()

        observerMiscShots()

        observeShowVin()

        observeStartInteriorShoot()

        observeStartMiscShoots()

        viewModel.show360InteriorDialog.observe(viewLifecycleOwner,{
            if (it)
                if (viewModel.interior360Dialog.value == null)
                ThreeSixtyInteriorHintDialog().show(requireActivity().supportFragmentManager, "ThreeSixtyInteriorHintDialog")
        })
    }

    private fun observeShowVin() {
        viewModel.showVin.observe(viewLifecycleOwner, {
            if (viewModel.isProjectCreated.value == null)
                if (it) {
                    initProjectDialog()
                    shoot("create project called")
                }
        })
    }

    private fun observeIsProjectCreated() {
        viewModel.isProjectCreated.observe(viewLifecycleOwner, {
            if (it) {
                if (viewModel.isSubCategoryConfirmed.value == null) {
                    shoot("init subCategory called")
                    intSubcategorySelection()
                } else {
                    //set default angles on sub cat response
                    shoot("initangles, initProgressFrames, and and observe overlays called")
                    initAngles()
//                    if (viewModel.startInteriorShots.value == null){
                    observeOverlays()
//                    }
                }
            }
        })
    }

    private fun observeStartInteriorShoot() {
        viewModel.startInteriorShots.observe(viewLifecycleOwner, {
            if (it && viewModel.startInteriorShoot.value == null)
                startInteriorShots()
        })
    }

    private fun observeStartMiscShoots() {
        viewModel.startMiscShots.observe(viewLifecycleOwner, {
            if (it)
                startMiscShots()
        })
    }

    private fun initShootHint() {
        requireContext().captureEvent(Events.SHOW_HINT, Properties())
        ShootHintDialog().show(requireActivity().supportFragmentManager, "ShootHintDialog")

    }


    private fun initProjectDialog() {
        CreateProjectAndSkuDialog().show(
            requireActivity().supportFragmentManager,
            "CreateProjectAndSkuDialog"
        )
    }


    private fun initAngles() {
       if (viewModel.fromDrafts){
           if (requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0) == 0){
               if (requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0) == 0){
                   when(getString(R.string.app_name)){
                       AppConstants.CARS24,AppConstants.CARS24_INDIA ->
                           viewModel.exterirorAngles.value = 5
                       else ->  {
                           viewModel.exterirorAngles.value = 8
                       }
                   }
               }else{
                   viewModel.exterirorAngles.value = requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0)

               }
              }
       }else{
           if (!viewModel.fromDrafts){
               when(getString(R.string.app_name)){
                AppConstants.CARS24,AppConstants.CARS24_INDIA ->  viewModel.exterirorAngles.value = 5
                    else ->  {
                        viewModel.exterirorAngles.value = 8
                    }
                }
           }
       }

        when(getString(R.string.app_name)) {
            AppConstants.KARVI,AppConstants.CARS24_INDIA,AppConstants.CARS24 -> {}
            else -> {
                    binding.tvShoot?.setOnClickListener {
                        if ((viewModel.startInteriorShots.value != true || viewModel.startMiscShots.value != true )
                            &&
                            viewModel.sku.value?.skuId == null)
                        AngleSelectionDialog().show(requireActivity().supportFragmentManager, "AngleSelectionDialog"
                        )
                    }


            }
        }

        //update progress list
        viewModel.exterirorAngles.observe(viewLifecycleOwner, {
            binding.tvShoot?.text = "Angles 1/${viewModel.getSelectedAngles()}"
            if (viewModel.shootList.value.isNullOrEmpty())
                initProgressFrames()
            else if (viewModel.shootList.value?.size!! < viewModel.getSelectedAngles()!!)
                initProgressFrames()

            if (viewModel.subCategory.value?.prod_cat_id != null)
                getOverlays()
        })
    }

    private fun initProgressFrames() {
        //update this shoot number
        if (viewModel.fromDrafts){
            val intent = requireActivity().intent
            viewModel.shootNumber.value = requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0)

            when {
                intent.getBooleanExtra(AppConstants.RESUME_EXTERIOR,false) -> {
                    viewModel.shootNumber.value = requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0)
                    startExteriroShot()
                }
                intent.getBooleanExtra(AppConstants.RESUME_INTERIOR,false) -> {
                    binding.tvShoot?.isClickable = false

                    viewModel.subCategoriesResponse.observe(
                        viewLifecycleOwner,{
                            when(it) {
                                is Resource.Success -> {
                                    when (requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE,0)
                                    ) {
                                        it.value.interior.size -> {
                                            viewModel.showMiscDialog.value = true
                                        }
                                        0 -> {
                                            viewModel.showInteriorDialog.value = true
                                        }
                                        else -> {
                                            viewModel.startInteriorShots.value = true
                                            viewModel.interiorAngles.value =  it.value.interior.size

                                            if (interiorAdapter == null) {
                                                    interiorAdapter = InteriorAdapter(requireContext(),
                                                        it.value.interior as ArrayList<NewSubCatResponse.Interior>
                                                    )

                                                    binding.rvSubcategories.apply {
                                                        layoutManager = LinearLayoutManager(requireContext())
                                                        this?.adapter = interiorAdapter
                                                    }
                                                }

                                            viewModel.interiorShootNumber.value = requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE,0)
                                        }
                                    }
                                }
                            }
                        }
                    )

                }
                intent.getBooleanExtra(AppConstants.RESUME_MISC,false) -> {
                    binding.tvShoot?.isClickable = false

                    viewModel.subCategoriesResponse.observe(
                        viewLifecycleOwner,{
                            when(it) {
                                is Resource.Success -> {
                                    when {
                                        intent.getIntExtra(AppConstants.MISC_SIZE,0) ==
                                                it.value.miscellaneous.size -> {
                                            //select background
                                            viewModel.selectBackground.value = true
                                        }
                                        intent.getIntExtra(AppConstants.MISC_SIZE,0) ==
                                                0 -> {
                                            viewModel.showMiscDialog.value = true
                                        }
                                        else -> {
                                            viewModel.startMiscShots.value = true
                                            if (viewModel.categoryDetails.value?.categoryName == "Bikes") {
                                                val filteredList: List<NewSubCatResponse.Miscellaneous> = it.value.miscellaneous.filter {
                                                    it.prod_sub_cat_id ==   viewModel.subCategory.value?.prod_sub_cat_id
                                                }

                                                it.value.miscellaneous = filteredList
                                            }

                                            viewModel.miscAngles.value =  it.value.miscellaneous.size

                                            if (progressAdapter == null) {
                                                progressAdapter = ShootProgressAdapter(
                                                    requireContext(),
                                                    viewModel.getShootProgressList(
                                                        viewModel.miscAngles.value!!,
                                                        intent.getIntExtra(AppConstants.MISC_SIZE,0)
                                                    )
                                                )

                                                binding.rvProgress.apply {
                                                    layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
                                                    this?.adapter = progressAdapter
                                                }
                                            }

                                            if (miscAdapter == null) {
                                                miscAdapter = MiscAdapter(requireContext(),
                                                    it.value.miscellaneous as ArrayList<NewSubCatResponse.Miscellaneous>
                                                )

                                                binding.rvSubcategories.apply {
                                                    layoutManager = LinearLayoutManager(requireContext())
                                                    this?.adapter = miscAdapter
                                                }
                                            }

                                            viewModel.miscShootNumber.value = requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE,0)
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    )
                }
            }
           }else {
            if (viewModel.shootList.value.isNullOrEmpty()) {
                viewModel.shootNumber.value = 0
                shoot("shoot number is 0")
            } else {
                viewModel.shootNumber.value = viewModel.shootList.value!!.size
                shoot("shoot number is- " + viewModel.shootList.value!!.size)
            }

            startExteriroShot()
        }
    }

    private fun startExteriroShot() {
        progressAdapter = ShootProgressAdapter(
            requireContext(),
            viewModel.getShootProgressList(
                viewModel.exterirorAngles.value!!,
                viewModel.shootNumber.value!!
            )
        )

        binding.rvProgress.apply {
            this?.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this?.adapter = progressAdapter
        }

        viewModel.shootNumber.observe(viewLifecycleOwner, {
            binding.tvShoot?.text =
                "Angles ${viewModel.shootNumber.value!! + 1}/${viewModel.getSelectedAngles()}"

            viewModel.overlaysResponse.observe(viewLifecycleOwner, {
                when (it) {
                    is Resource.Success -> {
                        val name = it.value.data[viewModel.shootNumber.value!!].display_name
                        val overlay = it.value.data[viewModel.shootNumber.value!!].display_thumbnail

                        binding.tvAngleName?.text = name

                        loadOverlay(name,overlay)

                    }
                    else -> {
                    }
                }
            })
            progressAdapter!!.updateList(viewModel.shootNumber.value!!)
            shoot("updateList in progress adapter called- " + viewModel.shootNumber.value!!)
        })
    }

    private fun loadOverlay(name : String,overlay : String) {

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
                                    val properties =  Properties()
                                    properties["name"] = name
                                    properties["error"] = e?.localizedMessage
                                    properties["category"] = viewModel.categoryDetails.value?.categoryName

                                    requireContext().captureEvent(
                                        Events.OVERLAY_LOAD_FIALED,
                                        properties
                                    )

                                    snackbar = Snackbar.make(binding.root, "Overlay Failed to load", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("Retry") {
                                            loadOverlay(name,overlay)
                                        }
                                        .setActionTextColor(ContextCompat.getColor(requireContext(),R.color.primary))

                                    snackbar?.show()
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {

                                   if (snackbar != null)
                                       snackbar?.dismiss()

                                    val properties =  Properties()
                                    properties["name"] = name
                                    properties["category"] = viewModel.categoryDetails.value?.categoryName

                                    requireContext().captureEvent(
                                        Events.OVERLAY_LOADED,
                                        properties
                                    )

                                    getPreviewDimensions(binding.imgOverlay!!)
                                    return false
                                }

                            })
                            .apply(requestOptions)
                            .into(binding.imgOverlay!!)

    }

    private fun intSubcategorySelection() {
        if (requireActivity().intent.getBooleanExtra("from_drafts",false))
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
                this?.layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
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

                    //set default angles on sub cat response
                    shoot("initangles, initProgressFrames, and and observe overlays called")
                    initAngles()
//                    if (viewModel.startInteriorShots.value == null){
                    observeOverlays()
//                    }

                    Utilities.hideProgressDialog()
                    shoot("hide progress dialog(subCatResponse sucess)")
                    subCategoriesAdapter.subCategoriesList =
                        it.value.data as ArrayList<NewSubCatResponse.Data>
                    subCategoriesAdapter.notifyDataSetChanged()

                    binding.clSubcatSelectionOverlay?.visibility = View.VISIBLE

                    when (viewModel.categoryDetails.value?.categoryName) {
//                        "Bikes" -> binding.tvSubCategory?.text =
//                            getString(R.string.bike_subcategory)
                    }
                }
                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.GET_SUBCATRGORIES_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    shoot("hide progress dialog(subCatResponse failur)")
                    handleApiError(it)
                }
            }
        })
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
            binding.imgOverlay.visibility = View.VISIBLE
            if (viewModel.startInteriorShots.value == true || viewModel.startMiscShots.value == true)
                binding.imgOverlay.visibility = View.INVISIBLE

//            if (viewModel.sku.value?.skuId != null && viewModel.categoryDetails.value?.imageType == "Exterior")
//                viewModel.showLeveler.value = true

            if (viewModel.categoryDetails.value?.imageType == "Exterior")
                viewModel.showLeveler.value = true

        }

        if (getString(R.string.app_name) == AppConstants.KARVI)
            binding.imgOverlay.visibility = View.GONE

        val intent = requireActivity().intent

        if (viewModel.fromDrafts){
            viewModel.isSubCategorySelected.value = true
          //  viewModel.shootList.value = ArrayList()
            viewModel.shootNumber.value = 0

            when{
                intent.getBooleanExtra(AppConstants.RESUME_EXTERIOR,false) -> {
                    viewModel.showLeveler.value = true
                    viewModel.shootNumber.value = intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0)

                    if (getString(R.string.app_name) != AppConstants.KARVI)
                        binding.imgOverlay.visibility = View.VISIBLE
                    if (viewModel.startInteriorShots.value == true || viewModel.startMiscShots.value == true)
                        binding.imgOverlay.visibility = View.INVISIBLE
                }
                intent.getBooleanExtra(AppConstants.RESUME_INTERIOR,false) -> {
                    binding.imgOverlay.visibility = View.GONE
                }
                intent.getBooleanExtra(AppConstants.RESUME_MISC,false) -> {
                    binding.imgOverlay.visibility = View.GONE
                }
                else -> {

                }

            }
        }
    }

    private fun getOverlays() {
        var s = ""
        viewModel.subCategory.value?.let {
            viewModel.getOverlays(
                Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
                requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
                it.prod_sub_cat_id!!,
                viewModel.exterirorAngles.value.toString()
            )

            requireContext().captureEvent(
                Events.GET_OVERLAYS_INTIATED,
                Properties().putValue("angles", viewModel.exterirorAngles.value)
                    .putValue("prod_sub_cat_id", it.prod_sub_cat_id!!)
            )

        }
    }

    private fun observeOverlays() {
        viewModel.overlaysResponse.observe(viewLifecycleOwner, { it ->
            when (it) {
                is Resource.Success -> {
                    //pre load overlays
                    val overlaysList = it.value.data.map { it.display_thumbnail }

                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.preloadOverlays(overlaysList)
                    }

                    requireContext().captureEvent(
                        Events.GET_OVERLAYS,
                        Properties().putValue("angles", it.value.data.size)
                    )

                    Utilities.hideProgressDialog()
                    shoot("hide progress dialog(overlays response sucess)")
                    binding.clSubcatSelectionOverlay?.visibility = View.GONE
                    showViews()
                }

                is Resource.Loading -> {
                    Utilities.showProgressDialog(requireContext())
                    shoot("show progress dialog(overlays response)")
                }

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.GET_OVERLAYS_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    shoot("show progress dialog(overlays response failure)")
                    handleApiError(it) {getOverlays()}
                }
            }
        })
    }

    private fun initInteriorShots() {
        viewModel.hideLeveler.value = true

        InteriorHintDialog().show(requireActivity().supportFragmentManager, "InteriorHintDialog")
    }

    private fun startInteriorShots() {
        viewModel.isCameraButtonClickable = true

        binding.rvSubcategories?.visibility = View.VISIBLE
        binding.tvShoot.isClickable = false
        binding.imgOverlay.visibility = View.INVISIBLE
        viewModel.hideLeveler.value = true

        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    val interiorList = it.value.interior as ArrayList<NewSubCatResponse.Interior>

                    if (viewModel.fromDrafts){
                        viewModel.interiorShootNumber.value = requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE,0)
                    }else {
                        val myInteriorShootList = viewModel.shootList.value?.filter {
                            it.image_category == "Interior"
                        }

                        //set interior angles value
                        if (!myInteriorShootList.isNullOrEmpty()) {
                            viewModel.interiorShootNumber.value = myInteriorShootList.size - 1
                            interiorList.get(myInteriorShootList.size - 1).isSelected = true
                        } else
                            viewModel.interiorShootNumber.value = 0
                    }

                    viewModel.interiorAngles.value = interiorList.size

                    interiorAdapter = InteriorAdapter(requireContext(), interiorList)

                    binding.rvSubcategories.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        this?.adapter = interiorAdapter
                    }
                    //change image type
                    viewModel.categoryDetails.value?.imageType = "Interior"
                }
                else -> {
                }
            }
        })

        viewModel.interiorShootNumber.observe(viewLifecycleOwner, {
            binding.tvAngleName?.text =
                interiorAdapter?.interiorList!![viewModel.interiorShootNumber.value!!].display_name
            binding.tvShoot?.text =
                "Angles ${viewModel.interiorShootNumber.value!! + 1}/${viewModel.interiorAngles.value}"

            if (viewModel.interiorShootNumber.value!! != 0)
                interiorAdapter!!.interiorList[viewModel.interiorShootNumber.value!! - 1].isSelected =
                    false

            interiorAdapter!!.interiorList[viewModel.interiorShootNumber.value!!].isSelected = true
            interiorAdapter!!.notifyDataSetChanged()
            binding.rvSubcategories?.scrollToPosition(viewModel.interiorShootNumber.value!!)

            progressAdapter = ShootProgressAdapter(
                requireContext(),
                viewModel.getShootProgressList(
                    viewModel.interiorAngles.value!!,
                    viewModel.interiorShootNumber.value!!
                )
            )

            binding.rvProgress.apply {
                this?.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                this?.adapter = progressAdapter
            }

            if (viewModel.interiorShootNumber.value!! == 0)
                progressAdapter!!.updateList(
                    viewModel.getShootProgressList(
                        viewModel.interiorAngles.value!!,
                        viewModel.interiorShootNumber.value!!
                    )
                )
            else
                progressAdapter!!.updateList(viewModel.interiorShootNumber.value!!)
            shoot("updateList in progress adapter called- " + viewModel.interiorShootNumber.value!!)
        })
    }

    private fun observerMiscShots() {
        viewModel.showMiscDialog.observe(viewLifecycleOwner, {
            if (it) {
                binding.imgOverlay?.visibility = View.GONE
                initMiscShots()
            }
        })
    }

    private fun initMiscShots() {
        viewModel.hideLeveler.value = true

        if (viewModel.startMiscShots.value == null && !viewModel.miscDialogShowed) {
            MiscShotsDialog().show(requireActivity().supportFragmentManager, "MiscShotsDialog")
            viewModel.miscDialogShowed = true
        }
    }

    private fun startMiscShots() {
        viewModel.isCameraButtonClickable = true

        binding.rvSubcategories?.visibility = View.VISIBLE
        binding.imgOverlay.visibility = View.INVISIBLE
        viewModel.hideLeveler.value = true

        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    var miscList = it.value.miscellaneous

                   if (viewModel.fromDrafts) {
                       viewModel.miscAngles.value =  it.value.miscellaneous.size
                       viewModel.miscShootNumber.value = requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE,0)

                       if (viewModel.categoryDetails.value?.categoryName == "Bikes") {
                           val filteredList: List<NewSubCatResponse.Miscellaneous> = it.value.miscellaneous.filter {
                               it.prod_sub_cat_id ==   viewModel.subCategory.value?.prod_sub_cat_id
                           }

                           it.value.miscellaneous = filteredList
                           miscList = it.value.miscellaneous
                       }

                   }
                   else {
                       val myMiscShootList = viewModel.shootList.value?.filter {
                           it.image_category == "Focus Shoot"
                       }

                    //set interior angles value
                    if (!myMiscShootList.isNullOrEmpty()) {
                        viewModel.miscShootNumber.value = myMiscShootList.size - 1
                        miscList.get(myMiscShootList.size - 1).isSelected = true
                    } else
                        viewModel.miscShootNumber.value = 0
                   }
                    viewModel.miscAngles.value = miscList.size

                    miscAdapter = MiscAdapter(
                        requireContext(),
                        miscList as ArrayList<NewSubCatResponse.Miscellaneous>
                    )

                    binding.rvSubcategories.apply {
                        layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
                        this?.adapter = miscAdapter
                    }

                    //change image type
                    viewModel.categoryDetails.value?.imageType = "Focus Shoot"
                }
                else -> {
                }
            }
        })

        viewModel.miscShootNumber.observe(viewLifecycleOwner, {
            binding.tvAngleName?.text =
                miscAdapter?.miscList!![viewModel.miscShootNumber.value!!].display_name
            binding.tvShoot?.text =
                "Angles ${viewModel.miscShootNumber.value!! + 1}/${viewModel.miscAngles.value}"

            if (viewModel.miscShootNumber.value!! != 0)
                miscAdapter?.miscList!![viewModel.miscShootNumber.value!! - 1].isSelected = false

            miscAdapter!!.miscList[viewModel.miscShootNumber.value!!].isSelected = true
            miscAdapter!!.notifyDataSetChanged()
            binding.rvSubcategories?.scrollToPosition(viewModel.miscShootNumber.value!!)

            if (viewModel.miscShootNumber.value!! == 0){
                if (progressAdapter == null) {
                    progressAdapter = ShootProgressAdapter(
                        requireContext(),
                        viewModel.getShootProgressList(
                            viewModel.miscAngles.value!!,
                            requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE,0)
                        )
                    )

                    binding.rvProgress.apply {
                        layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
                        this?.adapter = progressAdapter
                    }

                }else {
                    progressAdapter!!.updateList(
                        viewModel.getShootProgressList(
                            viewModel.miscAngles.value!!,
                            viewModel.miscShootNumber.value!!
                        )
                    )
                }
            } else {
                if (progressAdapter == null){
                    progressAdapter = ShootProgressAdapter(
                        requireContext(),
                        viewModel.getShootProgressList(
                            viewModel.miscAngles.value!!,
                            viewModel.miscShootNumber.value!!)
                        )
                    binding.rvProgress.apply {
                        layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
                        this?.adapter = progressAdapter
                    }
                }
                progressAdapter!!.updateList(viewModel.miscShootNumber.value!!)
                shoot("updateList in progress adapter called- " + viewModel.miscShootNumber.value!!)
            }})
    }

    override fun onBtnClick(position: Int, data: NewSubCatResponse.Data) {
        if (pos != position || !subCategoriesAdapter.selectionEnabled) {

            viewModel.subCategory.value = data
            pos = position

            subCategoriesAdapter.selectionEnabled = true
            subCategoriesAdapter.notifyDataSetChanged()

            getOverlays()
            shoot("get overlays called")

            viewModel.isSubCategorySelected.value = true
            viewModel.showLeveler.value = true

            if (viewModel.fromDrafts)
                startExteriroShot()
            shoot("isSubCategorySelected is true")
        }
    }

    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        ConfirmTagsDialog().show(
            requireActivity().supportFragmentManager,
            "ConfirmReshootDialog"
        )

//        ConfirmReshootDialog().show(
//            requireActivity().supportFragmentManager,
//            "ConfirmReshootDialog"
//        )
    }

    private fun getPreviewDimensions(view: View) {
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

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        shoot("onViewStateRestored called(overlay fragment)")
    }

    override fun onStart() {
        super.onStart()
        shoot("onStart called(overlay fragment)")
    }

    override fun onPause() {
        super.onPause()
        shoot("onPause called(overlay fragment)")
    }

    override fun onStop() {
        super.onStop()
        shoot("onStop called(overlay fragment)")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        shoot("onSaveInstanceState called(overlay fragment)")
    }

    override fun onResume() {
        super.onResume()
        shoot("onResume called(overlay fragment)")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        shoot("onDestroyView called(overlay fragment)")
    }

    override fun onDestroy() {
        super.onDestroy()
        shoot("onDestroy called(overlay fragment)")
        Utilities.hideProgressDialog()
        viewModel.showConfirmReshootDialog.value = false
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOverlaysBinding.inflate(inflater, container, false)
}