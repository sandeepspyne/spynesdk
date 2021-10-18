package com.spyneai.reshoot.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.enable
import com.spyneai.databinding.FragmentSelectImagesBinding
import com.spyneai.needs.AppConstants
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.processedimages.ui.data.ProcessedViewModel
import com.spyneai.reshoot.SelectImageAdapter
import com.spyneai.reshoot.data.SelectedImagesHelper
import org.json.JSONArray

class SelectImagesFragment : BaseFragment<ProcessedViewModel,FragmentSelectImagesBinding>(),OnItemClickListener{

    private var selectImageAdapter : SelectImageAdapter? = null

    private val permissions = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }
    }

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            onPermissionGranted()
        } else {
            Toast.makeText(requireContext(), R.string.message_no_permissions, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getImages()

        binding.btnReshoot.setOnClickListener {
            if (allPermissionsGranted()) {
                onPermissionGranted()
            } else {
                permissionRequest.launch(permissions.toTypedArray())
            }
        }
    }

    protected fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    open fun onPermissionGranted() {
        val list = selectImageAdapter?.listItems as ArrayList<ImagesOfSkuRes.Data>

        val selectedList = list.filter {
            it.isSelected
        }

        val selectedIdsMap = HashMap<Int,String>()

        selectedList.forEachIndexed { index, data ->
            selectedIdsMap.put(data.overlayId,data.image_name)
        }

        SelectedImagesHelper.selectedImages = selectedIdsMap

        val reshootIntent = Intent(requireActivity(),ReshootActivity::class.java)
        reshootIntent.apply {
            putExtra(AppConstants.PROJECT_ID,viewModel.projectId)
            putExtra(AppConstants.SKU_ID,viewModel.skuId)
            putExtra(AppConstants.SKU_NAME,viewModel.skuName)
            putExtra(AppConstants.CATEGORY_ID,requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID))
            putExtra(AppConstants.CATEGORY_NAME,requireActivity().intent.getStringExtra(AppConstants.CATEGORY_NAME))
            putExtra(AppConstants.SUB_CAT_ID,requireActivity().intent.getStringExtra(AppConstants.SUB_CAT_ID))
            putExtra(AppConstants.EXTERIOR_ANGLES,requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0))
            startActivity(this)
        }
    }

    private fun getImages() {
        try {
            val imagesResponse = (viewModel.imagesOfSkuRes.value as Resource.Success).value

            selectImageAdapter = SelectImageAdapter(imagesResponse.data,this)

            binding.rvSkuImages.apply {
                layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
                adapter = selectImageAdapter
            }
        }catch (e : Exception){
            val s = ""
        }

    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when(data){
            is ImagesOfSkuRes.Data -> {
                data.isSelected = !data.isSelected
                selectImageAdapter?.notifyItemChanged(position)

                val list = selectImageAdapter?.listItems as ArrayList<ImagesOfSkuRes.Data>

                val selectedList = list.filter {
                    it.isSelected == true
                }

                if (selectedList.isNullOrEmpty()){
                    binding.btnReshoot.text = getString(R.string.no_reshoot)
                    binding.btnReshoot.enable(false)
                }else {
                    binding.btnReshoot.text = getString(R.string.no_reshoot)+" "+selectedList.size+" Angles"
                    binding.btnReshoot.enable(true)
                }

            }
        }
    }

    override fun getViewModel() = ProcessedViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectImagesBinding.inflate(inflater, container, false)


}