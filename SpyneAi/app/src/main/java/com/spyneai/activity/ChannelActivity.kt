package com.spyneai.activity

import UploadPhotoResponse
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.adapter.ChannelAdapter
import com.spyneai.adapter.MarketAdapter
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.model.channel.ChannelUpdateRequest
import com.spyneai.model.channel.ChannelsResponse
import com.spyneai.model.channels.Data
import com.spyneai.model.marketupdate.MarketPlace
import com.spyneai.model.marketupdate.ShootMarketUpdateRequest
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_channel.*
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.activity_order.rvChannels
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChannelActivity : AppCompatActivity() {
    private lateinit var marketAdapter: MarketAdapter
    private lateinit var photoList: List<Data>
    var pos : Int = 0

    lateinit var channelList : List<ChannelsResponse>
    lateinit var channelLists : List<ChannelsResponse>
    lateinit var channelAdapter: ChannelAdapter
    lateinit var marketPlaceList: ArrayList<ChannelUpdateRequest>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setMarkets()
        listeners()
    }

    private fun setMarkets() {
        channelList = ArrayList<ChannelsResponse>()
        channelLists = ArrayList<ChannelsResponse>()
        marketPlaceList = ArrayList<ChannelUpdateRequest>()

        marketAdapter = MarketAdapter(this, channelList as ArrayList<ChannelsResponse>,
                object : MarketAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int, selectedItems: java.util.ArrayList<ChannelsResponse>) {
                        Log.e("position preview", position.toString())
                        marketAdapter.notifyDataSetChanged()
                        pos = position
                        (channelLists as ArrayList).clear()
                        (channelLists as ArrayList).addAll(selectedItems)
                    }
                })
        val layoutManager: RecyclerView.LayoutManager =
                GridLayoutManager(this, 3)
        rvChannels.setLayoutManager(layoutManager)
        rvChannels.setAdapter(marketAdapter)

        (channelList as ArrayList).clear()
        (channelList as ArrayList).addAll(Utilities.getList(this,AppConstants.CHANNEL_LIST)!!)
        marketAdapter.notifyDataSetChanged()
    }

    private fun updateMarket(channelLists : ArrayList<ChannelsResponse>) {
        Utilities.showProgressDialog(this)

        for (i in 0..channelLists.size-1) {
            val marketplaces = ChannelUpdateRequest(
                    channelLists[i].market_id,
                    channelLists[i].image_url,
                    channelLists[i].category)
            marketPlaceList.add(marketplaces)
        }

        val shootId = Utilities.getPreference(this, AppConstants.SHOOT_ID).toString()

        val shootMarketUpdateRequest = ShootMarketUpdateRequest(
                shootId,
            marketPlaceList
        )

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.updateMarket(
                Utilities.getPreference(this, AppConstants.tokenId), shootMarketUpdateRequest)

        call?.enqueue(object : Callback<UploadPhotoResponse> {
            override fun onResponse(call: Call<UploadPhotoResponse>,
                                    response: Response<UploadPhotoResponse>) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    val intent = Intent(this@ChannelActivity, OrderActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            override fun onFailure(call: Call<UploadPhotoResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(this@ChannelActivity,
                        "Server not responding!!!", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun listeners() {
        tvAddMarketplaces.setOnClickListener(View.OnClickListener {
            updateMarket(channelLists as ArrayList<ChannelsResponse>)
        })
    }

}