package com.spyneai.activity

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.adapter.BackgroundColourAdapter
import com.spyneai.adapter.MarketplacesAdapter
import com.spyneai.adapter.PhotosAdapter
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.carreplace.CarBackgroundsResponse
import com.spyneai.model.channel.BackgroundsResponse
import com.spyneai.model.channel.ChannelsResponse
import com.spyneai.model.marketplace.FootwearMarketplaceResponse
import com.spyneai.model.sku.Photos
import com.spyneai.model.skumap.UpdateSkuResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_generate_gif.*
import kotlinx.android.synthetic.main.activity_generate_gif.ivBackGif
import kotlinx.android.synthetic.main.activity_generate_gif.rvBackgroundsCars
import kotlinx.android.synthetic.main.activity_generate_gif.tvGenerateGif
import kotlinx.android.synthetic.main.activity_genrate_marketplace.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class GenrateMarketplaceActivity : AppCompatActivity() {

    private lateinit var photsAdapter: PhotosAdapter
    private lateinit var photoList: List<Photos>

    lateinit var imageList : List<String>
    public lateinit var imageFileList : ArrayList<File>
    public lateinit var imageFileListFrames : ArrayList<Int>

    private var currentPOsition : Int = 0
    lateinit var marketplacesList : ArrayList<FootwearMarketplaceResponse>
    lateinit var backgroundColourList : ArrayList<FootwearMarketplaceResponse>
    lateinit var marketplacesAdapter: MarketplacesAdapter
    lateinit var backgroundColourAdapter: BackgroundColourAdapter
    var backgroundSelect : String = ""
    var catName = "Footwear"
    var marketplacePosition = Int
    var backgroundPosition : Int = 0

    var totalImagesToUPload : Int = 0
    var totalImagesToUPloadIndex : Int = 0
    lateinit var gifList : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genrate_marketplace)
        fetchMarketplaces()
        setBackgroundColour()
        setMarketplaces()
        listeners()

        imageFileList = ArrayList<File>()
        imageFileListFrames = ArrayList<Int>()

        //Get Intents

        imageFileList.addAll(intent.getParcelableArrayListExtra(AppConstants.ALL_IMAGE_LIST)!!)
        imageFileListFrames.addAll(intent.getIntegerArrayListExtra(AppConstants.ALL_FRAME_LIST)!!)
        totalImagesToUPload = imageFileList.size
    }

    private fun fetchMarketplaces(){

        // Utilities.showProgressDialog(this)
            val request = RetrofitClients.buildService(APiService::class.java)
            val call = request.getChannelsList(catName)

            call?.enqueue(object : Callback<List<ChannelsResponse>> {
                override fun onResponse(
                    call: Call<List<ChannelsResponse>>,
                    response: Response<List<ChannelsResponse>>
                ) {
                    // Utilities.hideProgressDialog()
                    if (response.isSuccessful) {
                        if (!response.body().isNullOrEmpty() && response.body()?.size!! > 0) {
                            Utilities.setList(
                                this@GenrateMarketplaceActivity, AppConstants.CHANNEL_LIST,
                                response.body() as ArrayList
                            )
                        } else {
                            Utilities.hideProgressDialog()
                            Toast.makeText(
                                this@GenrateMarketplaceActivity,
                                "Server not responding!!!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<List<ChannelsResponse>>, t: Throwable) {
                    Utilities.hideProgressDialog()
                    Toast.makeText(
                        this@GenrateMarketplaceActivity,
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }


    private fun setBackgroundColour(){
        marketplacesList = ArrayList<FootwearMarketplaceResponse>()

        backgroundColourAdapter = BackgroundColourAdapter(this,
            marketplacesList as ArrayList<FootwearMarketplaceResponse>, 0,
            object : BackgroundColourAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position preview", position.toString())

                    backgroundPosition = position


                    //showPreviewCar()
                }
            })
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false)
        rvBackgroundsColour.setLayoutManager(layoutManager)
        rvBackgroundsColour.setAdapter(backgroundColourAdapter)



    }

    private fun setMarketplaces() {
        marketplacesList = ArrayList<FootwearMarketplaceResponse>()
//        gifList = ArrayList<String>()
//        gifList.addAll(intent.getParcelableArrayListExtra(AppConstants.BACKGROUND_LIST)!!)

        marketplacesAdapter = MarketplacesAdapter(this,
            marketplacesList as ArrayList<FootwearMarketplaceResponse>, 0,
            object : MarketplacesAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position preview", position.toString())
                    //if (position<carBackgroundList.size)
                    backgroundSelect  = marketplacesList[position].market_id.toString()
                    marketplacesAdapter.notifyDataSetChanged()

                    if (backgroundPosition==0){
                        Glide.with(this@GenrateMarketplaceActivity) // replace with 'this' if it's in activity
                            .load(marketplacesList[position].sample_image_1)
                            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                            .into(ivSampleOutput)
                    }else{
                        Glide.with(this@GenrateMarketplaceActivity) // replace with 'this' if it's in activity
                            .load(marketplacesList[position].sample_image_2)
                            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                            .into(ivSampleOutput)
                    }


                    //showPreviewCar()
                }
            })
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false)
        rvBackgroundsCars.setLayoutManager(layoutManager)
        rvBackgroundsCars.setAdapter(marketplacesAdapter)

        fetchBackgrounds()
    }


    private fun fetchBackgrounds() {
        (marketplacesList as ArrayList).clear()
        (marketplacesList as ArrayList).addAll(
            Utilities.getListMarketplaces(
                this, AppConstants.CHANNEL_LIST)!!)

        marketplacesAdapter.notifyDataSetChanged()

//        backgroundSelect  = marketplacesList[0].imageId.toString()

//        Glide.with(this@GenrateMarketplaceActivity) // replace with 'this' if it's in activity
//            .load(gifList[0])
//            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
//            .into(imageViewGif)
    }

    private fun listeners() {
        Log.e("Generate  SKU",
            Utilities.getPreference(this,
                AppConstants.SKU_NAME)!!)
        tvGenerateGif.setOnClickListener(View.OnClickListener {
            if (Utilities.isNetworkAvailable(this))
            {
                val intent = Intent(this@GenrateMarketplaceActivity,
                    TimerActivity::class.java)
                intent.putExtra(AppConstants.BG_ID,backgroundSelect)
                intent.putExtra(AppConstants.ALL_IMAGE_LIST, imageFileList)
                intent.putExtra(AppConstants.ALL_FRAME_LIST, imageFileListFrames)
//                intent.putExtra(AppConstants.GIF_LIST, gifList)
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(this,
                    "No internet Connection , Please Try Again! ",
                    Toast.LENGTH_LONG).show()
            }
        })

        ivBackGif.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

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
            Utilities.savePrefrence(this@GenrateMarketplaceActivity, AppConstants.SHOOT_ID, "")
            Utilities.savePrefrence(this@GenrateMarketplaceActivity, AppConstants.CATEGORY_ID, "")
            Utilities.savePrefrence(this@GenrateMarketplaceActivity, AppConstants.PRODUCT_ID, "")
            Utilities.savePrefrence(this@GenrateMarketplaceActivity, AppConstants.SKU_NAME, "")
            Utilities.savePrefrence(this@GenrateMarketplaceActivity, AppConstants.SKU_ID, "")


            val updateSkuResponseList = ArrayList<UpdateSkuResponse>()
            updateSkuResponseList.clear()

            Utilities.setList(
                this@GenrateMarketplaceActivity,
                AppConstants.FRAME_LIST, updateSkuResponseList
            )
            val intent = Intent(this, DashboardActivity::class.java)
            dialog.dismiss()

            startActivity(intent)
            finish()

        })
        dialogButtonNo.setOnClickListener(View.OnClickListener { dialog.dismiss() })
        dialog.show()
    }

}

