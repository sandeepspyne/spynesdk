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
import com.spyneai.setLocale
import com.spyneai.shoot.adapters.OverlaysAdapter
import com.spyneai.shoot.data.DraftClickedImages
import com.spyneai.shoot.data.OnOverlaySelectionListener
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.dialogs.*
import com.spyneai.shoot.utils.shoot
import kotlinx.coroutines.launch

class DraftShootFragment : BaseFragment<ShootViewModel, FragmentOverlaysV2Binding>(),
    OnItemClickListener, OnOverlaySelectionListener {

    val TAG = "FragmentOverlaysVTwo"
    private var showDialog = true
    var pos = 0
    var snackbar: Snackbar? = null
    lateinit var overlaysAdapter: OverlaysAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireContext().setLocale()

        viewModel.showOverlay.observe(viewLifecycleOwner, {
            if (it) {
                binding.imgOverlay.visibility = View.VISIBLE
            }else binding.imgOverlay.visibility = View.INVISIBLE
        })

        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (viewModel.showConfirmReshootDialog.value == true && !it.isNullOrEmpty()) {
                    val element = viewModel.getCurrentShoot()
                    showImageConfirmDialog(element!!)
                }
            } catch (e: Exception) {
                Log.d(TAG, "onViewCreated: " + e.localizedMessage)
                e.printStackTrace()
            }
        })

        viewModel.showInteriorDialog.observe(viewLifecycleOwner, {
            if (it) {
//                binding.imgOverlay?.visibility = View.GONE
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

        observeShootDimesions()

        getSubcategories()

        observeSubcategries()

        when (viewModel.categoryDetails.value?.imageType) {
            "Exterior" -> {
                observeOverlays()

                observeStartInteriorShoot()

                observeStartMiscShoots()

                observerMiscShots()
            }

            "Interior" -> {
                showViews()

                observeStartInteriorShoot()

                observeStartMiscShoots()

                observerMiscShots()
            }

            "Focus Shoot" -> {
                showViews()

                observeStartMiscShoots()

                observerMiscShots()
            }
        }

        viewModel.show360InteriorDialog.observe(viewLifecycleOwner, {
            if (it)
                if (viewModel.interior360Dialog.value == null)
                    ThreeSixtyInteriorHintDialog().show(
                        requireActivity().supportFragmentManager,
                        "ThreeSixtyInteriorHintDialog"
                    )
        })

        viewModel.isSkuCreated.observe(viewLifecycleOwner, {
            initAngles()
        })

        viewModel.onImageConfirmed.observe(viewLifecycleOwner, {
            if (viewModel.shootList.value != null) {
                viewModel.setSelectedItem(overlaysAdapter.listItems)
            }

            when (viewModel.categoryDetails.value?.imageType) {
                "Exterior" -> {
                    val list = overlaysAdapter.listItems as List<OverlaysResponse.Overlays>
                    viewModel.allExteriorClicked = list.all {
                        it.imageClicked
                    }
                }

                "Interior" -> {
                    val list = overlaysAdapter.listItems as List<NewSubCatResponse.Interior>
                    viewModel.allInteriorClicked = list.all {
                        it.imageClicked
                    }
                }

                "Focus Shoot" -> {
                    val list = overlaysAdapter.listItems as List<NewSubCatResponse.Miscellaneous>
                    viewModel.allMisc = list.all {
                        it.imageClicked
                    }
                }
            }
        })

        viewModel.updateSelectItem.observe(viewLifecycleOwner,{
            if (it){
                when(viewModel.categoryDetails.value?.imageType){
                    "Exterior" -> {
                        val list = overlaysAdapter?.listItems as List<OverlaysResponse.Overlays>

                        val element = list.firstOrNull {
                            it.isSelected
                        }

                        val data = list[viewModel.currentShoot]

                        viewModel.overlayId = data.id

                        if (element != null && data != element){
                            data.isSelected = true
                            element.isSelected = false
                            overlaysAdapter?.notifyItemChanged(viewModel.currentShoot)
                            overlaysAdapter?.notifyItemChanged(list.indexOf(element))
                            binding.rvSubcategories.scrollToPosition(viewModel.currentShoot)
                        }
                    }

                    "Interior" -> {
                        val list = overlaysAdapter?.listItems as List<NewSubCatResponse.Interior>

                        val element = list.firstOrNull {
                            it.isSelected
                        }

                        val data = list[viewModel.currentShoot]
                        viewModel.overlayId = data.overlayId

                        if (element != null && data != element){
                            data.isSelected = true
                            element.isSelected = false
                            overlaysAdapter?.notifyItemChanged(viewModel.currentShoot)
                            overlaysAdapter?.notifyItemChanged(list.indexOf(element))
                            binding.rvSubcategories.scrollToPosition(viewModel.currentShoot)
                        }
                    }

                    "Focus Shoot" -> {
                        val list = overlaysAdapter?.listItems as List<NewSubCatResponse.Miscellaneous>

                        val element = list.firstOrNull {
                            it.isSelected
                        }

                        val data = list[viewModel.currentShoot]
                        viewModel.overlayId = data.overlayId

                        if (element != null && data != element){
                            data.isSelected = true
                            element.isSelected = false
                            overlaysAdapter?.notifyItemChanged(viewModel.currentShoot)
                            overlaysAdapter?.notifyItemChanged(list.indexOf(element))
                            binding.rvSubcategories.scrollToPosition(viewModel.currentShoot)
                        }
                    }
                }

            }
        })

        viewModel.notifyItemChanged.observe(viewLifecycleOwner, {
            overlaysAdapter.notifyItemChanged(it)
        })

        viewModel.scrollView.observe(viewLifecycleOwner, {
            binding.rvSubcategories.scrollToPosition(it)
        })
    }

    private fun observeSubcategries() {
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    val subCategoriesResponse = it.value
                    val intent = requireActivity().intent
                    when {
                        intent.getBooleanExtra(AppConstants.RESUME_EXTERIOR, false) -> {
                            getOverlays()
                        }
                        intent.getBooleanExtra(AppConstants.RESUME_INTERIOR, false) -> {
                            binding.tvShoot?.isClickable = false

                            when (requireActivity().intent.getIntExtra(
                                AppConstants.INTERIOR_SIZE,
                                0
                            )) {
                                subCategoriesResponse.interior.size -> {
                                    viewModel.showMiscDialog.value = true
                                }
                                0 -> {
                                    viewModel.showInteriorDialog.value = true
                                }
                                else -> {
                                    viewModel.startInteriorShots.value = true
                                    viewModel.interiorAngles.value = subCategoriesResponse.interior.size
                                }
                            }
                        }

                        intent.getBooleanExtra(AppConstants.RESUME_MISC, false) -> {
                            binding.tvShoot?.isClickable = false

                            when {
                                intent.getIntExtra(AppConstants.MISC_SIZE, 0) ==
                                        subCategoriesResponse.miscellaneous.size -> {
                                    //select background
                                    viewModel.selectBackground.value = true
                                }
                                intent.getIntExtra(AppConstants.MISC_SIZE, 0) ==
                                        0 -> {
                                    viewModel.showMiscDialog.value = true
                                }
                                else -> {
                                    if (viewModel.categoryDetails.value?.categoryName == "Bikes") {
                                        val filteredList: List<NewSubCatResponse.Miscellaneous> =
                                            subCategoriesResponse.miscellaneous.filter {
                                                it.prod_sub_cat_id == viewModel.subCategory.value?.prod_sub_cat_id
                                            }

                                        subCategoriesResponse.miscellaneous = filteredList
                                    }

                                    viewModel.startMiscShots.value = true

//
//
                                    viewModel.miscAngles.value =
                                        subCategoriesResponse.miscellaneous.size
                                }
                            }
                        }
                    }
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { getSubcategories() }
                }
            }
        })
    }

    private fun getSubcategories() {
        Utilities.showProgressDialog(requireContext())

        viewModel.getSubCategories(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString()
        )
    }

    private fun observeShootDimesions() {
        viewModel.shootDimensions.observe(viewLifecycleOwner, {
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
        if (requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE, 0) == 0) {
            if (requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES, 0) == 0) {
                when (getString(R.string.app_name)) {
                    AppConstants.SELL_ANY_CAR ->
                        viewModel.exterirorAngles.value = 4
                    else -> {
                        viewModel.exterirorAngles.value = 8
                    }
                }

                getOverlays()
            } else {
                viewModel.exterirorAngles.value = requireActivity().intent.getIntExtra(
                    AppConstants.EXTERIOR_ANGLES, 0
                )
            }
        }
    }


    private fun loadOverlay(name: String, overlay: String) {

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
                    val properties = HashMap<String, Any?>()
                    properties["name"] = name
                    properties["error"] = e?.localizedMessage
                    properties["category"] = viewModel.categoryDetails.value?.categoryName

                    requireContext().captureEvent(
                        Events.OVERLAY_LOAD_FIALED,
                        properties
                    )

                    snackbar = Snackbar.make(
                        binding.root,
                        "Overlay Failed to load",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction("Retry") {
                            loadOverlay(name, overlay)
                        }
                        .setActionTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.primary
                            )
                        )

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

                    val properties = HashMap<String, Any?>()
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
//            imgOverlay?.visibility = View.VISIBLE
            viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
            viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
            viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
            tvSkuName?.text = viewModel.sku?.skuName
