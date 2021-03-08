package com.spyneai.activity

import UploadPhotoResponse
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.adapter.*
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.MyLookup
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.carreplace.CarBackgroundsResponse
import com.spyneai.model.channel.ChannelsResponse
import com.spyneai.model.nextsku.SkuRequest
import com.spyneai.model.order.MarketPlace
import com.spyneai.model.order.PlaceOrderResponse
import com.spyneai.model.order.Sku
import com.spyneai.needs.AppConstants
import com.spyneai.needs.ImageFilePath
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.fragment_background.view.*
import kotlinx.android.synthetic.main.fragment_channel.view.*
import kotlinx.android.synthetic.main.fragment_channel.view.rvChannel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class OrderActivity : AppCompatActivity() {
    private lateinit var channelAdapter: AddChannelAdapter
    private lateinit var channelList: List<MarketPlace>
    private lateinit var skuAdapter: SkuAdapter
    private lateinit var skuList: List<Sku>
    private lateinit var skuName : String

    private var tracker: SelectionTracker<Long>? = null

    lateinit var carBackgroundList : ArrayList<CarBackgroundsResponse>
    lateinit var carbackgroundsAdapter: CarBackgroundAdapter

    private val SELECT_PICTURE = 1
    private var savedUri: Uri? = null
    private var selectedImagePath: String? = null
    private lateinit var photoFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setBackgroundsCar()
        setChannels()
        setSkus()
        listeners()
        if (Utilities.getPreference(this,AppConstants.CATEGORY_NAME)!!.equals("Automobiles")) {
            cardChannels.visibility = View.GONE
            cardBackgroundCars.visibility = View.VISIBLE
        }
        else{
            cardChannels.visibility = View.VISIBLE
            cardBackgroundCars.visibility = View.GONE
        }



        if(savedInstanceState != null)
            tracker?.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker?.onSaveInstanceState(outState)
    }

    private fun setChannels() {
        channelList = ArrayList<MarketPlace>()
        channelAdapter = AddChannelAdapter(this, channelList as ArrayList<MarketPlace>,
            object : AddChannelAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position preview", position.toString())
                }
            })
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        rvChannels.setLayoutManager(layoutManager)
        rvChannels.setAdapter(channelAdapter)

