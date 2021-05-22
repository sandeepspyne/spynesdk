package com.spyneai.spyneaidemo.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.dashboard.ui.dashboard.MainDashboardActivity
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_show_gif.*
import java.net.URLEncoder

class ShowGifDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_gif_demo)
        showGif()
        listeners()
        //sendEmail()
    }

    private fun listeners() {
        tvYourEmailId.setText(Utilities.getPreference(this, AppConstants.EMAIL_ID))

        tvViewAllImages.setOnClickListener(View.OnClickListener {
            val intent = Intent(this,
                    ShowImagesDemActivity::class.java)
            startActivity(intent)
        })

        ivBackShowGif.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        ivHomeGif.setOnClickListener(View.OnClickListener {
            Utilities.savePrefrence(this@ShowGifDemoActivity, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this@ShowGifDemoActivity, AppConstants.CATEGORY_ID, "")
            Utilities.savePrefrence(this@ShowGifDemoActivity, AppConstants.PRODUCT_ID, "")
            Utilities.savePrefrence(this@ShowGifDemoActivity, AppConstants.SKU_NAME, "")
            Utilities.savePrefrence(this@ShowGifDemoActivity, AppConstants.SKU_ID, "")
            val intent = Intent(this, MainDashboardActivity::class.java)
            startActivity(intent)
            finish()

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
                if (i.resolveActivity(packageManager) != null) {
                    startActivity(i)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

    }

    private fun showGif() {

        if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("4") && Utilities.getPreference(this, AppConstants.backgroundNumber).equals("0"))
        {
            Glide.with(this) // replace with 'this' if it's in activity
                .load("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/4angles/radiant%20grey/radiant%20grey%20gif.gif")
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(imageView)

        }else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("4") && Utilities.getPreference(this, AppConstants.backgroundNumber).equals("1"))
        {
            Glide.with(this) // replace with 'this' if it's in activity
                .load("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/4angles/radiant%20slate/radiant%20slate%20gif.gif")
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(imageView)

        }

        else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("8") && Utilities.getPreference(this, AppConstants.backgroundNumber).equals("0"))
        {
            Glide.with(this) // replace with 'this' if it's in activity
                .load("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20grey/radiant%20grey%20gif.gif")
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(imageView)

        }else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("8") && Utilities.getPreference(this, AppConstants.backgroundNumber).equals("1"))
        {
            Glide.with(this) // replace with 'this' if it's in activity
                .load("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20slate/radiant%20slate%20gif.gif")
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(imageView)

        }


        Log.e("Gif to play",intent.getStringExtra(AppConstants.GIF).toString())
        // webView.loadUrl(intent.getStringExtra(AppConstants.GIF)!!);
    }

//    override fun onBackPressed() {
//        super.onBackPressed()
//        val intent = Intent(this,
//                MainDashboardActivity::class.java)
//        startActivity(intent)
//        finish()
//    }

}