package com.spyneai.shoot.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
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
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentOverlaysV2Binding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.InteriorAdapter
import com.spyneai.shoot.adapters.MiscAdapter
import com.spyneai.shoot.adapters.NewSubCategoriesAdapter
import com.spyneai.shoot.adapters.OverlaysAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.dialogs.*
import com.spyneai.shoot.utils.shoot
import kotlinx.coroutines.launch

class FragmentOverlaysVTwo : BaseFragment<ShootViewModel, FragmentOverlaysV2Binding>(),
    NewSubCategoriesAdapter.BtnClickListener, OnItemClickListener {

    val TAG = "FragmentOverlaysVTwo"
    lateinit var subCategoriesAdapter: NewSubCategoriesAdapter
    var interiorAdapter: InteriorAdapter? = null
    var miscAdapter: MiscAdapter? = null
    private var showDialog = true
    var pos = 0
    var snackbar : Snackbar? = null
    lateinit var overlaysAdapter: OverlaysAdapter


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

        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (viewModel.showConfirmReshootDialog.value == true && !it.isNullOrEmpty()) {
                    shoot("confirm reshoot dialog called")
                    shoot("shootList sine(no. of images)- " + it.size)
                    val element = viewModel.getCurrentShoot()
                    showImageConfirmDialog(element!!)
                }
            } catch (e: Exception) {
                Log.d(TAG, "onViewCreated: "+e.localizedMessage)
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

        observerMiscShots()

        observeStartInteriorShoot()

        observeStartMiscShoots()

        viewModel.show360InteriorDialog.observe(viewLifecycleOwner,{
            if (it)
                if (viewModel.interior360Dialog.value == null)
                    ThreeSixtyInteriorHintDialog().show(requireActivity().supportFragmentManager, "ThreeSixtyInteriorHintDialog")
        })

        observeShootDimesions()
        observeOverlays()

        viewModel.isSkuCreated.observe(viewLifecycleOwner,{
            initAngles()
        })

        viewModel.fetchOverlays.observe(viewLifecycleOwner,{

        })

        viewModel.onImageConfirmed.observe(viewLifecycleOwner,{
            if (viewModel.shootList.value != null){
                val list = overlaysAdapter.listItems as List<OverlaysResponse.Data>

                val position = viewModel.sequence

                list[position].isSelected = false
                list[position].imageClicked = true
                list[position].imagePath = viewModel.getCurrentShoot()!!.capturedImage
                overlaysAdapter.notifyItemChanged(position)

                Log.d(TAG, "onViewCreated: "+position)
                Log.d(TAG, "onViewCreated: "+list[position].imagePath)

                if (position != list.size.minus(1)){
                    list[position.plus(1)].isSelected = true
                    viewModel.sequence = position.plus(1)

                    overlaysAdapter.notifyItemChanged(position.plus(1))
                    binding.rvSubcategories.scrollToPosition(position)

                }else {
                    val element = list.firstOrNull {
                        !it.isSelected
                    }

                    if (element != null){
                        element?.isSelected = true
                        viewModel.sequence = element?.sequenceNumber!!
                        overlaysAdapter.notifyItemChanged(viewModel.sequence)
                        binding.rvSubcategories.scrollToPosition(viewModel.sequence)

                        Log.d(TAG, "onItemClick: "+viewModel.sequence)
                    }
                }


            }
        })
    }

    private fun observeShootDimesions() {
        viewModel.shootDimensions.observe(viewLifecycleOwner,{
            getPreviewDimensions(binding.imgOverlay)
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


    private fun initAngles() {
        if (viewModel.fromDrafts){
            if (requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0) == 0){
                if (requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0) == 0){
                    when(getString(R.string.app_name)){
                        AppConstants.CARS24, AppConstants.CARS24_INDIA ->
                            viewModel.exterirorAngles.value = 5
                        AppConstants.SELL_ANY_CAR ->
                            viewModel.exterirorAngles.value = 4
                        else ->  {
                            viewModel.exterirorAngles.value = 8
                        }
                    }
                }else{
                    viewModel.exterirorAngles.value = requireActivity().intent.getIntExtra(
                        AppConstants.EXTERIOR_ANGLES,0)
                }
            }
        }else{
//            if (!viewModel.fromDrafts){
//                when(getString(R.string.app_name)){
//                    AppConstants.CARS24, AppConstants.CARS24_INDIA ->
//                        viewModel.exterirorAngles.value = 5
//                    AppConstants.SELL_ANY_CAR ->
//                        viewModel.exterirorAngles.value = 4
//                    else ->  {
//                        viewModel.exterirorAngles.value = 8
//                    }
//                }
//            }
        }

        when(getString(R.string.app_name)) {
            AppConstants.KARVI, AppConstants.CARS24_INDIA, AppConstants.CARS24 -> {}
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
            binding.tvShoot?.text = getString(R.string.angles)+" 1/${viewModel.getSelectedAngles(getString(
                R.string.app_name))}"
            if (viewModel.shootList.value.isNullOrEmpty())
                initProgressFrames()
            else if (viewModel.shootList.value?.size!! < viewModel.getSelectedAngles(getString(R.string.app_name))!!)
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

                                            viewModel.interiorShootNumber.value = requireActivity().intent.getIntExtra(
                                                AppConstants.INTERIOR_SIZE,0)
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


                                            if (miscAdapter == null) {
                                                miscAdapter = MiscAdapter(requireContext(),
                                                    it.value.miscellaneous as ArrayList<NewSubCatResponse.Miscellaneous>
                                                )

                                                binding.rvSubcategories.apply {
                                                    layoutManager = LinearLayoutManager(requireContext())
                                                    this?.adapter = miscAdapter
                                                }
                                            }

                                            viewModel.miscShootNumber.value = requireActivity().intent.getIntExtra(
                                                AppConstants.MISC_SIZE,0)
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

        viewModel.shootNumber.observe(viewLifecycleOwner, {
            binding.tvShoot?.text =
                "Angles ${viewModel.sequence.plus(1)}/${viewModel.getSelectedAngles(getString(
                    R.string.app_name))}"

            viewModel.overlaysResponse.observe(viewLifecycleOwner, {
                when (it) {
                    is Resource.Success -> {
                        val name = it.value.data[viewModel.shootNumber.value!!].display_name
                        val overlay = it.value.data[viewModel.shootNumber.value!!].display_thumbnail


                        loadOverlay(name,overlay)
                    }
                    else -> {
                    }
                }
            })
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
                        .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.primary))

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



    private fun showViews() {
        binding.apply {
            binding.llAngles.visibility = View.VISIBLE
            tvSkuName?.visibility = View.VISIBLE
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
                    Utilities.hideProgressDialog()

                    //pre load overlays
                    val overlaysList = it.value.data.map { it.display_thumbnail }

                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.preloadOverlays(overlaysList)
                    }

                    //set overlays
                    it.value.data[0].isSelected = true
                    overlaysAdapter = OverlaysAdapter(it.value.data,this@FragmentOverlaysVTwo)
                    viewModel.selectedOverlay = it.value.data[0]

                    binding.rvSubcategories.apply {
                        visibility = View.VISIBLE
                        layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
                        adapter = overlaysAdapter
                    }

                    requireContext().captureEvent(
                        Events.GET_OVERLAYS,
                        Properties().putValue("angles", it.value.data.size)
                    )

                    shoot("hide progress dialog(overlays response sucess)")
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
                        viewModel.interiorShootNumber.value = requireActivity().intent.getIntExtra(
                            AppConstants.INTERIOR_SIZE,0)
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


            binding.tvShoot?.text =
                "Angles ${viewModel.interiorShootNumber.value!! + 1}/${viewModel.interiorAngles.value}"

            if (viewModel.interiorShootNumber.value!! != 0)
                interiorAdapter!!.interiorList[viewModel.interiorShootNumber.value!! - 1].isSelected =
                    false

            interiorAdapter!!.interiorList[viewModel.interiorShootNumber.value!!].isSelected = true
            interiorAdapter!!.notifyDataSetChanged()
            binding.rvSubcategories?.scrollToPosition(viewModel.interiorShootNumber.value!!)

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
                        viewModel.miscShootNumber.value = requireActivity().intent.getIntExtra(
                            AppConstants.MISC_SIZE,0)

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
                        layoutManager = LinearLayoutManager(requireContext(),
                            LinearLayoutManager.VERTICAL,false)
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

            binding.tvShoot?.text =
                "Angles ${viewModel.miscShootNumber.value!! + 1}/${viewModel.miscAngles.value}"

            if (viewModel.miscShootNumber.value!! != 0)
                miscAdapter?.miscList!![viewModel.miscShootNumber.value!! - 1].isSelected = false

            miscAdapter!!.miscList[viewModel.miscShootNumber.value!!].isSelected = true
            miscAdapter!!.notifyDataSetChanged()
            binding.rvSubcategories?.scrollToPosition(viewModel.miscShootNumber.value!!)
        })
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

        when(getString(R.string.app_name)){
            AppConstants.OLA_CABS -> {
                ConfirmTagsDialog().show(
                    requireActivity().supportFragmentManager,
                    "ConfirmTagsDialog")
            }else -> {
            ConfirmReshootDialog().show(
                requireActivity().supportFragmentManager,
                "ConfirmReshootDialog"
            )
        }
        }


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
    ) = FragmentOverlaysV2Binding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when(data){
            is OverlaysResponse.Data -> {

                if (data.imageClicked){
                    ReclickDialog().show(requireActivity().supportFragmentManager,"ReclickDialog")
                }

                viewModel.sequence = position
                val list = overlaysAdapter.listItems as List<OverlaysResponse.Data>

                val element = list.firstOrNull {
                    it.isSelected
                }

                if (element != null && data != element){
                    loadOverlay(data.angle_name,data.display_thumbnail)
                    viewModel.selectedOverlay = data

                    data.isSelected = true
                    element.isSelected = false
                    overlaysAdapter.notifyItemChanged(position)
                    overlaysAdapter.notifyItemChanged(list.indexOf(element))
                    binding.rvSubcategories.scrollToPosition(position)
                }
            }
        }
    }
}