/*
        tracker = SelectionTracker.Builder<Long>(
            "selection-1",
            rvChannels,
            StableIdKeyProvider(rvChannels),
            MyLookup(rvChannels),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()
*/


    }
    private fun setBackgroundsCar() {
        carBackgroundList = ArrayList<CarBackgroundsResponse>()
        carbackgroundsAdapter = CarBackgroundAdapter(this,
            carBackgroundList as ArrayList<CarBackgroundsResponse>,
            0,
            object : CarBackgroundAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position preview", position.toString())
                }
            })
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false)
        rvBackgroundsCar.setLayoutManager(layoutManager)
        rvBackgroundsCar.setAdapter(carbackgroundsAdapter)

        fetchBackgrounds()
    }

    private fun fetchBackgrounds() {
        (carBackgroundList as ArrayList).clear()
        val carBackgroundLists = CarBackgroundsResponse(0,
            "https://spyne-cliq.s3.ap-south-1.amazonaws.com/spyne-cliq/default/image+2.png",
            0,"")

        (carBackgroundList as ArrayList).add(carBackgroundLists)

        (carBackgroundList as ArrayList).addAll(
            Utilities.getListBackgroundsCar(
                this,AppConstants.BACKGROUND_LIST_CARS)!!)
        carbackgroundsAdapter.notifyDataSetChanged()
    }

    private fun setSkus() {
        skuList = ArrayList<Sku>()
        skuAdapter = SkuAdapter(this, skuList,
            object : SkuAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position preview", position.toString())
                }
            })
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvSkus.setLayoutManager(layoutManager)
        rvSkus.setAdapter(skuAdapter)

        //   channelAdapter.setTracker(tracker)
        fetchSkus()
    }

    private fun fetchSkus() {
        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.getOrderList(
            Utilities.getPreference(this,AppConstants.tokenId),
            Utilities.getPreference(this,AppConstants.SHOOT_ID),
            Utilities.getPreference(this,AppConstants.SKU_ID))

        call?.enqueue(object : Callback<PlaceOrderResponse> {
            override fun onResponse(call: Call<PlaceOrderResponse>,
                                    response: Response<PlaceOrderResponse>) {
                if (response.isSuccessful){
                    if (response.body() != null && response.body()?.payload != null) {
                        if (response.body()?.payload!!.data.numberOfSkus > 0) {
                            (skuList as ArrayList).clear()
                            (channelList as ArrayList).clear()
                            (skuList as ArrayList).addAll(response.body()?.payload!!.data.skus as ArrayList)
                            (channelList as ArrayList).addAll(response.body()?.payload!!.data.marketPlace as ArrayList)
                            skuName = response.body()?.payload!!.data.skus[response.body()?.payload!!.data.skus.size - 1].displayName
                        }
                    }
                    if (skuList.size > 0)
                        skuAdapter.notifyDataSetChanged()
                    if (channelList.size > 0)
                        channelAdapter.notifyDataSetChanged()
                }
            }
            override fun onFailure(call: Call<PlaceOrderResponse>, t: Throwable) {
                Toast.makeText(this@OrderActivity,
                    "Server not responding!!!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun listeners() {
        tvAddSku.setOnClickListener(View.OnClickListener {
            fetchNextSkus()
        })

        ivAddSku.setOnClickListener(View.OnClickListener {
            fetchNextSkus()
        })
        imgSku.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@OrderActivity, ChannelActivity::class.java)
            startActivity(intent)
        })
        imgBack.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
        tvPreviewOrder.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@OrderActivity, PreviewActivity::class.java)
            startActivity(intent)
        })
        tvStartOrder.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@OrderActivity, PreviewActivity::class.java)
            startActivity(intent)
        })
        btnUploadLogo.setOnClickListener(View.OnClickListener {
            val checkSelfPermission = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                //Requests permissions to be granted to this application at runtime
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
                )
            } else {
                defaultSet()
            }
        })

    }

    private fun fetchNextSkus() {
        val request = RetrofitClient.buildService(APiService::class.java)
        val skuRequest = SkuRequest(skuName,
            Utilities.getPreference(this,AppConstants.SHOOT_ID).toString(),
            Utilities.getPreference(this,AppConstants.SKU_ID).toString())

        val call = request.getNextShoot(Utilities.getPreference(this,AppConstants.tokenId),skuRequest )

        call?.enqueue(object : Callback<UploadPhotoResponse> {
            override fun onResponse(call: Call<UploadPhotoResponse>,
                                    response: Response<UploadPhotoResponse>) {
                if (response.isSuccessful){
                    val intent = Intent(this@OrderActivity, CameraActivity::class.java)

                    intent.putExtra(AppConstants.FRAME, response.body()?.payload!!.data.currentFrame)
                    intent.putExtra(AppConstants.TOTAL_FRAME, response.body()?.payload!!.data.totalFrames)

                    Utilities.savePrefrence(this@OrderActivity,AppConstants.SHOOT_ID,response.body()?.payload!!.data.shootId)
                    Utilities.savePrefrence(this@OrderActivity,AppConstants.SKU_NAME,response.body()?.payload!!.data.skuName)
                    Utilities.savePrefrence(this@OrderActivity,AppConstants.SKU_ID,response.body()?.payload!!.data.skuId)
                    startActivity(intent)
                }
            }
            override fun onFailure(call: Call<UploadPhotoResponse>, t: Throwable) {
                Toast.makeText(this@OrderActivity,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        var intents : Intent? = null
        if (intent.getStringExtra(AppConstants.FROM_ACTIVITY) != null) {
            //intents = Intent(this@OrderActivity, DashboardActivity::class.java)
            finish()
        }
        else{
            intents = Intent(this@OrderActivity, DashboardActivity::class.java)
            startActivity(intents)
            finish()
        }
    }

    private fun defaultSet() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select Picture"
            ), SELECT_PICTURE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                val selectedImageUri: Uri = data!!.getData()!!
                selectedImagePath = ImageFilePath.getPath(this, data.getData()!!);
                photoFile = File(selectedImagePath)
                savedUri = Uri.fromFile(photoFile)

                //  photoFilePath = compressFileFromBitmap()

                val myBitmapLogo = BitmapFactory.decodeFile(photoFile.absolutePath)
                ivLogo.setImageBitmap(myBitmapLogo)

                cardLogo.visibility = View.VISIBLE
                ivCorners.visibility = View.VISIBLE

                Utilities.savePrefrence(
                    this,
                    AppConstants.LOGO_FILE,
                    photoFile.toString()
                )
                ivCorners.performClick()
            }
        }
    }
}