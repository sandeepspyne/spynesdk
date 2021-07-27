package com.spyneai.processedimages.ui

import android.app.*
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.downloader.*
import com.spyneai.R
import com.spyneai.activity.DownloadingActivity
import com.spyneai.activity.OrderSummary2Activity
import com.spyneai.activity.ShowGifActivity
import com.spyneai.adapter.ShowReplacedImagesAdapter
import com.spyneai.adapter.ShowReplacedImagesFocusedAdapter
import com.spyneai.adapter.ShowReplacedImagesInteriorAdapter
import com.spyneai.credits.model.ReviewHolder
import com.spyneai.downloadsku.FetchBulkResponseV2
import com.spyneai.gotoHome
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClientSpyneAi
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.ScrollingLinearLayoutManager
import com.spyneai.needs.Utilities
import com.spyneai.shoot.utils.log
import com.spyneai.videorecording.model.VideoProcessingResponse

import com.spyneai.videorecording.service.FramesHelper
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ViewListener

import kotlinx.android.synthetic.main.activity_show_images.*
import kotlinx.android.synthetic.main.view_images.view.*

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class ShowImagesActivity : AppCompatActivity() {
    lateinit var builder: NotificationCompat.Builder
    lateinit var imageList: List<String>
    lateinit var imageListAfter: List<String>
    lateinit var imageListInterior: List<String>
    lateinit var imageListFocused: List<String>

    lateinit var imageListWaterMark: ArrayList<String>
    lateinit var listHdQuality: ArrayList<String>
    lateinit var imageNameList: ArrayList<String>
    var catName: String = ""
    var numberOfImages: Int = 0

    private lateinit var showReplacedImagesAdapter: ShowReplacedImagesAdapter
    private lateinit var ShowReplacedImagesInteriorAdapter: ShowReplacedImagesInteriorAdapter
    private lateinit var ShowReplacedImagesFocusedAdapter: ShowReplacedImagesFocusedAdapter

    var downloadCount: Int = 0
    lateinit var Category: String
    var TAG = "ShowImagesActivity"

    var downloadHighQualityCount: Int = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_images)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setBulkImages()

        setListeners()

        imageNameList = ArrayList()

        if (intent.getStringExtra(AppConstants.CATEGORY_NAME) != null)
            catName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
        else
            catName = Utilities.getPreference(this, AppConstants.CATEGORY_NAME)!!

        //checkThreeSixtyInterior()

        if (catName.equals("Footwear")) {
            tvViewGif.visibility = View.GONE
        }
    }

