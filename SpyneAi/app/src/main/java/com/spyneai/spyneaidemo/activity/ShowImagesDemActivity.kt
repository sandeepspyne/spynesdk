package com.spyneai.spyneaidemo.activity

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.adapter.ShowReplacedImagesAdapter
import com.spyneai.aipack.FetchBulkResponse
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.gotoHome
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.ScrollingLinearLayoutManager
import com.spyneai.needs.Utilities
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener
import kotlinx.android.synthetic.main.activity_before_after.*
import kotlinx.android.synthetic.main.activity_show_gif.*
import kotlinx.android.synthetic.main.activity_show_images.*
import kotlinx.android.synthetic.main.activity_show_images.ivBackShowImages
import kotlinx.android.synthetic.main.activity_show_images.ivHomeShowImages
import kotlinx.android.synthetic.main.activity_show_images.rvImagesBackgroundRemoved
import kotlinx.android.synthetic.main.activity_show_images.tvViewGif
import kotlinx.android.synthetic.main.activity_show_images.tvYourEmailIdReplaced
import kotlinx.android.synthetic.main.activity_show_images_demo.*
import kotlinx.android.synthetic.main.activity_timer.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.view_custom.view.*
import kotlinx.android.synthetic.main.view_images.view.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class ShowImagesDemActivity : AppCompatActivity() {
    lateinit var imageList : ArrayList<String>
    lateinit var imageListAfter : ArrayList<String>

    private lateinit var showReplacedImagesAdapter: ShowReplacedImagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_images_demo)
        fetchFromLocal()
        setBulkImages()
        setListeners()
    }

    private fun setListeners() {
        tvYourEmailIdReplaced.setText(Utilities.getPreference(this, AppConstants.EMAIL_ID))

        tvViewGif.setOnClickListener(View.OnClickListener {
            val intent = Intent(this,
                    ShowGifDemoActivity::class.java)
            startActivity(intent)
        })


        ivBackShowImages.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        ivHomeShowImages.setOnClickListener(View.OnClickListener {
            Utilities.savePrefrence(this@ShowImagesDemActivity, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this@ShowImagesDemActivity, AppConstants.CATEGORY_ID, "")
            Utilities.savePrefrence(this@ShowImagesDemActivity, AppConstants.PRODUCT_ID, "")
            Utilities.savePrefrence(this@ShowImagesDemActivity, AppConstants.SKU_NAME, "")
            Utilities.savePrefrence(this@ShowImagesDemActivity, AppConstants.SKU_ID, "")
            gotoHome()

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
//                if (i.resolveActivity(packageManager) != null) {
                    startActivity(i)
//                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun setBulkImages() {

        showReplacedImagesAdapter = ShowReplacedImagesAdapter(this,
            imageList as ArrayList<String>,
                imageListAfter as ArrayList<String>,
            object : ShowReplacedImagesAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    showImagesDialog(position)
                    Log.e("position preview", position.toString())
                }
            })

        rvImagesBackgroundRemoved.setLayoutManager(ScrollingLinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        ))

        rvImagesBackgroundRemoved.setAdapter(showReplacedImagesAdapter)
