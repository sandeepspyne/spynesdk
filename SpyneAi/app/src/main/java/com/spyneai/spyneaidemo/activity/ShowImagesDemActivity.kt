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
import com.spyneai.BuildConfig
import com.spyneai.R
import com.spyneai.activity.DashboardActivity
import com.spyneai.adapter.ShowReplacedImagesAdapter
import com.spyneai.aipack.FetchBulkResponse
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
        setContentView(R.layout.activity_show_images)
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



        if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("4") && Utilities.getPreference(this, AppConstants.backgroundNumber).equals("0") )
        {
            var path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.b1)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.b2)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.b3)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.b4)
            imageList.add(path.toString())

            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantslate41)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantslate42)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantslate43)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantslate44)
            imageListAfter.add(path.toString())

        }else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("4") && Utilities.getPreference(this, AppConstants.backgroundNumber).equals("1") )
        {
            var path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.b1)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.b2)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.b3)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.b4)
            imageList.add(path.toString())

            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantsilver41)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantsilver42)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantsilver43)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantsilver44)
            imageListAfter.add(path.toString())

        }

        else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("8") && Utilities.getPreference(this, AppConstants.backgroundNumber).equals("0"))
        {

            var path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before1)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before2)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before3)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before4)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before5)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before6)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before7)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before8)
            imageList.add(path.toString())

            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantslate81)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantslate82)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantslate83)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantslate84)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantslate85)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantslate86)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantslate87)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantslate88)
            imageListAfter.add(path.toString())


        }
        else if (Utilities.getPreference(this, AppConstants.FRAME_SHOOOTS).equals("8") && Utilities.getPreference(this, AppConstants.backgroundNumber).equals("1"))
        {

            var path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before1)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before2)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before3)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before4)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before5)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before6)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before7)
            imageList.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.before8)
            imageList.add(path.toString())

            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantsilver81)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantsilver82)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantsilver83)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantsilver84)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantsilver85)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantsilver86)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantsilver87)
            imageListAfter.add(path.toString())
            path = Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.radiantsilver88)
            imageListAfter.add(path.toString())


        }





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
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
            dialog.dismiss()

        })
        dialogButtonNo.setOnClickListener(View.OnClickListener { dialog.dismiss() })
        dialog.show()
    }

}