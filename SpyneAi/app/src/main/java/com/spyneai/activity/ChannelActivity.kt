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
import com.spyneai.adapter.MarketAdapter
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.model.channels.Data
import com.spyneai.model.channels.MarketplaceResponse
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

    lateinit var marketPlaceList : List<Data>
    lateinit var marketPlace: ArrayList<MarketPlace>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setMarkets()
        listeners()

    }


    private fun setMarkets() {
        photoList = ArrayList<Data>()
        marketPlaceList = ArrayList<Data>()
        marketPlace = ArrayList<MarketPlace>()

        marketAdapter = MarketAdapter(this, photoList,
                object : MarketAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int, selectedItems: ArrayList<Data>) {
                        Log.e("position preview", position.toString())
                        marketAdapter.notifyDataSetChanged()
                        pos = position
                        (marketPlaceList as ArrayList).clear()
                        (marketPlaceList as ArrayList).addAll(selectedItems)
                    }
                })
        val layoutManager: RecyclerView.LayoutManager =
                GridLayoutManager(this, 3)
        rvChannels.setLayoutManager(layoutManager)
        rvChannels.setAdapter(marketAdapter)

        fetchMarkets()
    }

    private fun fetchMarkets() {
        Utilities.showProgressDialog(this)
        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.getMarketList(
                Utilities.getPreference(this, AppConstants.tokenId),
                Utilities.getPreference(this, AppConstants.CATEGORY_ID),
                Utilities.getPreference(this, AppConstants.PRODUCT_ID))

        call?.enqueue(object : Callback<MarketplaceResponse> {
            override fun onResponse(call: Call<MarketplaceResponse>,
                                    response: Response<MarketplaceResponse>) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    if (response.body()?.payload!!.data.isNotEmpty())
                    {
                        (photoList as ArrayList).clear()
                        (photoList as ArrayList).addAll(response.body()?.payload!!.data as ArrayList)
                    }
                    marketAdapter.notifyDataSetChanged()
                }
            }
            override fun onFailure(call: Call<MarketplaceResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(this@ChannelActivity,
                        "Server not responding!!!", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun updateMarket(marketPlaceList : ArrayList<Data>) {
        Utilities.showProgressDialog(this)

        for (i in 0..marketPlaceList.size-1) {
            val marketplaces = MarketPlace(marketPlaceList[i].markId, marketPlaceList[i].displayName)
            marketPlace.add(marketplaces)
        }

        val shootMarketUpdateRequest = ShootMarketUpdateRequest(
            Utilities.getPreference(this, AppConstants.SHOOT_ID).toString(),
            marketPlace
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
            updateMarket(marketPlaceList as ArrayList<Data>)
        })
    }

}