package com.spyneai.orders.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.credits.model.ReviewHolder
import com.spyneai.databinding.ActivityKarviShowImagesBinding
import com.spyneai.downloadsku.FetchBulkResponseV2
import com.spyneai.gotoHome
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.needs.AppConstants
import com.spyneai.needs.ScrollingLinearLayoutManager
import com.spyneai.needs.Utilities
import com.spyneai.orders.ui.adapter.KarviImagesAdapter
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.dialogs.ResolutionNotSupportedFragment
import com.spyneai.shoot.utils.log
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KarviShowImagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKarviShowImagesBinding
    lateinit var builder: NotificationCompat.Builder
    lateinit var imageList: List<String>
    lateinit var imageListAfter: List<String>
    lateinit var imageListInterior: List<String>
    lateinit var imageListFocused: List<String>

    lateinit var imageListWaterMark: ArrayList<String>
    lateinit var listHdQuality: ArrayList<String>
    var catName: String = ""

    private lateinit var showReplacedImagesAdapter: KarviImagesAdapter
    private lateinit var ShowReplacedImagesInteriorAdapter: KarviImagesAdapter
    private lateinit var ShowReplacedImagesFocusedAdapter: KarviImagesAdapter

    lateinit var Category: String
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityKarviShowImagesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setBulkImages()

        setListeners()

        if (intent.getStringExtra(AppConstants.CATEGORY_NAME) != null)
            catName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
        else
            catName = Utilities.getPreference(this, AppConstants.CATEGORY_NAME)!!



    }

    private fun hideData(i: Int) {

        if (i == 0) {
            binding.tvYourEmailIdReplaced.visibility = View.VISIBLE
            binding.tvViewGif.visibility = View.GONE
            binding.tvInterior.visibility = View.GONE
        } else {
            binding.tvYourEmailIdReplaced.visibility = View.GONE
            binding.tvViewGif.visibility = View.GONE
            binding.tvInterior.visibility = View.GONE
        }
    }

    private fun setListeners() {

        binding.ivBackShowImages.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        binding.ivHomeShowImages.setOnClickListener(View.OnClickListener {
            gotoHome()
        })



        binding.llStartNewShoot.setOnClickListener {
            when(getString(R.string.app_name)){
                AppConstants.KARVI -> {
                    val cm = getSystemService(android.content.Context.CAMERA_SERVICE) as CameraManager

                    if (cm.cameraIdList != null && cm.cameraIdList.size > 1) {
                        val characteristics: CameraCharacteristics =
                            cm.getCameraCharacteristics("1")

                        val configs = characteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                        )

                        val s = configs?.getOutputSizes(ImageFormat.JPEG)

                        var resolutionSupported = false

                        s?.forEach { it ->
                            if (!resolutionSupported && it != null) {
                                if (it.width == 1024 && it.height == 768)
                                    resolutionSupported = true
                            }
                        }

                        if (resolutionSupported) {
                            startShoot()
                        }else {
                            //resolution not supported
                            ResolutionNotSupportedFragment().show(supportFragmentManager,"ResolutionNotSupportedFragment")
                        }
                    }else {
                        //resolution not supported
                        ResolutionNotSupportedFragment().show(supportFragmentManager,"ResolutionNotSupportedFragment")
                    }
                }

                else -> {
                    startShoot()
                }
            }
        }
    }

    private fun startShoot() {
        val intent = Intent(this, ShootActivity::class.java)
        intent.putExtra(AppConstants.CATEGORY_ID, AppConstants.CARS_CATEGORY_ID)
        intent.putExtra(AppConstants.CATEGORY_NAME,"Automobiles")
        startActivity(intent)
    }

    private fun setBulkImages() {
        Utilities.showProgressDialog(this)
        imageList = ArrayList<String>()
        imageListAfter = ArrayList<String>()
        imageListWaterMark = ArrayList<String>()
        imageListInterior = ArrayList<String>()
        imageListFocused = ArrayList<String>()
        listHdQuality = ArrayList<String>()

        showReplacedImagesAdapter = KarviImagesAdapter(this,
            listHdQuality as ArrayList<String>,
            object : KarviImagesAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    showImagesDialog(listHdQuality[position])
                    Log.e("position preview", position.toString())
                }
            })

        ShowReplacedImagesInteriorAdapter = KarviImagesAdapter(this,
            imageListInterior as ArrayList<String>,
            object : KarviImagesAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    //   showImagesDialog(position)
                    Log.e("position preview", position.toString())
                }
            })


        ShowReplacedImagesFocusedAdapter = KarviImagesAdapter(this,
            imageListFocused as ArrayList<String>,
            object : KarviImagesAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    //   showImagesDialog(position)
                    Log.e("position preview", position.toString())
                }
            })

        binding.rvImagesBackgroundRemoved.setLayoutManager(
            ScrollingLinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )

        binding.rvInteriors.setLayoutManager(
            ScrollingLinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )

        binding.rvFocused.setLayoutManager(
            ScrollingLinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )

        binding.rvImagesBackgroundRemoved.setAdapter(showReplacedImagesAdapter)
        binding.rvInteriors.setAdapter(ShowReplacedImagesInteriorAdapter)
        binding.rvFocused.setAdapter(ShowReplacedImagesFocusedAdapter)
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

                            Utilities.savePrefrence(
                                this@KarviShowImagesActivity,
                                AppConstants.NO_OF_IMAGES,
                                imageListAfter.size.toString()
                            )
                            hideData(0)
                        } else if (dataList!![i].image_category.equals("Interior")) {
                            Category = dataList!![i].image_category
                            (imageListInterior as ArrayList).add(dataList!![i].output_image_lres_url)
                            (imageListWaterMark as ArrayList).add(dataList!![i].output_image_lres_wm_url)
                            (listHdQuality as ArrayList).add(dataList!![i].output_image_hres_url)

                            Utilities.savePrefrence(
                                this@KarviShowImagesActivity,
                                AppConstants.NO_OF_IMAGES,
                                imageListAfter.size.toString()
                            )
                            hideData(0)
                        } else if (dataList!![i].image_category.equals("Focus Shoot")) {
                            Category = dataList!![i].image_category
                            (imageListFocused as ArrayList).add(dataList!![i].output_image_lres_url)
                            (imageListWaterMark as ArrayList).add(dataList!![i].output_image_lres_wm_url)
                            (listHdQuality as ArrayList).add(dataList!![i].output_image_hres_url)

                            Utilities.savePrefrence(
                                this@KarviShowImagesActivity,
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

                            Utilities.savePrefrence(
                                this@KarviShowImagesActivity,
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
                    this@KarviShowImagesActivity,
                    "Server not responding!!!", Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun showImagesDialog(url: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_show_processed_images)

        val window: Window = dialog.getWindow()!!
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val carouselViewImages: ImageView = dialog.findViewById(R.id.ivProcessed)
        val ivCrossImages: ImageView = dialog.findViewById(R.id.ivCrossImages)


        ivCrossImages.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        Glide.with(this)
            .load(url)
            .into(carouselViewImages)

        dialog.show()
    }

}