package com.spyneai.reshoot.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
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
import com.spyneai.dashboard.response.NewSubCatResponse
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentReshootBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.posthog.Events
import com.spyneai.reshoot.ReshootAdapter
import com.spyneai.reshoot.data.ReshootOverlaysRes
import com.spyneai.reshoot.data.SelectedImagesHelper
import com.spyneai.shoot.data.OnOverlaySelectionListener
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.dialogs.ConfirmReshootDialog
import com.spyneai.shoot.ui.dialogs.ConfirmTagsDialog
import com.spyneai.shoot.ui.dialogs.ReclickDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ConfirmReshootEcomDialog
import io.sentry.protocol.App
import org.json.JSONArray

class ReshootFragment : BaseFragment<ShootViewModel, FragmentReshootBinding>(), OnItemClickListener,
    OnOverlaySelectionListener {

    var reshootAdapter: ReshootAdapter? = null
    var snackbar: Snackbar? = null
    val TAG = "ReshootFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getOverlayIds()
        observerOverlayIds()

        binding.apply {
            tvSkuName.text = viewModel.sku.value?.skuName

        }
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

        viewModel.onImageConfirmed.observe(viewLifecycleOwner, {
            if (viewModel.shootList.value != null) {


                when (viewModel.categoryDetails.value?.categoryId) {
                    AppConstants.ECOM_CATEGORY_ID,
                    AppConstants.PHOTO_BOX_CATEGORY_ID,
                    AppConstants.FOOD_AND_BEV_CATEGORY_ID-> {
                        var list = reshootAdapter?.listItems as List<ImagesOfSkuRes.Data>

                        val position = viewModel.currentShoot

                        list[position].isSelected = false
                        list[position].imageClicked = true
                        list[position].imagePath = viewModel.getCurrentShoot()!!.capturedImage
                        reshootAdapter?.notifyItemChanged(position)

                        if (position != list.size.minus(1)) {
                            var foundNext = false

                            for (i in position..list.size.minus(1)) {
                                if (!list[i].isSelected && !list[i].imageClicked) {
                                    foundNext = true
                                    list[i].isSelected = true
                                    reshootAdapter?.notifyItemChanged(i)
                                    binding.rvImages.scrollToPosition(i.plus(2))
                                    break
                                }
                            }

                            if (!foundNext) {
                                val element = list.firstOrNull {
                                    !it.isSelected && !it.imageClicked
                                }

                                if (element != null) {
                                    element?.isSelected = true
                                    reshootAdapter?.notifyItemChanged(list.indexOf(element))
                                    binding.rvImages.scrollToPosition(viewModel.currentShoot)
                                }
                            }
                        } else {
                            val element = list.firstOrNull {
                                !it.isSelected && !it.imageClicked
                            }

                            if (element != null) {
                                element?.isSelected = true
                                reshootAdapter?.notifyItemChanged(list.indexOf(element))
                                binding.rvImages.scrollToPosition(viewModel.currentShoot)
                            }
                        }


                        viewModel.allReshootClicked = list.all { it.imageClicked }
                    }
                    else -> {
                        var list = reshootAdapter?.listItems as List<ReshootOverlaysRes.Data>

                        val position = viewModel.currentShoot

                        list[position].isSelected = false
                        list[position].imageClicked = true
                        list[position].imagePath = viewModel.getCurrentShoot()!!.capturedImage
                        reshootAdapter?.notifyItemChanged(position)

                        if (position != list.size.minus(1)) {
                            var foundNext = false

                            for (i in position..list.size.minus(1)) {
                                if (!list[i].isSelected && !list[i].imageClicked) {
                                    foundNext = true
                                    list[i].isSelected = true
                                    reshootAdapter?.notifyItemChanged(i)
                                    binding.rvImages.scrollToPosition(i.plus(2))
                                    break
                                }
                            }

                            if (!foundNext) {
                                val element = list.firstOrNull {
                                    !it.isSelected && !it.imageClicked
                                }

                                if (element != null) {
                                    element?.isSelected = true
                                    reshootAdapter?.notifyItemChanged(list.indexOf(element))
                                    binding.rvImages.scrollToPosition(viewModel.currentShoot)
                                }
                            }
                        } else {
                            val element = list.firstOrNull {
                                !it.isSelected && !it.imageClicked
                            }

                            if (element != null) {
                                element?.isSelected = true
                                reshootAdapter?.notifyItemChanged(list.indexOf(element))
                                binding.rvImages.scrollToPosition(viewModel.currentShoot)
                            }
                        }


                        viewModel.allReshootClicked = list.all { it.imageClicked }
                    }
                }
            }
        })

        viewModel.updateSelectItem.observe(viewLifecycleOwner,{
            if (it){
                when(viewModel.categoryDetails.value?.imageType){
                    "Exterior" -> {
                        val list = reshootAdapter?.listItems as List<OverlaysResponse.Data>

                        val element = list.firstOrNull {
                            it.isSelected
                        }

                        val data = list[viewModel.currentShoot]

                        viewModel.overlayId = data.id

                        if (element != null && data != element){
                            data.isSelected = true
                            element.isSelected = false
                            reshootAdapter?.notifyItemChanged(viewModel.currentShoot)
                            reshootAdapter?.notifyItemChanged(list.indexOf(element))
                            binding.rvImages.scrollToPosition(viewModel.currentShoot)
                        }
                    }

                    "Interior" -> {
                        val list = reshootAdapter?.listItems as List<NewSubCatResponse.Interior>

                        val element = list.firstOrNull {
                            it.isSelected
                        }

                        val data = list[viewModel.currentShoot]
                        viewModel.overlayId = data.overlayId

                        if (element != null && data != element){
                            data.isSelected = true
                            element.isSelected = false
                            reshootAdapter?.notifyItemChanged(viewModel.currentShoot)
                            reshootAdapter?.notifyItemChanged(list.indexOf(element))
                            binding.rvImages.scrollToPosition(viewModel.currentShoot)
                        }
                    }

                    "Focus Shoot" -> {
                        val list = reshootAdapter?.listItems as List<NewSubCatResponse.Miscellaneous>

                        val element = list.firstOrNull {
                            it.isSelected
                        }

                        val data = list[viewModel.currentShoot]
                        viewModel.overlayId = data.overlayId

                        if (element != null && data != element){
                            data.isSelected = true
                            element.isSelected = false
                            reshootAdapter?.notifyItemChanged(viewModel.currentShoot)
                            reshootAdapter?.notifyItemChanged(list.indexOf(element))
                            binding.rvImages.scrollToPosition(viewModel.currentShoot)
                        }
                    }
                }

            }
        })

        viewModel.isCameraButtonClickable = true

        viewModel.notifyItemChanged.observe(viewLifecycleOwner,{
            reshootAdapter?.notifyItemChanged(it)
        })

        viewModel.scrollView.observe(viewLifecycleOwner,{
            binding.rvImages.scrollToPosition(it)
        })

    }


    private fun getOverlayIds() {
        Utilities.showProgressDialog(requireContext())

        val ids = JSONArray()

        SelectedImagesHelper.selectedOverlayIds.keys.forEach {
            ids.put(it)
        }
        viewModel.getOverlayIds(ids)
    }

    private fun observerOverlayIds() {
        viewModel.reshootOverlaysRes.observe(viewLifecycleOwner, { it ->
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    val list = it.value.data
                    var index = 0

                    list.forEach {
                        it.imageName = SelectedImagesHelper.selectedOverlayIds[it.id]!!.imageName
                        it.sequenceNumber = SelectedImagesHelper.selectedOverlayIds[it.id]!!.sequenceNumber
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

                        val element = list.first {
                            !it.isSelected && !it.imageClicked
                        }

                        element.isSelected = true
                        index = list.indexOf(element)

                    } else {
                        //set overlays
                        list[index].isSelected = true
                    }

                    //set recycler view
                    reshootAdapter = ReshootAdapter(
                        list,
                        this,
                        this
                    )

                    binding.rvImages.apply {
                        layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        adapter = reshootAdapter
                        scrollToPosition(index)
                    }
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { getOverlayIds() }
                }
            }
        })
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
            AppConstants.SWIGGY->{
                ConfirmReshootEcomDialog().show(
                    requireActivity().supportFragmentManager,
                    "ConfirmReshootEcomDialog"
                )
            }
            AppConstants.SPYNE_AI->{
                when(viewModel.categoryDetails.value?.categoryId){
                    AppConstants.FOOD_AND_BEV_CATEGORY_ID->{
                        ConfirmReshootEcomDialog().show(
                            requireActivity().supportFragmentManager,
                            "ConfirmReshootEcomDialog"
                        )
                    }else->{
                    ConfirmReshootDialog().show(
                        requireActivity().supportFragmentManager,
                        "ConfirmReshootDialog"
                    )
                    }
                }
            }
            else -> {
                ConfirmReshootDialog().show(
                    requireActivity().supportFragmentManager,
                    "ConfirmReshootDialog"
                )
            }
        }


    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when(data){
            is OverlaysResponse.Data->{
                if (data.imageClicked){
                    showReclickDialog(
                        data.id,
                        position,
                        "Exterior")
                }else {
                    viewModel.overlayId = data.id

                    val list = reshootAdapter?.listItems as List<OverlaysResponse.Data>

                    val element = list.firstOrNull {
                        it.isSelected
                    }

                    if (element != null && data != element){
                        data.isSelected = true
                        element.isSelected = false
                        reshootAdapter?.notifyItemChanged(position)
                        reshootAdapter?.notifyItemChanged(list.indexOf(element))
                        binding.rvImages.scrollToPosition(position)
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

                    val list = reshootAdapter?.listItems as List<NewSubCatResponse.Interior>

                    val element = list.firstOrNull {
                        it.isSelected
                    }

                    if (element != null && data != element){
                        data.isSelected = true
                        element.isSelected = false
                        reshootAdapter?.notifyItemChanged(position)
                        reshootAdapter?.notifyItemChanged(list.indexOf(element))
                        binding.rvImages.scrollToPosition(position)
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

                    val list = reshootAdapter?.listItems as List<NewSubCatResponse.Miscellaneous>

                    val element = list.firstOrNull {
                        it.isSelected
                    }

                    if (element != null && data != element){
                        data.isSelected = true
                        element.isSelected = false
                        reshootAdapter?.notifyItemChanged(position)
                        reshootAdapter?.notifyItemChanged(list.indexOf(element))
                        binding.rvImages.scrollToPosition(position)
                    }
                }


            }
        }
    }

    override fun onOverlaySelected(view: View, position: Int, data: Any?) {
        viewModel.currentShoot = position

        when (data) {
            is ReshootOverlaysRes.Data -> {
                viewModel.reshotImageName = data.imageName
                viewModel.reshootSequence = data.sequenceNumber
                viewModel.overlayId = data.id

                if (data.type == "Exterior") {
                    viewModel.showLeveler.value = true
                    binding.imgOverlay?.visibility = View.VISIBLE
                    loadOverlay(data.displayName, data.displayThumbnail)
                } else {
                    viewModel.hideLeveler.value = true
                    binding.imgOverlay?.visibility = View.GONE
                }

                if (getString(R.string.app_name) == AppConstants.KARVI)
                    binding.imgOverlay?.visibility = View.GONE


                viewModel.categoryDetails.value?.imageType = if (data.type == "Misc") "Focus Shoot" else data.type

                binding.tvShoot?.text =
                    "Angles ${position.plus(1)}/${SelectedImagesHelper.selectedOverlayIds.size}"
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
                    val properties = HashMap<String,Any?>()
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

                    val properties = HashMap<String,Any?>()
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
    ) = FragmentReshootBinding.inflate(inflater, container, false)

}