package com.spyneai.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.adapter.PhotosAdapter
import com.spyneai.adapter.ShowReplacedImagesAdapter
import com.spyneai.aipack.FetchBulkResponse
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.sku.Photos
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_show_gif.*
import kotlinx.android.synthetic.main.activity_show_gif.ivHomeGif
import kotlinx.android.synthetic.main.activity_show_gif.tvRequestWapp
import kotlinx.android.synthetic.main.activity_show_images.*
import kotlinx.android.synthetic.main.activity_timer.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.internal.Util
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class ShowImagesActivity : AppCompatActivity() {
    lateinit var imageList : List<String>

    private lateinit var showReplacedImagesAdapter: ShowReplacedImagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_images)
        setBulkImages()
        setListeners()
    }

    private fun setListeners() {
        ivBackShowImages.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        ivHomeShowImages.setOnClickListener(View.OnClickListener {
            Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.CATEGORY_ID, "")
            Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.PRODUCT_ID, "")
            Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.SKU_NAME, "")
            Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.SKU_ID, "")
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()

            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                    this,
                    AppConstants.FRAME_LIST, updateSkuResponseList
            )
        })

        tvRequestWappImages.setOnClickListener(View.OnClickListener {
            try {
                val i = Intent(Intent.ACTION_VIEW)
                val url = "https://api.whatsapp.com/send?phone=" + "+919953325165" + "&text=" +
                        URLEncoder.encode("Hey! The Spyne 360° Shot looks impressive; I liked the user experience and would like to learn more about the commercial application and how I can best access this technology. I look forward to connecting!", "UTF-8")
                i.setPackage("com.whatsapp")
                i.setData(Uri.parse(url))
                if (i.resolveActivity(packageManager) != null) {
                    startActivity(i)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun setBulkImages() {
        imageList = ArrayList<String>()

        showReplacedImagesAdapter = ShowReplacedImagesAdapter(this,
            imageList as ArrayList<String>,
            object : ShowReplacedImagesAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position preview", position.toString())
                }
            })

        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(this,2)
        rvImagesBackgroundRemoved.setLayoutManager(layoutManager)
        rvImagesBackgroundRemoved.setAdapter(showReplacedImagesAdapter)
        fetchBulkUpload()
    }

    //Fetch bulk data
    private fun fetchBulkUpload() {
        Utilities.showProgressDialog(this)
        val request = RetrofitClients.buildService(APiService::class.java)
        val userId = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(this, AppConstants.tokenId)!!)
        val skuId = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(this, AppConstants.SKU_ID)!!)

        val call = request.fetchBulkImage(userId,skuId)

        call?.enqueue(object : Callback<List<FetchBulkResponse>> {
            override fun onResponse(call: Call<List<FetchBulkResponse>>,
                                    response: Response<List<FetchBulkResponse>>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    for (i in 0..response.body()!!.size-1)
                        (imageList as ArrayList).add(response.body()!![i].output_image_url)
                }
                showReplacedImagesAdapter.notifyDataSetChanged()
            }
            override fun onFailure(call: Call<List<FetchBulkResponse>>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(this@ShowImagesActivity,
                    "Server not responding!!!", Toast.LENGTH_SHORT).show()

            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}