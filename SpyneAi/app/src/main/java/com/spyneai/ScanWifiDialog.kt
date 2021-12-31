package com.spyneai

import android.app.Activity
import android.app.Dialog
import android.net.wifi.ScanResult
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.FragmentScanWifiDialogBinding
import com.spyneai.shoot.data.ShootViewModel
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener
import android.content.Intent

import android.content.ComponentName
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.service.Actions
import com.spyneai.service.ImageUploadingService
import com.spyneai.service.getServiceState
import com.spyneai.service.log
import com.spyneai.shoot.data.model.ShootData
import com.theta360.sample.v2.ImageListActivity
import kotlinx.coroutines.launch


class ScanWifiDialog : BaseDialogFragment<ShootViewModel, FragmentScanWifiDialogBinding>() {
    private val SECOND_ACTIVITY_REQUEST_CODE = 2
    var imageName =""
    var sequenceNumber: Int? = null





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCancelable(false)
        imageName= viewModel.sku.value?.skuName!!+ "_"+ viewModel.sku.value?.skuId!!+"_360int_1.JPG"
        WifiUtils.withContext(requireContext()).enableWifi()



        binding.tvWifiSetting.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val cn = ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings")
            intent.component = cn
            startActivityForResult(intent,0)
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            val wm = context?.applicationContext?.getSystemService(WIFI_SERVICE) as WifiManager
            val ip: String = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
            val gatewayInfo: String = Formatter.formatIpAddress(wm.dhcpInfo.gateway)
            if (ip == "192.168.1.5" && gatewayInfo == "192.168.1.1") {
                val color: Int? = context?.let { ContextCompat.getColor(it, R.color.primary) }
                val hexColor = java.lang.String.format("#%06X", 0xFFFFFF and color!!)
                Toast.makeText(context, "Connected With Ricoh Camera", Toast.LENGTH_SHORT).show()
                var intent = Intent(context, ImageListActivity::class.java)
                intent.putExtra("file_name", imageName)
                intent.putExtra("primary_color", hexColor)
                startActivityForResult(intent,SECOND_ACTIVITY_REQUEST_CODE)
            } else {
                Toast.makeText(
                    context, "You are Not connected with Ricoh Theta Camera", Toast.LENGTH_LONG
                ).show()
            }
        }
        if (requestCode == SECOND_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
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
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.insertImage(
                        ShootData(
                        "/storage/emulated/0/DCIM/Spyne/"+viewModel.sku.value?.skuName!!+ "_"+
                                viewModel.sku.value?.skuId!!+"_360int_1.JPG",
                        viewModel.sku.value?.projectId!!,
                        viewModel.sku.value?.skuId!!,
                        "360int",
                        Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
                        0,
                        sequenceNumber!!
                    )
                    )
                }
                startService()
                dismiss()
                viewModel.selectBackground.value = true
            }
        }


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
    override fun onResume() {
        super.onResume()

        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentScanWifiDialogBinding.inflate(inflater, container, false)
}