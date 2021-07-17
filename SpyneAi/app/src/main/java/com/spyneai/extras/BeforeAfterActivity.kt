package com.spyneai.extras

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.activity.CameraActivity
import com.spyneai.adapter.BeforeAfterAdapter
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.ai.GifResponse
import com.spyneai.model.beforeafter.BeforeAfterResponse
import com.spyneai.model.beforeafter.Data
import com.spyneai.model.carreplace.CarBackgroundsResponse
import com.spyneai.model.channel.BackgroundsResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.ScrollingLinearLayoutManager
import com.spyneai.needs.Utilities
import com.spyneai.shoot.ui.ShootActivity
import com.spyneai.shoot.ui.ShootPortraitActivity
import com.spyneai.spyneaidemo.activity.camera2.Camera2DemoActivity
import com.spyneai.threesixty.ui.ThreeSixtyActivity
import kotlinx.android.synthetic.main.activity_before_after.*
import kotlinx.android.synthetic.main.activity_before_after.llBeforeAfters
import kotlinx.android.synthetic.main.activity_show_images.*
import kotlinx.android.synthetic.main.fragment_background.*
import kotlinx.android.synthetic.main.fragment_channel.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BeforeAfterActivity : AppCompatActivity() {
    var before = ""
    var after = ""
    var catName = ""
    var catId = ""

    private lateinit var beforeAfterList: ArrayList<Data>
    private lateinit var beforeAfterAdapter: BeforeAfterAdapter
    lateinit var gifList : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_before_after)

        setRecycler()
        setData()
        listeners()

        tvShootTS.setOnClickListener {
            val intent = Intent(this, ThreeSixtyActivity::class.java)
            intent.putExtra(AppConstants.CATEGORY_ID, catId)
            intent.putExtra(AppConstants.CATEGORY_NAME, catName)
            intent.putExtra(AppConstants.GIF_LIST, gifList)
            Utilities.savePrefrence(this, AppConstants.FROM, "BA")
            startActivity(intent)

        }
        if (intent.getStringExtra(AppConstants.CATEGORY_NAME) != null)
            catName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
        else
            catName = Utilities.getPreference(this, AppConstants.CATEGORY_NAME)!!
    }



    private fun setRecycler() {
        gifList = ArrayList<String>()
        Log.e("Token Mine", Utilities.getPreference(this, AppConstants.TOKEN_ID).toString())
        beforeAfterList = ArrayList<Data>()
        beforeAfterAdapter = BeforeAfterAdapter(this, beforeAfterList,
            object : BeforeAfterAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position cat", position.toString())
                }
            })

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        rvBeforeAfter.setLayoutManager(ScrollingLinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        ))

        rvBeforeAfter.setAdapter(beforeAfterAdapter)
        if (Utilities.isNetworkAvailable(this))
            //fetchBeforeAfter()
        else
            Toast.makeText(
                this,
                "Please check your internet connection",
                Toast.LENGTH_SHORT
            ).show()
    }

    private fun setData() {
        catId = intent.getStringExtra(AppConstants.CATEGORY_ID)!!
        catName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
        tvCategoryName.text = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
        tvCategoryNameSub.text = intent.getStringExtra(AppConstants.DESCRIPTION)!!

        Glide.with(this).load(
            AppConstants.BASE_IMAGE_URL +
                    intent.getStringExtra(AppConstants.IMAGE_URL)!!
        ).into(imgCategory)

        val colors = intArrayOf(
            Color.parseColor(intent.getStringExtra(AppConstants.COLOR)!!),
            Color.parseColor("#FFFFFF")
        )

        val gd = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors)
        gd.cornerRadius = 0f
        llCategory.background = gd
    }

    private fun listeners() {
        if (Utilities.getPreference(this, AppConstants.CATEGORY_NAME).equals("Automobiles")){
            tvShootFootwear.setOnClickListener(View.OnClickListener {
                val intent = Intent(this, ShootActivity::class.java)
                intent.putExtra(AppConstants.CATEGORY_ID, catId)
                intent.putExtra(AppConstants.CATEGORY_NAME, catName)
                intent.putExtra(AppConstants.GIF_LIST, gifList)
                Utilities.savePrefrence(this, AppConstants.FROM, "BA")
                startActivity(intent)
            })
        }
        else if (Utilities.getPreference(this, AppConstants.CATEGORY_NAME).equals("Footwear")){
            tvShootFootwear.setOnClickListener(View.OnClickListener {
                val intent = Intent(this, ShootPortraitActivity::class.java)
                intent.putExtra(AppConstants.CATEGORY_ID, catId)
                intent.putExtra(AppConstants.CATEGORY_NAME, catName)
                intent.putExtra(AppConstants.GIF_LIST, gifList)
                Utilities.savePrefrence(this, AppConstants.FROM, "BA")
                startActivity(intent)
            })
        }else if (Utilities.getPreference(this, AppConstants.CATEGORY_NAME).equals("Grocery")){
            tvShootFootwear.setOnClickListener(View.OnClickListener {
                val intent = Intent(this, CameraActivity::class.java)
                intent.putExtra(AppConstants.CATEGORY_ID, catId)
                intent.putExtra(AppConstants.CATEGORY_NAME, catName)
                intent.putExtra(AppConstants.GIF_LIST, gifList)
                Utilities.savePrefrence(this, AppConstants.FROM, "BA")
                startActivity(intent)
            })
        }
        else if(Utilities.getPreference(this, AppConstants.CATEGORY_NAME).equals("Grocery")){
            tvShootFootwear.setOnClickListener(View.OnClickListener {

                // Need new cam
                val intent = Intent(this, CameraActivity::class.java)
                intent.putExtra(AppConstants.CATEGORY_ID, catId)
                intent.putExtra(AppConstants.CATEGORY_NAME, catName)
                intent.putExtra(AppConstants.GIF_LIST, gifList)
                Utilities.savePrefrence(this, AppConstants.FROM, "BA")
                startActivity(intent)
            })

        }

        imgBackBF.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
    }

    private fun fetchBeforeAfter() {
        Utilities.showProgressDialog(this)
        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.getBeforeAfter(
            Utilities.getPreference(this, AppConstants.TOKEN_ID),
            intent.getStringExtra(AppConstants.CATEGORY_ID)!!
        )

        call?.enqueue(object : Callback<BeforeAfterResponse> {
            override fun onResponse(
                call: Call<BeforeAfterResponse>,
                response: Response<BeforeAfterResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful && response.body()!!.payload.data.size > 0) {
                    (beforeAfterList as ArrayList).clear()
                    (beforeAfterList as ArrayList).addAll(response.body()!!.payload.data)
                    llBeforeAfters.visibility = View.VISIBLE
                    Log.e("beforeafterlist", beforeAfterList.toString())
                }
                else{
                    llBeforeAfters.visibility = View.GONE
                }
                beforeAfterAdapter.notifyDataSetChanged()

                fetchBackgroundCars()



            }

            override fun onFailure(call: Call<BeforeAfterResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(
                    this@BeforeAfterActivity,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }




    private fun fetchBackgroundCars()
    {
        //  Utilities.showProgressDialog(this)

        val request = RetrofitClients.buildService(APiService::class.java)
        val call = request.getBackgroundCars()

        call?.enqueue(object : Callback<List<CarBackgroundsResponse>> {
            override fun onResponse(
                call: Call<List<CarBackgroundsResponse>>,
                response: Response<List<CarBackgroundsResponse>>
            ) {
                if (response.isSuccessful) {
                    if (!response.body().isNullOrEmpty() && response.body()?.size!! > 0) {
                        Utilities.setList(
                            this@BeforeAfterActivity, AppConstants.BACKGROUND_LIST_CARS,
                            response.body() as ArrayList
                        )

                        fetchGifsList()
                    } else {
                        Toast.makeText(
                            this@BeforeAfterActivity,
                            "Server not responding!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<CarBackgroundsResponse>>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(
                    this@BeforeAfterActivity,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun fetchGifsList() {
        gifList = ArrayList<String>()

        val request = RetrofitClients.buildService(APiService::class.java)

        val call = request.getGifsList()

        call?.enqueue(object : Callback<List<GifResponse>> {
            override fun onResponse(call: Call<List<GifResponse>>,
                                    response: Response<List<GifResponse>>) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    gifList = ArrayList<String>()
                    for (i in 0..response.body()!!.size-1)
                        gifList.add(response.body()!![i].url)
                    Utilities.hideProgressDialog()

                    Utilities.setList(
                        this@BeforeAfterActivity, AppConstants.GIF_LIST,
                        response.body() as ArrayList
                    )

                }
            }
            override fun onFailure(call: Call<List<GifResponse>>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(this@BeforeAfterActivity,
                    "Server not responding!!!", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}