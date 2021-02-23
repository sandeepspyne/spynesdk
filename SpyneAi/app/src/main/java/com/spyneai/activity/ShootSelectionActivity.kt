package com.spyneai.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.adapter.CarBackgroundAdapter
import com.spyneai.adapter.ChannelsAdapter
import com.spyneai.adapter.PhotosAdapter
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.activity.CameraPreviewActivity
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.carreplace.CarBackgroundsResponse
import com.spyneai.model.channel.ChannelsResponse
import com.spyneai.model.sku.Photos
import com.spyneai.model.sku.SkuResponse
import com.spyneai.model.upload.PreviewResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_camera_preview.*
import kotlinx.android.synthetic.main.activity_edit_sku.*
import kotlinx.android.synthetic.main.activity_edit_sku.tvSkuName
import kotlinx.android.synthetic.main.activity_preview.*
import kotlinx.android.synthetic.main.activity_shoot_selection.rvChannels
import kotlinx.android.synthetic.main.activity_shoot_selection.*
import kotlinx.android.synthetic.main.fragment_channel.view.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ShootSelectionActivity : AppCompatActivity() {
    private lateinit var photsAdapter: PhotosAdapter
    private lateinit var photoList: List<Photos>

    lateinit var channelList : List<ChannelsResponse>
    lateinit var channelAdapter: ChannelsAdapter
    private var currentPOsition : Int = 0

    lateinit var carList : List<CarBackgroundsResponse>
    lateinit var carAdapter: CarBackgroundAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shoot_selection)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        currentPOsition = intent.getIntExtra(AppConstants.POSITION,0)!!

        fetchSkuData()
        /*  if (!Utilities.getPreference(this,AppConstants.CATEGORY_NAME)!!.equals("Automobiles")) {
              setRecycler()
          }
          else{*/
        setRecyclerCar()
        // }
    }

    private fun fetchSkuData() {
        photoList = ArrayList<Photos>()
        val request = RetrofitClient.buildService(APiService::class.java)

        val call = request.getSkuDetails(
                Utilities.getPreference(this, AppConstants.tokenId),
                intent.getStringExtra(AppConstants.SKU_ID)!!)

        call?.enqueue(object : Callback<SkuResponse> {
            override fun onResponse(call: Call<SkuResponse>,
                                    response: Response<SkuResponse>
            ) {
                if (response.isSuccessful){
                    tvSkuNames.setText(response.body()?.payload!!.data.displayName)
                    if (response.body()?.payload!!.data.photos.size > 0)
                    {
                        (photoList as ArrayList).clear()
                        (photoList as ArrayList).addAll(response.body()?.payload!!.data.photos as ArrayList)
                    }
                    listeners()
                }
            }
            override fun onFailure(call: Call<SkuResponse>, t: Throwable) {
                Toast.makeText(this@ShootSelectionActivity,
                        "Server not responding!!!", Toast.LENGTH_SHORT).show()
            }
        })
    }


    //Shoe flow
    private fun setRecycler() {
        channelList = ArrayList<ChannelsResponse>()
        channelAdapter = ChannelsAdapter(this, channelList as ArrayList<ChannelsResponse>,
                object : ChannelsAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int) {
                        Log.e("position preview", position.toString())
                        showPreview()
                    }
                })
        val layoutManager: RecyclerView.LayoutManager =
                LinearLayoutManager(this,
                        LinearLayoutManager.HORIZONTAL, false)
        rvChannels.setLayoutManager(layoutManager)
        rvChannels.setAdapter(channelAdapter)
        fetchChannels()
    }

    private fun fetchChannels() {
        (channelList as ArrayList).clear()
        (channelList as ArrayList).addAll(Utilities.getList(this,AppConstants.CHANNEL_LIST)!!)
        if (channelList.size!! > 0) {
            rvChannels.visibility = View.VISIBLE
            tvMarketPlace.visibility = View.GONE
        } else {
            rvChannels.visibility = View.GONE
            tvMarketPlace.visibility = View.VISIBLE
        }

        channelAdapter.notifyDataSetChanged()
    }

    //Car flow
    private fun setRecyclerCar() {
        carList = ArrayList<CarBackgroundsResponse>()
        carAdapter = CarBackgroundAdapter(this!!, carList as ArrayList<CarBackgroundsResponse>,0,
                object : CarBackgroundAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int) {
                        Log.e("position preview", position.toString())
                        Utilities.savePrefrence(this@ShootSelectionActivity
                                ,AppConstants.IMAGE_ID,carList[position].imageId.toString())

                        if (position == 0)
                        {
                            Glide.with(this@ShootSelectionActivity).load(photoList[currentPOsition].displayThumbnail).into(ivImage)
                        }
                        else {
                            showPreviewCar()
                        }
                        carAdapter.notifyDataSetChanged()

                    }
                })
        val layoutManager: RecyclerView.LayoutManager =
                LinearLayoutManager(this,
                        LinearLayoutManager.HORIZONTAL, false)
        rvChannels.setLayoutManager(layoutManager)
        rvChannels.setAdapter(carAdapter)
        fetchcars()
    }

    private fun fetchcars() {
        (carList as ArrayList).clear()
        val carBackgroundLists = CarBackgroundsResponse(0,
                "https://spyne-cliq.s3.ap-south-1.amazonaws.com/spyne-cliq/default/image+2.png",
                0,"")

        (carList as ArrayList).add(carBackgroundLists)
        (carList as ArrayList).addAll(Utilities.getListBackgroundsCar(this, AppConstants.BACKGROUND_LIST_CARS)!!)

        if (carList.size!! > 0) {
            rvChannels.visibility = View.VISIBLE
            tvMarketPlace.visibility = View.GONE
        } else {
            rvChannels.visibility = View.GONE
            tvMarketPlace.visibility = View.VISIBLE
        }
        carAdapter.notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    private fun listeners() {

        if (photoList.size > 0) {
            Glide.with(this).load(photoList[currentPOsition].displayThumbnail).into(ivImage)
            tvSkusCount.setText((currentPOsition + 1).toString() + "/" + photoList.size)
        }

        ivBackWard.setOnClickListener(View.OnClickListener {
            if (photoList.size > 0)
            {
                if (currentPOsition >0) {
                    Glide.with(this).load(photoList[currentPOsition - 1].displayThumbnail)
                            .into(ivImage)
                    currentPOsition = currentPOsition - 1
                }
                else if (currentPOsition  ==  0) {
                    Glide.with(this).load(photoList[currentPOsition].displayThumbnail)
                            .into(ivImage)
                    currentPOsition = 0
                }
            }
            tvSkusCount.setText((currentPOsition + 1).toString() + "/" + photoList.size)

        })

        ivForeward.setOnClickListener(View.OnClickListener {
            if (photoList.size > 0 )
            {
                if (currentPOsition < photoList.size-1) {
                    Glide.with(this).load(
                            photoList[currentPOsition + 1].displayThumbnail)
                            .into(ivImage)
                    currentPOsition = currentPOsition + 1
                }
                else if (currentPOsition == photoList.size-1) {
                    Glide.with(this).load(
                            photoList[currentPOsition].displayThumbnail)
                            .into(ivImage)
                    currentPOsition = photoList.size-1
                }

            }
            tvSkusCount.setText((currentPOsition + 1).toString() + "/" + photoList.size)

        })
        ivBack.setOnClickListener(View.OnClickListener {
            finish()
        })

        tvOrderNow.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            //intent.putExtra(AppConstants.POSITION,position)
            //intent.putExtra(AppConstants.SKU_ID,skuList[position].skuId)
            startActivity(intent)
        })

    }


    fun showPreview() {
        Utilities.showProgressDialog(this)
        val request = RetrofitClients.buildService(APiService::class.java)
        val img_url = RequestBody.create(MultipartBody.FORM,photoList[currentPOsition].displayThumbnail)

        val call = request.previewPhoto(img_url)
        //val imgPreview = findViewById<ImageView>(R.id.imgPreview)
        call?.enqueue(object : Callback<PreviewResponse> {
            override fun onResponse(
                    call: Call<PreviewResponse>,
                    response: Response<PreviewResponse>) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    if (response.body() != null)
                        Glide.with(this@ShootSelectionActivity)
                                .load(response.body()!!.url)
                                .into(ivImage)
                    Log.e("Respo Image Preview ", response.body().toString())
                }
            }

            override fun onFailure(call: Call<PreviewResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(this@ShootSelectionActivity,
                        "Some issue in image...",
                        Toast.LENGTH_SHORT).show()
                Log.e("Respo Image ", "Image error")
            }
        })
    }

    fun showPreviewCar() {
        Glide.with(this).load(photoList[currentPOsition].displayThumbnail).into(ivImage)

        Utilities.showProgressDialog(this)
        val img_url = RequestBody.create(MultipartBody.FORM,photoList[currentPOsition].displayThumbnail)

        val bg_replacement_backgound_id = RequestBody.create(MultipartBody.FORM, Utilities.getPreference(this,AppConstants.IMAGE_ID)!!)

        var front : String = "front"
        val car_bg_replacement_angle = RequestBody.create(MultipartBody.FORM, front  )

        val request = RetrofitClients.buildService(APiService::class.java)

        val call = request.previewPhotoCar( bg_replacement_backgound_id,img_url,car_bg_replacement_angle)
        //val imgPreview = findViewById<ImageView>(R.id.imgPreview)
        call?.enqueue(object : Callback<PreviewResponse> {
            override fun onResponse(
                    call: Call<PreviewResponse>,
                    response: Response<PreviewResponse>) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    if (response.body() != null)
                        Glide.with(this@ShootSelectionActivity)
                                .load(response.body()!!.url)
                                .into(ivImage)
                    Log.e("Respo Image Preview ", response.body().toString())
                }
            }

            override fun onFailure(call: Call<PreviewResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(this@ShootSelectionActivity,
                        "Some issue in image...",
                        Toast.LENGTH_SHORT).show()

                Log.e("Respo Image ", "Image error")
            }
        })

    }


}