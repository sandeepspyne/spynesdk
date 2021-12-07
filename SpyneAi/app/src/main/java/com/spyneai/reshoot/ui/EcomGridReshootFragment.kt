package com.spyneai.reshoot.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.databinding.FragmentEcomGridReshootBinding
import com.spyneai.databinding.FragmentEcomOverlayReshootBinding
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.reshoot.ReshootAdapter
import com.spyneai.reshoot.data.ReshootOverlaysRes
import com.spyneai.reshoot.data.SelectedImagesHelper
import com.spyneai.shoot.adapters.ClickedAdapter
import com.spyneai.shoot.data.OnOverlaySelectionListener
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.dialogs.ReclickDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ConfirmReshootEcomDialog
import com.theartofdev.edmodo.cropper.CropImage
import java.io.File


class EcomGridReshootFragment : BaseFragment<ShootViewModel, FragmentEcomGridReshootBinding>(),
    OnItemClickListener,
    OnOverlaySelectionListener {

    var reshootAdapter: ReshootAdapter? = null

    val TAG = "ReshootFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setReshootData()

        binding.apply {
            tvSkuName.visibility = View.VISIBLE
            ivBackCompleted.visibility = View.VISIBLE
            tvSkuName.text = viewModel.sku.value?.skuName
        }

        binding.ivBackCompleted.setOnClickListener {
            requireActivity().onBackPressed()
        }

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

        //observe new image clicked
        viewModel.onImageConfirmed.observe(viewLifecycleOwner, {
            if (viewModel.shootList.value != null) {
                var list = reshootAdapter?.listItems as List<ImagesOfSkuRes.Data>

                val position = viewModel.currentShoot

                list[position].isSelected = false
                list[position].imageClicked = true
                //list[position].imagePath = viewModel.getCurrentShoot()!!.capturedImage
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
        })

        viewModel.onImageConfirmed.observe(viewLifecycleOwner,{
            if (viewModel.shootList.value != null) {
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

                val s =""
                viewModel.allReshootClicked = list.all { it.imageClicked }
            }
        })

        viewModel.updateSelectItem.observe(viewLifecycleOwner,{ it ->
            if (it){
                val list = reshootAdapter?.listItems as List<ImagesOfSkuRes.Data>

                val element = list.firstOrNull {
                    it.isSelected
                }
                val data = list[viewModel.currentShoot]

                if (element != null && data != element) {
                    data.isSelected = true
                    element.isSelected = false
                    reshootAdapter?.notifyItemChanged(viewModel.currentShoot)
                    reshootAdapter?.notifyItemChanged(list.indexOf(element))
                    binding.rvImages.scrollToPosition(viewModel.currentShoot)
                }
            }
        })

        viewModel.notifyItemChanged.observe(viewLifecycleOwner, {
            reshootAdapter?.notifyItemChanged(it)
        })

        viewModel.scrollView.observe(viewLifecycleOwner, {
            binding.rvImages.scrollToPosition(it)
        })

        viewModel.isCameraButtonClickable = true
    }

    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        when (viewModel.categoryDetails.value?.imageType) {
            "Info" -> {
                CropImage.activity(Uri.fromFile(File(shootData.capturedImage)))
                    .start(requireActivity())
            }
            else ->
                ConfirmReshootEcomDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
        }
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

        viewModel.showLeveler.value = true
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentEcomGridReshootBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
            when(data){
                is ImagesOfSkuRes.Data -> {
                    if (data.imageClicked){
                        val bundle = Bundle()
                        bundle.putInt("overlay_id",data.overlayId)
                        bundle.putInt("position",position)
                        bundle.putString("image_type",data.image_category)
                        val reclickDialog = ReclickDialog()
                        reclickDialog.arguments = bundle
                        reclickDialog.show(requireActivity().supportFragmentManager,"ReclickDialog")
                    }else {
                        val list = reshootAdapter?.listItems as List<ImagesOfSkuRes.Data>

                        val element = list.firstOrNull {
                            it.isSelected
                        }

                        if (element != null && data != element) {
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

       when(data){
           is ImagesOfSkuRes.Data -> {
               viewModel.reshotImageName = data.image_name
               viewModel.reshootSequence = data.frame_seq_no.toInt()
               viewModel.categoryDetails.value?.imageType = data.image_category
               viewModel.overlayId = data.overlayId
           }
       }
    }
}