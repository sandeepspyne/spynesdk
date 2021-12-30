package com.spyneai

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
import com.spyneai.adapter.WifiAdapter
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.FragmentScanWifiDialogBinding
import com.spyneai.shoot.data.ShootViewModel
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener

class ScanWifiDialog : BaseDialogFragment<ShootViewModel, FragmentScanWifiDialogBinding>() {
    private var adapter: RecyclerView.Adapter<WifiAdapter.ViewHolder>? = null
    var imageName =""





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCancelable(false)
        imageName= viewModel.sku.value?.skuName!!+ "_"+ viewModel.sku.value?.skuId!!+"_360int_1.JPG"
        WifiUtils.withContext(requireContext()).enableWifi()
        WifiUtils.withContext(requireContext()).scanWifi(this::getScanResults).start()
        binding.rvWifi.layoutManager = LinearLayoutManager(activity)



        binding.tvRefresh.setOnClickListener {
            WifiUtils.withContext(requireContext()).scanWifi(this::getScanResults).start()
        }







//        WifiUtils.withContext(requireContext())
//            .connectWith("THETAYP00110544.OSC")
//            .setTimeout(40000)
//            .onConnectionResult(object : ConnectionSuccessListener {
//                override fun success() {
//                    Toast.makeText(requireContext(), "SUCCESSFULLY CONNECTED TO RICOH THETA CAMERA", Toast.LENGTH_SHORT).show()
//                }
//
//                override fun failed(errorCode: ConnectionErrorCode) {
//                    Toast.makeText(
//                        requireContext(),
//                        "EPIC FAIL!$errorCode",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            })
//            .start()


    }

    private fun getScanResults(results: List<ScanResult>) {
        if (results.isEmpty()) {
            adapter = WifiAdapter(results,imageName)
            binding.rvWifi.adapter=adapter
            return
        }
        adapter = WifiAdapter(results,imageName)
        binding.rvWifi.adapter=adapter
    }

    override fun onResume() {
        super.onResume()

        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentScanWifiDialogBinding.inflate(inflater, container, false)
}