//        fetchBulkUpload()
    }

    private fun fetchFromLocal(){
        imageList = ArrayList<String>()
        imageListAfter = ArrayList<String>()


        imageList.clear()
        imageListAfter.clear()

        if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("4")
            && Utilities.getPreference(this, AppConstants.backgroundNumber).equals("0") )
        {
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/1.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/2.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/3.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/4.jpg")

            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20grey/1_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20grey/2.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20grey/3_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20grey/4_aoutput.png")

        }else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("4")
            && Utilities.getPreference(this, AppConstants.backgroundNumber).equals("1") )
        {

            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/1.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/2.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/3.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/4.jpg")

            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20slate/1_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20slate/2.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20slate/3_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20slate/4_aoutput.png")

        }

        else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("8") && Utilities.getPreference(this, AppConstants.backgroundNumber).equals("0"))
        {


            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/1.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/2.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/3.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/4.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/5.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/6.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/7.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/8.jpg")

            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20grey/1_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20grey/2.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20grey/3_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20grey/4_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20grey/5_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20grey/6_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20grey/7_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20grey/8_aoutput.png")


        }
        else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("8")
            && Utilities.getPreference(this, AppConstants.backgroundNumber).equals("1"))
        {

            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/1.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/2.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/3.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/4.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/5.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/6.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/7.jpg")
            imageList.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/8.jpg")

            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20slate/1_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20slate/2.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20slate/3_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20slate/4_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20slate/5_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20slate/6_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20slate/7_aoutput.png")
            imageListAfter.add("https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/radiant%20slate/8_aoutput.png")


        }

        Log.e("Frame Shoots", Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).toString())
        Log.e("BG Shoots", Utilities.getPreference(this, AppConstants.backgroundNumber).toString())
        Log.e("imageList", imageList.toString())
        Log.e("imageAfterList", imageListAfter.toString())


    }

    //Fetch bulk data
    private fun fetchBulkUpload() {
        Utilities.showProgressDialog(this)
        val request = RetrofitClients.buildService(APiService::class.java)
        val userId = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(this, AppConstants.TOKEN_ID)!!)
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
                    for (i in 0..response.body()!!.size-1) {
                        (imageList as ArrayList).add(response.body()!![i].input_image_url)
                        (imageListAfter as ArrayList).add(response.body()!![i].output_image_url)
                    }
                }
                showReplacedImagesAdapter.notifyDataSetChanged()
            }
            override fun onFailure(call: Call<List<FetchBulkResponse>>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(this@ShowImagesDemActivity,
                    "Server not responding!!!", Toast.LENGTH_SHORT).show()

            }
        })
    }



    fun showImagesDialog(position: Int)
    {
        Utilities.showProgressDialog(this)
        Handler().postDelayed({
            Utilities.hideProgressDialog()
        }, 3000)

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_show_images)

        val window: Window = dialog.getWindow()!!
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT)

        val carouselViewImages: CarouselView = dialog.findViewById(R.id.carouselViewImages)
        val ivCrossImages: ImageView = dialog.findViewById(R.id.ivCrossImages)

        ivCrossImages.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        carouselViewImages.setPageCount(imageList.size);
        carouselViewImages.setViewListener(viewListener);
        carouselViewImages.setCurrentItem(position)

        dialog.show()
    }


    var viewListener = object : ViewListener {
        override fun setViewForPosition(position: Int): View? {
            val customView: View = layoutInflater.inflate(R.layout.view_images, null)

            Glide.with(this@ShowImagesDemActivity) // replace with 'this' if it's in activity
                    .load(imageList[position])
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                    .into(customView.ivBefore)
            Glide.with(this@ShowImagesDemActivity) // replace with 'this' if it's in activity
                    .load(imageListAfter[position])
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                    .into(customView.ivAfter)

            return customView
        }
    }

    override fun onBackPressed() {
        //  super.onBackPressed()
        showExitDialog()
    }

    fun showExitDialog( ) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_exit)
        val dialogButtonYes: TextView = dialog.findViewById(R.id.btnYes)
        val dialogButtonNo: TextView = dialog.findViewById(R.id.btnNo)

        dialogButtonYes.setOnClickListener(View.OnClickListener {
            Utilities.savePrefrence(this@ShowImagesDemActivity, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this@ShowImagesDemActivity, AppConstants.CATEGORY_ID, "")
            Utilities.savePrefrence(this@ShowImagesDemActivity, AppConstants.PRODUCT_ID, "")
            Utilities.savePrefrence(this@ShowImagesDemActivity, AppConstants.SKU_NAME, "")
            Utilities.savePrefrence(this@ShowImagesDemActivity, AppConstants.SKU_ID, "")


            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                this@ShowImagesDemActivity,
                AppConstants.FRAME_LIST, updateSkuResponseList
            )
            val intent = Intent(this, MainDashboardActivity::class.java)
            startActivity(intent)
            finish()
            dialog.dismiss()

        })
        dialogButtonNo.setOnClickListener(View.OnClickListener { dialog.dismiss() })
        dialog.show()
    }

}