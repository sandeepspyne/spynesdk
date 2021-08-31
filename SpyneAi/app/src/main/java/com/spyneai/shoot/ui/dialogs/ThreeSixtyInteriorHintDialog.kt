package com.spyneai.shoot.ui.dialogs

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.Dialog360InteriorBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import kotlinx.coroutines.launch

class ThreeSixtyInteriorHintDialog : BaseDialogFragment<ShootViewModel, Dialog360InteriorBinding>(),
    PickiTCallbacks {

    var pickIt: PickiT? = null
    var filePath = ""

    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.data
            try {
                var file = data!!.toFile()
                filePath = file.path

                showImage(filePath)

            } catch (ex: IllegalArgumentException) {
                pickIt?.getPath(data, Build.VERSION.SDK_INT)
            }

        }
    }

    private fun showImage(filePath: String) {
        binding.apply {
            tvDescription.visibility = View.GONE
            ivSelectedImage.visibility = View.VISIBLE
            tvSkipShoot.visibility = View.VISIBLE

            tvSkip.text = "Reselect"
            tvUpload.text = "Confirm"
        }

        Glide.with(requireContext())
            .load(filePath)
            .into(binding.ivSelectedImage)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pickIt = PickiT(requireContext(), this, requireActivity())

        isCancelable = false

        binding.tvSkipShoot.setOnClickListener {
           // viewModel.interior360Dialog.value = true
            dismiss()
            viewModel.selectBackground.value = true
        }

        binding.tvSkip.setOnClickListener {
            if (binding.tvSkip.text.toString() == "Skip") {
               // viewModel.interior360Dialog.value = true
                dismiss()
                viewModel.selectBackground.value = true
            }else {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startForResult.launch(intent)
            }
        }

        binding.tvUpload.setOnClickListener {
            if (binding.tvUpload.text == "Select") {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startForResult.launch(intent)
            }else {
               // viewModel.interior360Dialog.value = true
                viewModel.threeSixtyInteriorSelected = true

                //add image for upload

                val sequenceNumber = viewModel.shootList.value?.size?.plus(1)
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.
                    insertImage(ShootData(
                        filePath,
                        viewModel.sku.value?.projectId!!,
                        viewModel.sku.value?.skuId!!,
                        "360int",
                        Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
                        sequenceNumber!!
                    ))
                }

                dismiss()

                viewModel.selectBackground.value = true
            }
        }
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = Dialog360InteriorBinding.inflate(inflater, container, false)

    override fun PickiTonUriReturned() {

    }

    override fun PickiTonStartListener() {

    }

    override fun PickiTonProgressUpdate(progress: Int) {

    }

    override fun PickiTonCompleteListener(
        path: String?,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        Reason: String?
    ) {
        filePath = path!!
        showImage(filePath)
    }
}