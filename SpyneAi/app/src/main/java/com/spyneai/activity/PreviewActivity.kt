package com.spyneai.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.adapter.*
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.channel.ChannelsResponse
import com.spyneai.model.order.MarketPlace
import com.spyneai.model.order.PlaceOrderResponse
import com.spyneai.model.order.Sku
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.activity_preview.*
import kotlinx.android.synthetic.main.activity_preview.rvChannels
import kotlinx.android.synthetic.main.activity_preview.rvSkus
import kotlinx.android.synthetic.main.activity_shoot_selection.*
import kotlinx.android.synthetic.main.fragment_channel.*
import kotlinx.android.synthetic.main.fragment_channel.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PreviewActivity : AppCompatActivity() {

    lateinit var channelList : List<MarketPlace>
    lateinit var channelAdapter: AddChannelsAdapter
    private lateinit var skuAdapter: SkusAdapter
    private lateinit var skuList: List<Sku>
    private lateinit var skuName : String

    private var amount : String = ""
    private var skuCount : String = ""
    private var channelCount : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setChannels()
        setSkus()
        fetchSkus()
        listeners()
    }

    private fun setChannels() {
        channelList = ArrayList<MarketPlace>()
        channelAdapter = AddChannelsAdapter(this, channelList as ArrayList<MarketPlace>,
                object : AddChannelsAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int) {
                        Log.e("position preview", position.toString())
                    }
                })
        val layoutManager: RecyclerView.LayoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvChannels.setLayoutManager(layoutManager)
        rvChannels.setAdapter(channelAdapter)
    }

    private fun setSkus() {
        skuList = ArrayList<Sku>()
        skuAdapter = SkusAdapter(this, skuList,
                object : SkusAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int) {
                        Log.e("position preview MAin", position.toString())
                    }
                })
        val layoutManager: RecyclerView.LayoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvSkus.setLayoutManager(layoutManager)
        rvSkus.setAdapter(skuAdapter)
        fetchSkus()
    }

    private fun fetchSkus() {
        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.getOrderList(
                Utilities.getPreference(this,AppConstants.tokenId),
                Utilities.getPreference(this,AppConstants.SHOOT_ID),
                Utilities.getPreference(this,AppConstants.SKU_ID))

        call?.enqueue(object : Callback<PlaceOrderResponse> {
            override fun onResponse(call: Call<PlaceOrderResponse>,
                                    response: Response<PlaceOrderResponse>) {
                if (response.isSuccessful){
                    if (response.body()?.payload!!.data.numberOfSkus > 0)
                    {
                        (skuList as ArrayList).clear()
                        (channelList as ArrayList).clear()
                        (skuList as ArrayList).addAll(response.body()?.payload!!.data.skus as ArrayList)
                        (channelList as ArrayList).addAll(response.body()?.payload!!.data.marketPlace as ArrayList)
                    }
                    skuName = response.body()?.payload!!.data.skus[response.body()?.payload!!.data.skus.size - 1].displayName
                    skuAdapter.notifyDataSetChanged()
                    channelAdapter.notifyDataSetChanged()

                    amount = response.body()!!.payload.data.shootAmount.toString()
                    skuCount = response.body()!!.payload.data.numberOfSkus.toString()
                    amount = response.body()!!.payload.data.marketPlace.size.toString()
                }
            }
            override fun onFailure(call: Call<PlaceOrderResponse>, t: Throwable) {
                Toast.makeText(this@PreviewActivity,
                        "Server not responding!!!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun listeners() {
        tvOrder.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra(AppConstants.AMOUNT,amount)
            intent.putExtra(AppConstants.CHANNEL_COUNT,channelCount)
            intent.putExtra(AppConstants.SKU_COUNT,skuCount)
            startActivity(intent)
        })
        ivBackPreview.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}