//            binding.imgOverlay.visibility = View.VISIBLE
            if (viewModel.startInteriorShots.value == true || viewModel.startMiscShots.value == true) {
//                binding.imgOverlay.visibility = View.INVISIBLE
                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
            }


            if (viewModel.categoryDetails.value?.imageType == "Exterior") {
//                viewModel.showLeveler.value = true
                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
            }

        }

        if (getString(R.string.app_name) == AppConstants.KARVI) {
//            binding.imgOverlay.visibility = View.GONE
            viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
            viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
            viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
        }

        val intent = requireActivity().intent

        viewModel.isSubCategorySelected.value = true
        //  viewModel.shootList.value = ArrayList()

        when {
            intent.getBooleanExtra(AppConstants.RESUME_EXTERIOR, false) -> {
//                viewModel.showLeveler.value = true
                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive

                if (getString(R.string.app_name) != AppConstants.KARVI) {
//                    binding.imgOverlay.visibility = View.VISIBLE
                    viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                    viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                    viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
                }
                if (viewModel.startInteriorShots.value == true || viewModel.startMiscShots.value == true) {
//                    binding.imgOverlay.visibility = View.INVISIBLE
                    viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                    viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                    viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
                }
            }
            intent.getBooleanExtra(AppConstants.RESUME_INTERIOR, false) -> {
//                binding.imgOverlay.visibility = View.GONE
                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
            }
            intent.getBooleanExtra(AppConstants.RESUME_MISC, false) -> {
//                binding.imgOverlay.visibility = View.GONE
                viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
                viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
                viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
            }
            else -> {

            }

        }
    }

    private fun getOverlays() {
        Utilities.showProgressDialog(requireContext())

        viewModel.subCategory.value?.let {
            viewModel.getOverlays(
                Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
                requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID).toString(),
                it.prod_sub_cat_id!!,
                viewModel.exterirorAngles.value.toString()
            )

            requireContext().captureEvent(
                Events.GET_OVERLAYS_INTIATED,
                HashMap<String, Any?>()
                    .apply {
                        this.put("angles", viewModel.exterirorAngles.value)
                        this.put("prod_sub_cat_id", it.prod_sub_cat_id!!)
                    }
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

                    val selctedDraftList = DraftClickedImages.clickedImagesMap

                    val list = it.value.data
                    //set overlays
                    list.forEachIndexed { index, data ->
                        if (selctedDraftList.get(data.id.toString()) != null) {
                            list[index].imageClicked = true
                            list[index].imagePath = selctedDraftList.get(data.id.toString())!!
                        }
                    }

                    if (viewModel.shootList.value != null) {
                        list.forEach { overlay ->
                            val element = viewModel.shootList.value!!.firstOrNull {
                                it.overlayId == overlay.id
                            }

                            if (element != null) {
                                overlay.imageClicked = true
                                overlay.imagePath = element.capturedImage
                            }
                        }
                    }

                    val notSelected = list.firstOrNull {
                        !it.isSelected && !it.imageClicked
                    }

                    var index = -1
                    if (notSelected != null) {
                        index = list.indexOf(notSelected)

                        list[index].isSelected = true

                        viewModel.displayName = list[index].display_name
                        viewModel.displayThumbanil = list[index].display_thumbnail

                    }

                    val s = ""

                    overlaysAdapter = OverlaysAdapter(
                        list,
                        this@DraftShootFragment,
                        this@DraftShootFragment
                    )


                    binding.rvSubcategories.apply {
                        visibility = View.VISIBLE
                        layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL, false
                        )
                        adapter = overlaysAdapter
                    }

                    if (index != -1) {
                        binding.rvSubcategories.scrollToPosition(index)
                    }

                    requireContext().captureEvent(
                        Events.GET_OVERLAYS,
                        HashMap<String, Any?>()
                            .apply {
                                this.put("angles", it.value.data.size)
                            }

                    )

                    showViews()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()

                    requireContext().captureFailureEvent(
                        Events.GET_OVERLAYS_FAILED, HashMap<String, Any?>(),
                        it.errorMessage!!
                    )
                    shoot("show progress dialog(overlays response failure)")
                    handleApiError(it) { getOverlays() }
                }
            }
        })
    }

    private fun initInteriorShots() {
//        viewModel.hideLeveler.value = true
        viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
        viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
        viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive

        InteriorHintDialog().show(requireActivity().supportFragmentManager, "InteriorHintDialog")
    }

    private fun startInteriorShots() {
        viewModel.isCameraButtonClickable = true

        if (viewModel.subCategoriesResponse.value is Resource.Success){
            binding.apply {
            binding.llAngles.visibility = View.VISIBLE
            rvSubcategories?.visibility = View.VISIBLE
            tvShoot.isClickable = false
        }

        val subCatResponse = (viewModel.subCategoriesResponse.value as Resource.Success).value

        val list = subCatResponse.interior as ArrayList<NewSubCatResponse.Interior>

        viewModel.interiorAngles.value = list.size

        val selctedDraftList = DraftClickedImages.clickedImagesMap

        //set overlays
        list.forEachIndexed { index, data ->
            if (selctedDraftList[data.overlayId.toString()] != null) {
                list[index].imageClicked = true
                list[index].imagePath = selctedDraftList[data.overlayId.toString()]!!
            }
        }

        if (viewModel.shootList.value != null) {
            list.forEach { overlay ->
                val element = viewModel.shootList.value!!.firstOrNull {
                    it.overlayId == overlay.overlayId
                }

                if (element != null) {
                    overlay.imageClicked = true
                    overlay.imagePath = element.capturedImage
                }
            }
        }

        val notSelected = list.firstOrNull {
            !it.isSelected && !it.imageClicked
        }

        var index = -1
        if (notSelected != null) {
            index = list.indexOf(notSelected)

            list[index].isSelected = true

            viewModel.displayName = list[index].display_name
            viewModel.displayThumbanil = list[index].display_thumbnail

        }

        val s = ""

        overlaysAdapter = OverlaysAdapter(
            list,
            this@DraftShootFragment,
            this@DraftShootFragment
        )

        binding.rvSubcategories.apply {
            visibility = View.VISIBLE
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL, false
            )
            adapter = overlaysAdapter
        }

        if (index != -1) {
            binding.rvSubcategories.scrollToPosition(index)
        }

        //change image type
        viewModel.categoryDetails.value?.imageType = "Interior"
        }

    }

    private fun observerMiscShots() {
        viewModel.showMiscDialog.observe(viewLifecycleOwner, {
            if (it) {
//                binding.imgOverlay?.visibility = View.GONE
                initMiscShots()
            }
        })
    }

    private fun initMiscShots() {
//        viewModel.hideLeveler.value = true
        viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
        viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
        viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive

        if (viewModel.startMiscShots.value == null && !viewModel.miscDialogShowed) {
            MiscShotsDialog().show(requireActivity().supportFragmentManager, "MiscShotsDialog")
            viewModel.miscDialogShowed = true
        }
    }

    private fun startMiscShots() {
        viewModel.isCameraButtonClickable = true

       if (viewModel.subCategoriesResponse.value is Resource.Success){
           binding.apply {
               binding.llAngles.visibility = View.VISIBLE
               rvSubcategories?.visibility = View.VISIBLE
           }


           val subCatResponse = (viewModel.subCategoriesResponse.value as Resource.Success).value

           viewModel.miscAngles.value = subCatResponse.miscellaneous.size
           binding.rvSubcategories.scrollToPosition(0)


           if (viewModel.categoryDetails.value?.categoryId == AppConstants.BIKES_CATEGORY_ID) {
               val filteredList: List<NewSubCatResponse.Miscellaneous> =
                   subCatResponse.miscellaneous.filter {
                       it.prod_sub_cat_id == viewModel.subCategory.value?.prod_sub_cat_id
                   }

               subCatResponse.miscellaneous = filteredList
               viewModel.miscAngles.value = subCatResponse.miscellaneous.size
           }

           val selctedDraftList = DraftClickedImages.clickedImagesMap
           val list = subCatResponse.miscellaneous
           //set overlays
           list.forEachIndexed { index, data ->
               if (selctedDraftList.get(data.overlayId.toString()) != null) {
                   list[index].imageClicked = true
                   list[index].imagePath = selctedDraftList.get(data.overlayId.toString())!!
               }
           }

           if (viewModel.shootList.value != null) {
               list.forEach { overlay ->
                   val element = viewModel.shootList.value!!.firstOrNull {
                       it.overlayId == overlay.overlayId
                   }

                   if (element != null) {
                       overlay.imageClicked = true
                       overlay.imagePath = element.capturedImage
                   }
               }
           }

           val notSelected = list.firstOrNull {
               !it.isSelected && !it.imageClicked
           }

           var index = -1
           if (notSelected != null) {
               index = list.indexOf(notSelected)

               list[index].isSelected = true

               viewModel.displayName = list[index].display_name
               viewModel.displayThumbanil = list[index].display_thumbnail

           }

           overlaysAdapter = OverlaysAdapter(
               list,
               this@DraftShootFragment,
               this@DraftShootFragment
           )

           val s = ""

           binding.rvSubcategories.apply {
               visibility = View.VISIBLE
               layoutManager = LinearLayoutManager(
                   requireContext(),
                   LinearLayoutManager.VERTICAL, false
               )
               adapter = overlaysAdapter
           }

           if (index != -1) {
               binding.rvSubcategories.scrollToPosition(index)
           }

           //change image type
//           viewModel.hideLeveler.value = true
//           viewModel.showOverlay.value = false
           viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
           viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
           viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
           viewModel.categoryDetails.value?.imageType = "Focus Shoot"
       }
    }


    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData

        when (getString(R.string.app_name)) {
            AppConstants.OLA_CABS,
            AppConstants.CARS24,
            AppConstants.CARS24_INDIA -> {
                ConfirmTagsDialog().show(
                    requireActivity().supportFragmentManager,
                    "ConfirmTagsDialog"
                )
            }
            else -> {
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

    override fun onDestroy() {
        super.onDestroy()
        shoot("onDestroy called(overlay fragment)")
        Utilities.hideProgressDialog()
        viewModel.showConfirmReshootDialog.value = false
    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when(data){
            is OverlaysResponse.Overlays->{
                if (data.imageClicked){
                    showReclickDialog(
                        data.id,
                        position,
                        "Exterior")
                }else {
                    viewModel.overlayId = data.id

                    val list = overlaysAdapter?.listItems as List<OverlaysResponse.Overlays>

                    val element = list.firstOrNull {
                        it.isSelected
                    }

                    if (element != null && data != element){
                        data.isSelected = true
                        element.isSelected = false
                        overlaysAdapter?.notifyItemChanged(position)
                        overlaysAdapter?.notifyItemChanged(list.indexOf(element))
                        binding.rvSubcategories.scrollToPosition(position)
                    }
                }
            }

            is NewSubCatResponse.Interior ->{
                if (data.imageClicked){
                    showReclickDialog(
                        data.overlayId,
                        position,
                        "Interior")
                }else {
                    viewModel.overlayId = data.overlayId

                    val list = overlaysAdapter?.listItems as List<NewSubCatResponse.Interior>

                    val element = list.firstOrNull {
                        it.isSelected
                    }

                    if (element != null && data != element){
                        data.isSelected = true
                        element.isSelected = false
                        overlaysAdapter?.notifyItemChanged(position)
                        overlaysAdapter?.notifyItemChanged(list.indexOf(element))
                        binding.rvSubcategories.scrollToPosition(position)
                    }
                }
            }

            is NewSubCatResponse.Miscellaneous ->{
                if (data.imageClicked){
                    showReclickDialog(
                        data.overlayId,
                        position,
                        "Focus Shoot")
                }else {
                    viewModel.overlayId = data.overlayId

                    val list = overlaysAdapter?.listItems as List<NewSubCatResponse.Miscellaneous>

                    val element = list.firstOrNull {
                        it.isSelected
                    }

                    if (element != null && data != element){
                        data.isSelected = true
                        element.isSelected = false
                        overlaysAdapter?.notifyItemChanged(position)
                        overlaysAdapter?.notifyItemChanged(list.indexOf(element))
                        binding.rvSubcategories.scrollToPosition(position)
                    }
                }


            }
        }
    }

    override fun onOverlaySelected(view: View, position: Int, data: Any?) {
        viewModel.currentShoot = position

        when (data) {
            is OverlaysResponse.Overlays -> {
                viewModel.displayName = data.display_name
                viewModel.displayThumbanil = data.display_thumbnail

                loadOverlay(data.angle_name, data.display_thumbnail)
                viewModel.overlayId = data.id

                binding.tvShoot?.text = getString(R.string.angles)+" ${position.plus(1)}/${
                    viewModel.getSelectedAngles(
                        getString(
                            R.string.app_name
                        )
                    )
                }"
            }

            is NewSubCatResponse.Interior -> {
                viewModel.displayName = data.display_name
                viewModel.displayThumbanil = data.display_thumbnail
                viewModel.overlayId = data.overlayId

                binding.tvShoot?.text =
                    getString(R.string.angles)+" ${position.plus(1)}/${viewModel.interiorAngles.value}"
            }

            is NewSubCatResponse.Miscellaneous -> {
                viewModel.displayName = data.display_name
                viewModel.displayThumbanil = data.display_thumbnail
                viewModel.overlayId = data.overlayId

                binding.tvShoot?.text = getString(R.string.angles)+" ${position.plus(1)}/${viewModel.miscAngles.value}"
            }
        }
    }

    private fun showReclickDialog(overlayId: Int,position: Int,type: String) {
        val bundle = Bundle()
        bundle.putInt("overlay_id",overlayId)
        bundle.putInt("position",position)
        bundle.putString("image_type",type)
        val reclickDialog = ReclickDialog()
        reclickDialog.arguments = bundle
        reclickDialog.show(requireActivity().supportFragmentManager,"ReclickDialog")
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOverlaysV2Binding.inflate(inflater, container, false)
}