package com.spyneai.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.spyneai.R
import com.spyneai.adapter.PhotosAdapter
import com.spyneai.adapter.SkuAdapter
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.model.order.Photo
import com.spyneai.model.order.PlaceOrderResponse
import com.spyneai.model.order.Sku
import com.spyneai.model.sku.Photos
import com.spyneai.model.sku.SkuResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_edit_sku.*
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.activity_order.rvSkus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditSkuActivity : AppCompatActivity() {
    private lateinit var photsAdapter: PhotosAdapter
    private lateinit var photoList: List<Photos>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_sku)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setSkuImages()
        imgBacks.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
    }

    private fun setSkuImages() {
        photoList = ArrayList<Photos>()
        photsAdapter = PhotosAdapter(this, photoList,
            object : PhotosAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position preview", position.toString())
                }
            })

        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(this,2)
        rvPhotos.setLayoutManager(layoutManager)
        rvPhotos.setAdapter(photsAdapter)
        fetchSkuData()
    }

    private fun fetchSkuData() {
        val request = RetrofitClient.buildService(APiService::class.java)

        val call = request.getSkuDetails(
            Utilities.getPreference(this,AppConstants.tokenId),
                intent.getStringExtra(AppConstants.SKU_ID)!!)

        call?.enqueue(object : Callback<SkuResponse> {
            override fun onResponse(call: Call<SkuResponse>,
                                    response: Response<SkuResponse>
            ) {
                if (response.isSuccessful){
                    tvSkuName.setText(response.body()?.payload!!.data.displayName)
                    if (response.body()?.payload!!.data.photos.size > 0)
                    {
                        (photoList as ArrayList).clear()
                        (photoList as ArrayList).addAll(response.body()?.payload!!.data.photos as ArrayList)
                    }
                    photsAdapter.notifyDataSetChanged()
                }
            }
            override fun onFailure(call: Call<SkuResponse>, t: Throwable) {
                Toast.makeText(this@EditSkuActivity,
                    "Server not responding!!!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}