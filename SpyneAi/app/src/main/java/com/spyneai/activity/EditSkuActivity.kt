package com.spyneai.activity

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
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

        ivDeleteSkus.setOnClickListener(View.OnClickListener {
            showDialogDelete(tvSkuName.text.toString())
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

    private fun deleteSkus() {
        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.deleteSku(
                Utilities.getPreference(this, AppConstants.tokenId),
                Utilities.getPreference(this, AppConstants.SHOOT_ID),
                Utilities.getPreference(this, AppConstants.SKU_ID))

        call?.enqueue(object : Callback<PlaceOrderResponse> {
            override fun onResponse(call: Call<PlaceOrderResponse>,
                                    response: Response<PlaceOrderResponse>) {
                if (response.isSuccessful) {
                    onBackPressed()
                }
            }

            override fun onFailure(call: Call<PlaceOrderResponse>, t: Throwable) {
                Toast.makeText(this@EditSkuActivity,
                        "Server not responding!!!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun showDialogDelete( msg: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_delete)
        val text = dialog.findViewById(R.id.tvSkuNameDialog) as TextView
        text.text = msg

        val dialogButtonYes: TextView = dialog.findViewById(R.id.btnYes)
        val dialogButtonNo: TextView = dialog.findViewById(R.id.btnNo)

        dialogButtonYes.setOnClickListener(View.OnClickListener { deleteSkus() })
        dialogButtonNo.setOnClickListener(View.OnClickListener { dialog.dismiss() })
        dialog.show()
    }


}