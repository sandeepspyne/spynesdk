package com.spyneai.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.spyneai.R
import com.spyneai.adapter.ShowReplacedImagesAdapter
import com.spyneai.adapter.ShowReplacedImagesInteriorAdapter
import com.spyneai.aipack.FetchBulkResponse
import com.spyneai.camera2.Camera2Activity
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
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ShowImagesActivity : AppCompatActivity() {
    lateinit var imageList : List<String>
    lateinit var imageListAfter : List<String>
    lateinit var imageListInterior : List<String>

    private lateinit var showReplacedImagesAdapter: ShowReplacedImagesAdapter
    private lateinit var ShowReplacedImagesInteriorAdapter: ShowReplacedImagesInteriorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_images)
        setBulkImages()
        setListeners()
    }

    private fun setListeners() {
        tvViewGif.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this,
                ShowGifActivity::class.java
            )
            startActivity(intent)
        })


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
                        URLEncoder.encode(
                            "Hey! The Spyne 360° Shot looks impressive; I liked the user experience and would like to learn more about the commercial application and how I can best access this technology. I look forward to connecting!",
                            "UTF-8"
                        )
                i.setPackage("com.whatsapp")
                i.setData(Uri.parse(url))
                startActivity(i)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun setBulkImages() {
        imageList = ArrayList<String>()
        imageListAfter = ArrayList<String>()
        imageListInterior = ArrayList<String>()

        showReplacedImagesAdapter = ShowReplacedImagesAdapter(this,
            imageList as ArrayList<String>,
            imageListAfter as ArrayList<String>,
            object : ShowReplacedImagesAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    showImagesDialog(position)
                    Log.e("position preview", position.toString())
                }
            })

        ShowReplacedImagesInteriorAdapter = ShowReplacedImagesInteriorAdapter(this,
            imageListInterior as ArrayList<String>,
            object : ShowReplacedImagesInteriorAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                 //   showImagesDialog(position)
                    Log.e("position preview", position.toString())
                }
            })

        rvImagesBackgroundRemoved.setLayoutManager(
            ScrollingLinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )

        rvInteriors.setLayoutManager(
            GridLayoutManager(
                this,
                2
            )
        )

        rvImagesBackgroundRemoved.setAdapter(showReplacedImagesAdapter)
        rvInteriors.setAdapter(ShowReplacedImagesInteriorAdapter)
        fetchBulkUpload()
    }

    //Fetch bulk data
    private fun fetchBulkUpload() {
        Utilities.showProgressDialog(this)
        val request = RetrofitClients.buildService(APiService::class.java)
        val userId = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(this, AppConstants.tokenId)!!
        )
        val skuId = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(this, AppConstants.SKU_ID)!!
        )

        val call = request.fetchBulkImage(userId, skuId)

        call?.enqueue(object : Callback<List<FetchBulkResponse>> {
            override fun onResponse(
                call: Call<List<FetchBulkResponse>>,
                response: Response<List<FetchBulkResponse>>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    for (i in 0..response.body()!!.size - 1) {
                        if (response.body()!![i].category.equals("Exterior")) {
                            (imageList as ArrayList).add(response.body()!![i].input_image_url)
                            (imageListAfter as ArrayList).add(response.body()!![i].output_image_url)
                        }
                        else{
                            (imageListInterior as ArrayList).add(response.body()!![i].output_image_url)
                        }
                    }
                }
                showReplacedImagesAdapter.notifyDataSetChanged()
                ShowReplacedImagesInteriorAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<List<FetchBulkResponse>>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(
                    this@ShowImagesActivity,
                    "Server not responding!!!", Toast.LENGTH_SHORT
                ).show()

            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.SHOOT_ID, "")
        Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.CATEGORY_ID, "")
        Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.PRODUCT_ID, "")
        Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.SKU_NAME, "")
        Utilities.savePrefrence(this@ShowImagesActivity, AppConstants.SKU_ID, "")
        val intent = Intent(this, DashboardActivity::class.java)

        val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
        updateSkuResponseList.clear()

        Utilities.setList(
            this@ShowImagesActivity,
            AppConstants.FRAME_LIST, updateSkuResponseList
        )
        startActivity(intent)
        finish()
    }

    fun showImagesDialog(position: Int) {
        Utilities.showProgressDialog(this)
        Handler().postDelayed({
            Utilities.hideProgressDialog()
        }, 3000)

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_show_images)

        val window: Window = dialog.getWindow()!!
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

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

            Glide.with(this@ShowImagesActivity) // replace with 'this' if it's in activity
                .load(imageList[position])
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(customView.ivBefore)
            Glide.with(this@ShowImagesActivity) // replace with 'this' if it's in activity
                .load(imageListAfter[position])
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                .into(customView.ivAfter)

            return customView
        }
    }


    //Download
    /*
    fun downLoad()
    {
        PRDownloader.initialize(getApplicationContext());
        // Enabling database for resume support even after the application is killed:
        // Enabling database for resume support even after the application is killed:
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build()
        PRDownloader.initialize(applicationContext, config)


        val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        val downloadId = PRDownloader.download(
            "https://storage.googleapis.com/spyne-cliq/spyne-cliq/product/cars/demo/8angles/1.jpg",
            getOutputDirectory(),
            "Spyne" + SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()))
            .build()
            .setOnStartOrResumeListener { }
            .setOnPauseListener { }
            .setOnCancelListener(object : OnCancelListener {
                override fun onCancel() {}
            })
            .setOnProgressListener { }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {

                }

                override fun onError(error: com.downloader.Error?) {
                    TODO("Not yet implemented")
                }

                fun onError(error: Error?) {}
            })
    }
    */


    private fun getOutputDirectory(): String? {
        val mediaDir = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            externalMediaDirs.firstOrNull()?.let {
                File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir.toString() else filesDir.toString()
    }


}