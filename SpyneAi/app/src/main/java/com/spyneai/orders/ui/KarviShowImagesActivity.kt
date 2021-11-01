package com.spyneai.orders.ui

import android.app.Dialog
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.credits.model.ReviewHolder
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.ActivityKarviShowImagesBinding
import com.spyneai.gotoHome
import com.spyneai.isMagnatoMeterAvailable
import com.spyneai.needs.AppConstants
import com.spyneai.needs.ScrollingLinearLayoutManager
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.ProcessedImage
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.orders.ui.adapter.KarviImagesAdapter
import com.spyneai.processedimages.ui.data.ProcessedViewModel
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.dialogs.NoMagnaotoMeterDialog

class KarviShowImagesActivity : AppCompatActivity() {

    var shootId = ""
    private lateinit var binding: ActivityKarviShowImagesBinding
    lateinit var builder: NotificationCompat.Builder
    lateinit var imageList: List<String>
    lateinit var imageListAfter: List<String>
    lateinit var imageListInterior: List<String>
    lateinit var imageListFocused: List<String>
    val processdImagesListInterior = ArrayList<ProcessedImage>()
    val processdImagesListFocused = ArrayList<ProcessedImage>()
    val processdImagesList = ArrayList<ProcessedImage>()

    lateinit var imageListWaterMark: ArrayList<String>
    lateinit var listHdQuality: ArrayList<String>
    var catName: String = ""

    private lateinit var showReplacedImagesAdapter: KarviImagesAdapter
    private lateinit var ShowReplacedImagesInteriorAdapter: KarviImagesAdapter
    private lateinit var ShowReplacedImagesFocusedAdapter: KarviImagesAdapter

    lateinit var Category: String
    lateinit var viewModel : ProcessedViewModel
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityKarviShowImagesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this, ViewModelFactory()).get(ProcessedViewModel::class.java)
        viewModel.skuId = Utilities.getPreference(this, AppConstants.SKU_ID)!!

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setBulkImages()

        setListeners()

        if (intent.getStringExtra(AppConstants.CATEGORY_NAME) != null)
            catName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
        else
            catName = Utilities.getPreference(this, AppConstants.CATEGORY_NAME)!!

        observeSkuData()

        binding.tvReshoot.setOnClickListener {
            val imagesResponse = (viewModel.imagesOfSkuRes.value as Resource.Success).value

            val element = imagesResponse.data.firstOrNull {
                it.overlayId == null
            }

            if (element == null)
                viewModel.reshoot.value = true
            else
                Toast.makeText(this,"Reshoot not allowed for old shoots",Toast.LENGTH_LONG).show()
        }

    }

    private fun observeSkuData() {

        viewModel.imagesOfSkuRes.observe(this,{
            when(it){
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    var dataList: List<ImagesOfSkuRes.Data> = it.value.data

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
                            processdImagesList.add(ProcessedImage(dataList!![i].output_image_lres_url))

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
                            processdImagesListInterior.add(ProcessedImage(dataList!![i].output_image_lres_url))


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
                            processdImagesListFocused.add(ProcessedImage(dataList!![i].output_image_lres_url))

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
                            processdImagesList.add(ProcessedImage(dataList!![i].output_image_lres_url))

                            Utilities.savePrefrence(
                                this@KarviShowImagesActivity,
                                AppConstants.NO_OF_IMAGES,
                                imageListAfter.size.toString()
                            )
                            hideData(1)
                        }
                    }

                    showReplacedImagesAdapter.notifyDataSetChanged()
                    ShowReplacedImagesInteriorAdapter.notifyDataSetChanged()
                    ShowReplacedImagesFocusedAdapter.notifyDataSetChanged()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it){fetchBulkUpload()}
                }
            }
        })
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
            if (isMagnatoMeterAvailable()){
                startShoot()
            }else {
                NoMagnaotoMeterDialog().show(supportFragmentManager,"NoMagnaotoMeterDialog")
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
            processdImagesList,
            object : KarviImagesAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    showImagesDialog(listHdQuality[position])
                    Log.e("position preview", position.toString())
                }
            })

        ShowReplacedImagesInteriorAdapter = KarviImagesAdapter(this,
            processdImagesListInterior,
            object : KarviImagesAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    //   showImagesDialog(position)
                    Log.e("position preview", position.toString())
                }
            })


        ShowReplacedImagesFocusedAdapter = KarviImagesAdapter(this,
            processdImagesListFocused,
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

        shootId = Utilities.getPreference(this, AppConstants.SKU_ID)!!

        getSkuImages()

    }

    private fun getSkuImages() {
        // Utilities.showProgressDialog(requireContext())

        viewModel.getImagesOfSku(
            Utilities.getPreference(this,AppConstants.AUTH_KEY).toString(),
            viewModel.skuId!!
        )
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