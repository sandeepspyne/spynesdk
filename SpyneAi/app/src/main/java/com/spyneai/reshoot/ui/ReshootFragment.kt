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
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
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
import org.json.JSONArray

class ReshootFragment : BaseFragment<ShootViewModel, FragmentReshootBinding>(), OnItemClickListener,
    OnOverlaySelectionListener {

    var reshootAdapter: ReshootAdapter? = null
    var snackbar: Snackbar? = null
    val TAG = "ReshootFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (viewModel.categoryDetails.value?.categoryName) {
            "E-Commerce" -> {
                setReshootData()
            }
            else -> {
                getOverlayIds()
                observerOverlayIds()
            }
        }


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


                when (viewModel.categoryDetails.value?.categoryName) {
                    "E-Commerce" -> {
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

        viewModel.isCameraButtonClickable = true

    }

    private fun setReshootData() {
        val list = SelectedImagesHelper.selectedImages
        var index = 0


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
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = reshootAdapter
            scrollToPosition(index)
        }
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
        viewModel.reshootOverlaysRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    val list = it.value.data
                    var index = 0

                    list.forEach {
                        it.imageName = SelectedImagesHelper.selectedOverlayIds.get(it.id).toString()
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
            else -> {
                ConfirmReshootDialog().show(
                    requireActivity().supportFragmentManager,
                    "ConfirmReshootDialog"
                )
            }
        }


    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when (data) {
            is ReshootOverlaysRes.Data -> {

                if (data.imageClicked) {
                    ReclickDialog().show(requireActivity().supportFragmentManager, "ReclickDialog")
                }
                val list = reshootAdapter?.listItems as List<ReshootOverlaysRes.Data>

                val element = list.firstOrNull {
                    it.isSelected
                }

                if (element != null && data != element) {
                    //loadOverlay(data.angle_name,data.display_thumbnail)
                    //viewModel.selectedOverlay = data

                    data.isSelected = true
                    element.isSelected = false
                    reshootAdapter?.notifyItemChanged(position)
                    reshootAdapter?.notifyItemChanged(list.indexOf(element))
                    binding.rvImages.scrollToPosition(position)
                }
            }

            is ImagesOfSkuRes.Data -> {
                if (data.imageClicked) {
                    ReclickDialog().show(requireActivity().supportFragmentManager, "ReclickDialog")
                }
                val list = reshootAdapter?.listItems as List<ImagesOfSkuRes.Data>

                val element = list.firstOrNull {
                    it.isSelected
                }

                if (element != null && data != element) {
                    //loadOverlay(data.angle_name,data.display_thumbnail)
                    //viewModel.selectedOverlay = data

                    data.isSelected = true
                    element.isSelected = false
                    reshootAdapter?.notifyItemChanged(position)
                    reshootAdapter?.notifyItemChanged(list.indexOf(element))
                    binding.rvImages.scrollToPosition(position)
                }
            }
        }
    }

    override fun onOverlaySelected(view: View, position: Int, data: Any?) {
        viewModel.currentShoot = position

        when (data) {
            is ReshootOverlaysRes.Data -> {
                viewModel.reshotImageName = data.imageName

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

                viewModel.categoryDetails.value?.imageType = data.type

                viewModel.overlayId = data.id

                binding.tvShoot?.text =
                    "Angles ${position.plus(1)}/${SelectedImagesHelper.selectedOverlayIds.size}"
            }
            is ImagesOfSkuRes.Data -> {
                viewModel.reshotImageName = data.image_name

                viewModel.showLeveler.value = true

                viewModel.categoryDetails.value?.imageType = data.image_category

                viewModel.overlayId = data.overlayId

                binding.tvShoot?.text =
                    "Angles ${position.plus(1)}/${SelectedImagesHelper.selectedImages.size}"
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
                    val properties = Properties()
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

                    val properties = Properties()
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

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentReshootBinding.inflate(inflater, container, false)

}