package com.spyneai.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.gotoHome
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.ai.GifFetchResponse
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_show_gif.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class ShowGifActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_gif)
        fetchGif()

        listeners()
    }

    private fun listeners() {
        tvYourEmailId.setText(Utilities.getPreference(this, AppConstants.EMAIL_ID))

        tvViewAllImages.setOnClickListener(View.OnClickListener {
            val intent = Intent(this,
                    ShowImagesActivity::class.java)
            startActivity(intent)
        })

        ivBackShowGif.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        ivHomeGif.setOnClickListener(View.OnClickListener {
            Utilities.savePrefrence(this@ShowGifActivity, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this@ShowGifActivity, AppConstants.CATEGORY_ID, "")
            Utilities.savePrefrence(this@ShowGifActivity, AppConstants.PRODUCT_ID, "")
            Utilities.savePrefrence(this@ShowGifActivity, AppConstants.SKU_NAME, "")
            Utilities.savePrefrence(this@ShowGifActivity, AppConstants.SKU_ID, "")
            gotoHome()

            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                    this,
                    AppConstants.FRAME_LIST, updateSkuResponseList
            )
        })
        tvRequestWapp.setOnClickListener(View.OnClickListener {
            try {
                val i = Intent(Intent.ACTION_VIEW)
                val url = "https://api.whatsapp.com/send?phone=" + "+919953325165" + "&text=" +
                        URLEncoder.encode("Hey! The Spyne 360° Shot looks impressive; I liked the user experience and would like to learn more about the commercial application and how I can best access this technology. I look forward to connecting!", "UTF-8")
                i.setPackage("com.whatsapp")
                i.setData(Uri.parse(url))


                    startActivity(i)


            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


    //Fetch Gif
    fun fetchGif() {
        Utilities.showProgressDialog(this)
        val request = RetrofitClients.buildService(APiService::class.java)

        val userId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.TOKEN_ID)!!)

        val skuId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.SKU_ID)!!)

        val call = request.fetchUserGif(userId, skuId)

        call?.enqueue(object : Callback<List<GifFetchResponse>> {
            override fun onResponse(call: Call<List<GifFetchResponse>>,
                                    response: Response<List<GifFetchResponse>>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        Glide.with(this@ShowGifActivity) // replace with 'this' if it's in activity
                                .load(response.body()!![0].gif_url)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                                .into(imageView)
                    } else {
                        Toast.makeText(this@ShowGifActivity,
                                "Unable to fetch project details currently. Please try again later !", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<GifFetchResponse>>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(this@ShowGifActivity,
                        "No details found for this project. Please try again later !", Toast.LENGTH_SHORT).show()
            }
        })
    }


}
