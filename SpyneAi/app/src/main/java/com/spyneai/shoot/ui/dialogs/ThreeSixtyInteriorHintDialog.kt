package com.spyneai.shoot.ui.dialogs

import android.app.Activity
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.Dialog360InteriorBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.service.Actions
import com.spyneai.service.ImageUploadingService
import com.spyneai.service.getServiceState
import com.spyneai.service.log
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.theta360.sample.v2.ImageListActivity
import kotlinx.coroutines.launch
import android.app.Activity.RESULT_OK
import android.net.wifi.ScanResult
import android.util.Log
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.ScanWifiDialog
import com.spyneai.adapter.WifiAdapter
import com.spyneai.logout.LogoutDialog
import com.thanosfisherman.wifiutils.WifiUtils


import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode

import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener








class ThreeSixtyInteriorHintDialog : BaseDialogFragment<ShootViewModel, Dialog360InteriorBinding>(),
    PickiTCallbacks {
    private val SECOND_ACTIVITY_REQUEST_CODE = 0
    private var TAG = "ThreeSixtyInteriorHintDialog"

    var pickIt: PickiT? = null
    var filePath = ""
    var sequenceNumber: Int? = null
    var imageName =""

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

            tvShoot.text = "Reselect"
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

//        binding.tvSkipShoot.setOnClickListener {
//            if (binding.tvShoot.text.toString() == "Shoot 360") {
//               // viewModel.interior360Dialog.value = true
//                dismiss()
//                viewModel.selectBackground.value = true
//            }else {
//
//            }
//        }


        binding.tvShoot.setOnClickListener {
            if (binding.tvShoot.text.toString() == "Shoot 360") {
                imageName= viewModel.sku.value?.skuName!!+ "_"+ viewModel.sku.value?.skuId!!+"_360int_1.JPG"
//                WifiUtils.withContext(requireContext()).enableWifi()
                WifiUtils.withContext(requireContext()).scanWifi(this::getScanResults).start()
//                WifiUtils.withContext(requireContext())
//                    .connectWith("THETAYP00110544.OSC")
//                    .setTimeout(40000)
//                    .onConnectionResult(object : ConnectionSuccessListener {
//                        override fun success() {
//                            Toast.makeText(requireContext(), "SUCCESSFULLY CONNECTED TO RICOH THETA CAMERA", Toast.LENGTH_SHORT).show()
//                        }
//
//                        override fun failed(errorCode: ConnectionErrorCode) {
//                            Toast.makeText(
//                                requireContext(),
//                                "EPIC FAIL!$errorCode",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    })
//                    .start()

                val wm = requireContext().applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                val ip: String = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
                val gatewayInfo: String = Formatter.formatIpAddress(wm.dhcpInfo.gateway)

            if(ip=="192.168.1.5" && gatewayInfo=="192.168.1.1") {
                val color:Int= ContextCompat.getColor(requireContext(), R.color.primary)
                val hexColor = java.lang.String.format("#%06X", 0xFFFFFF and color)
                Toast.makeText(requireContext(), "Connected With Camera", Toast.LENGTH_SHORT).show()
                var intent = Intent(requireContext(), ImageListActivity::class.java)
                intent.putExtra("file_name",imageName)
                intent.putExtra("primary_color",hexColor)
                startActivityForResult(intent,SECOND_ACTIVITY_REQUEST_CODE)
            }
            else{
                ScanWifiDialog().show(requireActivity().supportFragmentManager, "ScanWifiDialog")
            }
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

                 sequenceNumber = if (viewModel.fromDrafts){
                    if (viewModel.shootList.value != null){
                        requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0)
                            .plus(requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE,0))
                            .plus(requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE,0))
                            .plus(viewModel.shootList.value!!.size.plus(1))

                    }else{
                        requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0)
                            .plus(requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE,0))
                            .plus(requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE,0))
                            .plus(1)
                    }

                }else {
                    viewModel.shootList.value?.size?.plus(1)
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.
                    insertImage(ShootData(
                        filePath,
                        viewModel.sku.value?.projectId!!,
                        viewModel.sku.value?.skuId!!,
                        "360int",
                        Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
                        0,
                        sequenceNumber!!
                    ))
                }

                startService()

                dismiss()

                viewModel.selectBackground.value = true
            }
        }
    }


    private fun getScanResults(results: List<ScanResult>) {
        if (results.isEmpty()) {
//            adapter = WifiAdapter(results)
            return
        }
//        adapter = WifiAdapter(results)
    }


    private fun startService() {
        var action = Actions.START
        if (getServiceState(requireContext()) == com.spyneai.service.ServiceState.STOPPED && action == Actions.STOP)
            return

        val serviceIntent = Intent(requireContext(), ImageUploadingService::class.java)
        serviceIntent.action = action.name

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            log("Starting the service in >=26 Mode")
            ContextCompat.startForegroundService(requireContext(), serviceIntent)
            return
        } else {
            log("Starting the service in < 26 Mode")
            requireActivity().startService(serviceIntent)
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check that it is the SecondActivity with an OK result
        if (requestCode == SECOND_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                WifiUtils.withContext(requireContext()).disableWifi();
                // Get String data from Intent
                val is360Clicked: String? = data?.getStringExtra("button_confirm")
                Toast.makeText(requireContext(),is360Clicked,Toast.LENGTH_LONG).show()
                sequenceNumber = if (viewModel.fromDrafts){
                    if (viewModel.shootList.value != null){
                        requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0)
                            .plus(requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE,0))
                            .plus(requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE,0))
                            .plus(viewModel.shootList.value!!.size.plus(1))

                    }else{
                        requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_SIZE,0)
                            .plus(requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE,0))
                            .plus(requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE,0))
                            .plus(1)
                    }

                }else {
                    viewModel.shootList.value?.size?.plus(1)
                }
                viewModel.threeSixtyInteriorSelected = true
                viewModel.shootList.value?.size?.plus(1)
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.insertImage(ShootData(
                        "/storage/emulated/0/DCIM/Spyne/"+viewModel.sku.value?.skuName!!+ "_"+
                                viewModel.sku.value?.skuId!!+"_360int_1.JPG",
                        viewModel.sku.value?.projectId!!,
                        viewModel.sku.value?.skuId!!,
                        "360int",
                        Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
                        0,
                        sequenceNumber!!
                    ))
                }
                startService()
                dismiss()
                viewModel.selectBackground.value = true
            }
        }
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