//    private fun checkThreeSixtyInterior() {
//
//        val request = RetrofitClientSpyneAi.buildService(APiService::class.java)
//
//        val call = request.getThreeSixtyInteriorByShootId(
//            Utilities.getPreference(this, AppConstants.SKU_ID)
//                .toString()
//        )
//
//        call?.enqueue(object : Callback<VideoProcessingResponse> {
//            override fun onResponse(
//                call: Call<VideoProcessingResponse>,
//                response: Response<VideoProcessingResponse>
//            ) {
//                Log.d(TAG, "onResponse:  processVideo success ")
//                if (response.isSuccessful) {
//
//                    var videoProcessResponse = response.body()
//
//                    if (videoProcessResponse != null) {
//                        //   task.frames = response.body()
//                        FramesHelper.framesMap.put(
//                            (Utilities.getPreference(this@ShowImagesActivity, AppConstants.SKU_ID).toString()), videoProcessResponse
//                        )
//
//                        tv_three_sixty_view.visibility = View.VISIBLE
//                        tv_three_sixty_view.setOnClickListener {
//                            var intent = Intent(this@ShowImagesActivity, ThreeSixtyInteriorViewActivity::class.java)
//                            intent.action = Utilities.getPreference(this@ShowImagesActivity, AppConstants.SKU_ID).toString()
//
//                            startActivity(intent)
//                        }
//                    } else {
//                        Log.d(TAG, "onResponse:  processVideo success null ")
//                    }
//                } else {
//
//                }
//            }
//
//            override fun onFailure(call: Call<VideoProcessingResponse>, t: Throwable) {
//                Log.d(TAG, "onResponse: processVideo failure" + t.localizedMessage)
//            }
//        })
//
//    }

    private fun hideData(i: Int) {

        if (i == 0) {
            tvYourEmailIdReplaced.visibility = View.VISIBLE
            tvViewGif.visibility = View.GONE
            tvInterior.visibility = View.GONE
//            llDownloads.visibility = View.VISIBLE
        } else {
            tvYourEmailIdReplaced.visibility = View.GONE
            tvViewGif.visibility = View.GONE
            tvInterior.visibility = View.GONE
//            llDownloads.visibility = View.GONE
        }
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
            gotoHome()

            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                this,
                AppConstants.FRAME_LIST, updateSkuResponseList
            )
        })

        tvDownloadFree.setOnClickListener {
            Utilities.savePrefrence(this, AppConstants.DOWNLOAD_TYPE, "watermark")
            val downloadIntent = Intent(this, DownloadingActivity::class.java)
            downloadIntent.putExtra(AppConstants.LIST_WATERMARK, imageListWaterMark)
            downloadIntent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
            downloadIntent.putExtra(AppConstants.LIST_IMAGE_NAME, imageNameList)
            downloadIntent.putExtra("is_paid",intent.getBooleanExtra("is_paid",false))
            startActivity(downloadIntent)
        }

        llDownloadHdImages.setOnClickListener {
            Utilities.savePrefrence(this, AppConstants.DOWNLOAD_TYPE, "hd")
            val orderIntent = Intent(this, OrderSummary2Activity::class.java)
            orderIntent.putExtra(AppConstants.LIST_WATERMARK, imageListWaterMark)
            orderIntent.putExtra(AppConstants.LIST_HD_QUALITY, listHdQuality)
            orderIntent.putExtra(AppConstants.LIST_IMAGE_NAME, imageNameList)
            orderIntent.putExtra("is_paid",intent.getBooleanExtra("is_paid",false))

            var skuId = Utilities.getPreference(this, AppConstants.SKU_ID)
                .toString()

            var skuName = Utilities.getPreference(this, AppConstants.SKU_ID)
                .toString()

            orderIntent.putExtra(AppConstants.SKU_ID,skuId)
            orderIntent.putExtra(AppConstants.SKU_NAME,skuName)
            orderIntent.putExtra(AppConstants.IMAGE_TYPE,intent.getStringExtra(AppConstants.IMAGE_TYPE))
            startActivity(orderIntent)
        }
    }


    private fun setBulkImages() {
        Utilities.showProgressDialog(this)
        imageList = ArrayList<String>()
        imageListAfter = ArrayList<String>()
        imageListWaterMark = ArrayList<String>()
        imageListInterior = ArrayList<String>()
        imageListFocused = ArrayList<String>()
        listHdQuality = ArrayList<String>()

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


        ShowReplacedImagesFocusedAdapter = ShowReplacedImagesFocusedAdapter(this,
            imageListFocused as ArrayList<String>,
            object : ShowReplacedImagesFocusedAdapter.BtnClickListener {
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

        rvFocused.setLayoutManager(
            GridLayoutManager(
                this,
                2
            )
        )

        rvImagesBackgroundRemoved.setAdapter(showReplacedImagesAdapter)
        rvInteriors.setAdapter(ShowReplacedImagesInteriorAdapter)
        rvFocused.setAdapter(ShowReplacedImagesFocusedAdapter)
        fetchBulkUpload()
    }

    //Fetch bulk data
    private fun fetchBulkUpload() {
        val request = RetrofitClients.buildService(APiService::class.java)
        val authKey = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(this, AppConstants.AUTH_KEY)!!
        )
        val skuId = RequestBody.create(
            MultipartBody.FORM,
            Utilities.getPreference(this, AppConstants.SKU_ID)!!
        )

        val call = request.fetchBulkImageV2(skuId, authKey)

        call?.enqueue(object : Callback<FetchBulkResponseV2> {
            override fun onResponse(
                call: Call<FetchBulkResponseV2>,
                response: Response<FetchBulkResponseV2>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    var dataList: List<FetchBulkResponseV2.Data> = response.body()!!.data
                    for (i in 0..(dataList.size) -1) {
                        if (dataList!![i].image_category.equals("Exterior")) {
                            Category = dataList!![i].image_category
                            (imageList as ArrayList).add(dataList!![i].input_image_lres_url)
                            (imageListAfter as ArrayList).add(dataList!![i].output_image_lres_wm_url)

                            //save for in case of user review
                            if (imageListAfter != null && imageList.size > 0)
                                ReviewHolder.orgUrl = imageList.get(0)

                            if (imageListAfter != null && imageListAfter.size > 0)
                                ReviewHolder.editedUrl = imageListAfter.get(0)

                            (imageListWaterMark as ArrayList).add(dataList!![i].output_image_lres_wm_url)
                            (listHdQuality as ArrayList).add(dataList!![i].output_image_hres_url)

                            imageNameList.add(dataList[i].image_name)

                            Utilities.savePrefrence(
                                this@ShowImagesActivity,
                                AppConstants.NO_OF_IMAGES,
                                imageListAfter.size.toString()
                            )

                            hideData(0)
                        } else if (dataList!![i].image_category.equals("Interior")) {
                            Category = dataList!![i].image_category
                            (imageListInterior as ArrayList).add(dataList!![i].output_image_lres_url)
                            (imageListWaterMark as ArrayList).add(dataList!![i].output_image_lres_wm_url)
                            (listHdQuality as ArrayList).add(dataList!![i].output_image_hres_url)
                            imageNameList.add(dataList[i].image_name)

                            Utilities.savePrefrence(
                                this@ShowImagesActivity,
                                AppConstants.NO_OF_IMAGES,
                                imageListAfter.size.toString()
                            )
                            hideData(0)
                        } else if (dataList!![i].image_category.equals("Focus Shoot")) {
                            Category = dataList!![i].image_category
                            (imageListFocused as ArrayList).add(dataList!![i].output_image_lres_url)
                            (imageListWaterMark as ArrayList).add(dataList!![i].output_image_lres_wm_url)
                            (listHdQuality as ArrayList).add(dataList!![i].output_image_hres_url)
                            imageNameList.add(dataList[i].image_name)

                            Utilities.savePrefrence(
                                this@ShowImagesActivity,
                                AppConstants.NO_OF_IMAGES,
                                imageListAfter.size.toString()
                            )
                            hideData(0)
                        } else {
                            Category = dataList!![i].image_category
                            (imageList as ArrayList).add(dataList!![i].input_image_lres_url)
                            (imageListAfter as ArrayList).add(dataList!![i].output_image_lres_wm_url)
                            (listHdQuality as ArrayList).add(dataList!![i].output_image_hres_url)
                            (imageListWaterMark as ArrayList).add(dataList!![i].output_image_lres_wm_url)
                            imageNameList.add(dataList[i].image_name)

                            Utilities.savePrefrence(
                                this@ShowImagesActivity,
                                AppConstants.NO_OF_IMAGES,
                                imageListAfter.size.toString()
                            )
                            hideData(1)
                        }
                    }
                }
                showReplacedImagesAdapter.notifyDataSetChanged()
                ShowReplacedImagesInteriorAdapter.notifyDataSetChanged()
                ShowReplacedImagesFocusedAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<FetchBulkResponseV2>, t: Throwable) {
                Utilities.hideProgressDialog()
                log("Error: "+t.localizedMessage)
                Toast.makeText(
                    this@ShowImagesActivity,
                    "Server not responding!!!", Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun showImagesDialog(position: Int) {
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
}
