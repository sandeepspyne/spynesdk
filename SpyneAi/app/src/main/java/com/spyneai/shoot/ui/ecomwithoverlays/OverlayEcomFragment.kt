package com.spyneai.shoot.ui.ecomwithoverlays

import android.graphics.drawable.Drawable
import android.os.Bundle
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
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentOverlayEcomBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.OverlaysAdapter
import com.spyneai.shoot.data.OnOverlaySelectionListener
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.dialogs.AngleSelectionDialog
import com.spyneai.shoot.ui.dialogs.ReclickDialog
import com.spyneai.startUploadingService
import kotlinx.android.synthetic.main.fragment_overlays.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class OverlayEcomFragment : BaseFragment<ShootViewModel, FragmentOverlayEcomBinding>(),
    OnOverlaySelectionListener, OnItemClickListener {


    var overlaysAdapter: OverlaysAdapter? = null
    var snackbar: Snackbar? = null
    //var position = 1
    var pos = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.showOverlay.observe(viewLifecycleOwner, {
            if (it) {
                binding.imgOverlay.visibility = View.VISIBLE
            }else binding.imgOverlay.visibility = View.INVISIBLE
        })

        viewModel.showGrid.observe(viewLifecycleOwner, {
            if (it) {
                binding.groupGridLines?.visibility= View.VISIBLE
            }else binding.groupGridLines?.visibility = View.INVISIBLE
        })

        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (viewModel.showConfirmReshootDialog.value == true && !it.isNullOrEmpty()) {
                    val element = viewModel.getCurrentShoot()
                    showImageConfirmDialog(element!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        viewModel.isSkuCreated.observe(viewLifecycleOwner, {
            getOverlays()
        })

        observeOverlays()

        viewModel.onImageConfirmed.observe(viewLifecycleOwner, {
            if (viewModel.shootList.value != null && overlaysAdapter != null) {
                viewModel.setSelectedItem(overlaysAdapter?.listItems!!)
            }

            try {
                val list = overlaysAdapter?.listItems as List<OverlaysResponse.Overlays>
                viewModel.allEcomOverlyasClicked = list.all {
                    it.imageClicked
                }

            } catch (e: Exception) {

            }
        })

        viewModel.updateSelectItem.observe(viewLifecycleOwner,{
            if (it){
                val list = overlaysAdapter?.listItems as List<OverlaysResponse.Overlays>

                val element = list.firstOrNull {
                    it.isSelected
                }

                val data = list[viewModel.currentShoot]

                if (element != null && data != element) {
                    data.isSelected = true
                    element.isSelected = false
                    overlaysAdapter?.notifyItemChanged(viewModel.currentShoot)
                    overlaysAdapter?.notifyItemChanged(list.indexOf(element))
                    binding.rvSubcategories.scrollToPosition(viewModel.currentShoot)

                    val s = ""
                }
            }
        })

        viewModel.notifyItemChanged.observe(viewLifecycleOwner, {
            overlaysAdapter?.notifyItemChanged(it)
        })

        viewModel.scrollView.observe(viewLifecycleOwner, {
            binding.rvSubcategories.scrollToPosition(it)
        })
    }

    private fun observeOverlays() {
        viewModel.overlaysResponse.observe(viewLifecycleOwner, { it ->
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    //set exterior angle value
                    viewModel.exterirorAngles.value = it.value.data.size
                    viewModel.sku?.apply {
                        initialFrames = it.value.data.size
                        totalFrames = it.value.data.size
                    }

                    //update exterior angles in local DB
                   GlobalScope.launch(Dispatchers.IO) {
                       viewModel.updateSkuExteriorAngles()

                       //start sync service
                       GlobalScope.launch(Dispatchers.Main) {
                           requireContext().startUploadingService(
                               OverlayEcomFragment::class.java.simpleName
                           )
                       }
                   }

                    viewModel.displayName = it.value.data[0].display_name
                    viewModel.displayThumbanil = it.value.data[0].display_thumbnail

                    requireContext().captureEvent(
                        Events.GET_OVERLAYS,
                        HashMap<String, Any?>()
                            .apply {
                                this["angles"] = it.value.data.size
                            }
                    )

                    if (viewModel.fromDrafts) {
                        binding.tvShoot?.text = "${
                            requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE, 0)
                                .plus(1)
                        }/${it.value.data.size}"
                    } else {
                        binding.tvShoot?.text = "1/${it.value.data.size}"

                    }

                    val overlaysList = it.value.data
                    var index = 0

                    if (viewModel.shootList.value != null) {
                        overlaysList.forEach { overlay ->
                            val element = viewModel.shootList.value!!.firstOrNull {
                                it.overlayId == overlay.id
                            }

                            if (element != null) {
                                overlay.imageClicked = true
                                overlay.imagePath = element.capturedImage
                            }
                        }

                        val element = overlaysList.firstOrNull {
                            !it.isSelected && !it.imageClicked
                        }

                        if (element != null) {
                            element.isSelected = true
                            viewModel.displayName = element.display_name
                            viewModel.displayThumbanil = element.display_thumbnail

                            index = overlaysList.indexOf(element)
                        }
                    } else {
                        //set overlays
                        overlaysList[0].isSelected = true
                        viewModel.displayName = it.value.data[0].display_name
                        viewModel.displayThumbanil = it.value.data[0].display_thumbnail
                    }


                    overlaysAdapter = OverlaysAdapter(
                        overlaysList,
                        this@OverlayEcomFragment,
                        this@OverlayEcomFragment
                    )

                    binding.rvSubcategories.apply {
                        visibility = View.VISIBLE
                        layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        adapter = overlaysAdapter
                    }

                    binding.rvSubcategories.scrollToPosition(index)

                    requireContext().captureEvent(
                        Events.GET_OVERLAYS,
                        HashMap<String, Any?>()
                            .apply {
                                this.put("angles", it.value.data.size)
                            }

                    )

                    showViews()
                }

                is Resource.Loading -> Utilities.showProgressDialog(requireContext())

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.GET_OVERLAYS_FAILED, HashMap<String, Any?>(),
                        it.errorMessage!!
                    )
                    Utilities.hideProgressDialog()
                    handleApiError(it) { getOverlays() }
                }
            }
        })
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


    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        ConfirmReshootPortraitDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
    }


    private fun getOverlays() {
        viewModel.subCategory.value?.let {
            viewModel.getOverlays(
                Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
                viewModel.categoryDetails.value?.categoryId!!,
                it.prod_sub_cat_id!!,
                viewModel.exterirorAngles.value.toString()
            )

            requireContext().captureEvent(
                Events.GET_OVERLAYS_INTIATED,
                HashMap<String, Any?>()
                    .apply {
                        this.put("angles", viewModel.exterirorAngles.value.toString())
                        this.put("prod_sub_cat_id", it.prod_sub_cat_id!!)
                    }
            )
        }
    }


    private fun showViews() {
        binding.apply {
            tvSkuName?.visibility = View.VISIBLE
            tvAngleName?.visibility = View.VISIBLE
            llProgress?.visibility = View.VISIBLE
//            imgOverlay?.visibility = View.VISIBLE
            tvSkuName?.text = viewModel.sku?.skuName
        }
        viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
        viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
        viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive

        if (viewModel.fromDrafts && viewModel.categoryDetails.value?.categoryId == AppConstants.FOOTWEAR_CATEGORY_ID){
//            viewModel.showLeveler.value = true
            viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
            viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
            viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
        }

    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOverlayEcomBinding.inflate(inflater, container, false)

    override fun onOverlaySelected(view: View, position: Int, data: Any?) {
        viewModel.currentShoot = position

        when (data) {
            is OverlaysResponse.Overlays -> {
                viewModel.displayName = data.display_name
                viewModel.displayThumbanil = data.display_thumbnail
                viewModel.overlayId = data.id

                if (getString(R.string.app_name) != AppConstants.KARVI)
                    loadOverlay(data.angle_name, data.display_thumbnail)


                binding.tvShoot?.text =
                    position.plus(1).toString() + "/" + viewModel.exterirorAngles.value.toString()

            }

        }
    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        viewModel.currentShoot = position

        when (data) {
            is OverlaysResponse.Overlays -> {
                if (data.imageClicked){
                    val bundle = Bundle()
                    bundle.putInt("overlay_id",data.id)
                    bundle.putInt("position",position)
                    bundle.putString("image_type",
                        viewModel.categoryDetails.value?.imageType)
                    val reclickDialog = ReclickDialog()
                    reclickDialog.arguments = bundle
                    reclickDialog.show(requireActivity().supportFragmentManager,"ReclickDialog")
                }else{
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

}