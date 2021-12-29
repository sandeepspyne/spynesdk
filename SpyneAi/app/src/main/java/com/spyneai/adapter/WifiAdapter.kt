package com.spyneai.adapter

import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.BaseApplication.Companion.getContext
import com.spyneai.R
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener
import com.theta360.sample.v2.ImageListActivity

class WifiAdapter(private val wifiList: List<ScanResult>,private val fileName: String) : RecyclerView.Adapter<WifiAdapter.ViewHolder>() {

    private var context: Context? = null


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvWifi: TextView = itemView.findViewById(R.id.tvWifi)

        init {

            tvWifi.setOnClickListener {
//                var position: Int = adapterPosition
//                val context = itemView.context
//                val intent = Intent(context, DetailPertanyaan::class.java).apply {
//                    putExtra("NUMBER", position)
//                    putExtra("CODE", itemKode.text)
//                    putExtra("CATEGORY", itemKategori.text)
//                    putExtra("CONTENT", itemIsi.text)
//                }
//                context.startActivity(intent)
            }
        }
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wifi, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: WifiAdapter.ViewHolder, position: Int) {
        holder.tvWifi.text = wifiList?.get(position).SSID
        holder.tvWifi.setOnClickListener {
            if(holder.tvWifi.text=="THETAYP00110544.OSC"){
                WifiUtils.withContext(getContext())
                .connectWith("THETAYP00110544.OSC")
                .setTimeout(40000)
                .onConnectionResult(object : ConnectionSuccessListener {
                    override fun success() {
                        val wm = getContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        val ip: String = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
                        val gatewayInfo: String = Formatter.formatIpAddress(wm.dhcpInfo.gateway)
                        if(ip=="192.168.1.5" && gatewayInfo=="192.168.1.1") {
                            val color:Int= ContextCompat.getColor(getContext(), R.color.primary)
                            val hexColor = java.lang.String.format("#%06X", 0xFFFFFF and color)
                            Toast.makeText(getContext(), "Connected With Camera", Toast.LENGTH_SHORT).show()
                            var intent = Intent(getContext(), ImageListActivity::class.java)
                            intent.putExtra("file_name",fileName)
                            intent.putExtra("primary_color",hexColor)
                            getContext().startActivity(intent)
                            Toast.makeText(getContext(), "SUCCESSFULLY CONNECTED TO RICOH THETA CAMERA", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun failed(errorCode: ConnectionErrorCode) {
                        Toast.makeText(
                            getContext(),
                            "EPIC FAIL!$errorCode",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
                .start()
            }  else{
                Toast.makeText(getContext(), "This is not Ricoh Theta Wifi Network", Toast.LENGTH_SHORT).show()
            }


        }
    }

    override fun getItemCount(): Int {
        return wifiList?.size
    }





}