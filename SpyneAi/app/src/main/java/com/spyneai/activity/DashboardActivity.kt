package com.spyneai.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.extras.OnboardTwoActivity
import com.spyneai.R
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.adapter.HomeFragment
import com.spyneai.extras.BeforeAfterActivity
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.model.categories.CategoriesResponse
import com.spyneai.model.categories.Data
import com.spyneai.model.shoot.CreateCollectionRequest
import com.spyneai.model.shoot.CreateCollectionResponse
import com.spyneai.model.shoot.UpdateShootCategoryRequest
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.synnapps.carouselview.ViewListener
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.view_custom.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DashboardActivity : AppCompatActivity() {
    var fragment: Fragment? = null
    var fragmentManager: FragmentManager? = null
    var fragmentTransaction: FragmentTransaction? = null

    lateinit var categoriesResponseList : ArrayList<Data>
    lateinit var categoriesAdapter : CategoriesDashboardAdapter
    lateinit var rv_categories : RecyclerView
    val sampleImages = intArrayOf(
        R.mipmap.w1,
        R.mipmap.w2,
        R.mipmap.w3,
        R.mipmap.w4
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        showCoachMarks()
        Utilities.savePrefrence(
                this,
                AppConstants.FRAME_SHOOOTS,
                ""
        )
        Utilities.savePrefrence(this,
                AppConstants.SKU_ID,
                "")
        setRecycler()

        if (Utilities.isNetworkAvailable(this))
            fetchCategories()
        else
            Toast.makeText(
                this,
                "Please check your internet connection",
                Toast.LENGTH_SHORT
            ).show()

        setCarosels()
        setFooters(0)
/*
        Utilities.savePrefrence(
            this,
            AppConstants.tokenId,
            "C1i19DFuH"
        )
*/

        finishAllBacks()
        listeners()
    }


    private fun setCarosels() {
        tvCompleted.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, CompletedProjectsActivity::class.java)
            startActivity(intent)
        })

        tvOngoing.setOnClickListener(View.OnClickListener {
            /* val intent = Intent(contexts, OngoingActivity::class.java)
            startActivity(intent)*/
            Toast.makeText(
                this,
                "Coming Soon !",
                Toast.LENGTH_SHORT
            ).show()

        })

        carouselView!!.setPageCount(sampleImages.size);
        carouselView.setViewListener(viewListener);

        tvHaveLook.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=LV9sgTxsIgI")
                )
            )
            Log.i("Video", "Video Playing....")
        })
    }

    var viewListener = object : ViewListener {
        override fun setViewForPosition(position: Int): View? {
            val customView: View = layoutInflater.inflate(R.layout.view_custom, null)
            customView.ivCarosel.setImageResource(sampleImages[position])
            return customView
        }
    }

    private fun setRecycler() {
        Log.e("Token Mine", Utilities.getPreference(this, AppConstants.tokenId).toString())
        categoriesResponseList = ArrayList<Data>()
        categoriesAdapter = CategoriesDashboardAdapter(this, categoriesResponseList,
            object : CategoriesDashboardAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position cat", position.toString())
                    if (position < 2)
                        setShoot(categoriesResponseList, position)
                    else
                        Toast.makeText(
                            this@DashboardActivity,
                            "Coming Soon !",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            })

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rvCategoriesDashboard.setLayoutManager(layoutManager)
        rvCategoriesDashboard.setAdapter(categoriesAdapter)

    }

    private fun fetchCategories() {
//        Utilities.showProgressDialog(this)
        categoriesResponseList.clear()

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.getCategories(Utilities.getPreference(this, AppConstants.tokenId))

        call?.enqueue(object : Callback<CategoriesResponse> {
            override fun onResponse(
                call: Call<CategoriesResponse>,
                response: Response<CategoriesResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    if (response.body()?.payload?.data?.size!! > 0) {
                        categoriesResponseList.addAll(response.body()?.payload?.data!!)
                    }

                    categoriesAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<CategoriesResponse>, t: Throwable) {
                Log.e("ok", "no way")
                Utilities.hideProgressDialog()
                Toast.makeText(
                    this@DashboardActivity,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setShoot(categoriesResponseList: ArrayList<Data>, position: Int) {
        Utilities.showProgressDialog(this)
        val createCollectionRequest = CreateCollectionRequest("Spyne Shoot");

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.createCollection(
            Utilities.getPreference(this, AppConstants.tokenId),
            createCollectionRequest
        )

        call?.enqueue(object : Callback<CreateCollectionResponse> {
            override fun onResponse(
                call: Call<CreateCollectionResponse>,
                response: Response<CreateCollectionResponse>
            ) {
                if (response.isSuccessful) {
                    Log.e("ok", response.body()?.payload?.data?.shootId.toString())
                    Utilities.savePrefrence(
                        this@DashboardActivity,
                        AppConstants.SHOOT_ID,
                        response.body()?.payload?.data?.shootId.toString()
                    )
                    setCategoryMap(response.body()?.payload?.data?.shootId.toString(), position)
                }
            }

            override fun onFailure(call: Call<CreateCollectionResponse>, t: Throwable) {
                Log.e("ok", "no way")

            }
        })
    }

    private fun setCategoryMap(shootId: String, position: Int) {

        val updateShootCategoryRequest = UpdateShootCategoryRequest(
            shootId,
            categoriesResponseList[position].catId,
            categoriesResponseList[position].displayName
        )

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.updateShootCategory(
            Utilities.getPreference(this, AppConstants.tokenId),
            updateShootCategoryRequest
        )

        call?.enqueue(object : Callback<CreateCollectionResponse> {
            override fun onResponse(
                call: Call<CreateCollectionResponse>,
                response: Response<CreateCollectionResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful) {
                    val intent = Intent(this@DashboardActivity, BeforeAfterActivity::class.java)
                    intent.putExtra(
                        AppConstants.CATEGORY_ID,
                        categoriesResponseList[position].catId
                    )
                    intent.putExtra(
                        AppConstants.CATEGORY_NAME,
                        categoriesResponseList[position].displayName
                    )
                    intent.putExtra(
                        AppConstants.IMAGE_URL,
                        categoriesResponseList[position].displayThumbnail
                    )
                    intent.putExtra(
                        AppConstants.DESCRIPTION,
                        categoriesResponseList[position].description
                    )
                    intent.putExtra(AppConstants.COLOR, categoriesResponseList[position].colorCode)
                    startActivity(intent)
                    Log.e(
                        "Category map",
                        categoriesResponseList[position].catId + " " + response.body()!!.msgInfo.msgDescription
                    )
                }
            }

            override fun onFailure(call: Call<CreateCollectionResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Log.e("ok", "no way")

            }
        })
    }

    private fun showCoachMarks() {

    }
    //var viewListener = ViewListener { layoutInflater.inflate(R.layout.view_custom, null) }
    private fun finishAllBacks() {
        SplashActivity().finish()
        OnboardOneActivity().finish()
        OnboardTwoActivity().finish()
        OnboardThreeActivity().finish()
        SignInActivity().finish()
    }

    private fun listeners() {
        ivClicks.setOnClickListener(View.OnClickListener {
            val intent = Intent(applicationContext, CategoriesActivity::class.java)
            startActivity(intent)
        })

        tvHome.setOnClickListener(View.OnClickListener {
            setFooters(0)
        })

        tvNotifications.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                this,
                "Coming Soon!",
                Toast.LENGTH_SHORT
            ).show()
        })

        tvOrders.setOnClickListener(View.OnClickListener {
           /* fragment = OrdersFragment(this)
            fragmentManager = supportFragmentManager
            fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction!!.replace(R.id.fragment_container_view, fragment!!)
            fragmentTransaction!!.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            fragmentTransaction!!.commit()
            setFooters(2)*/
         //   setFooters(2)

            Toast.makeText(
                    this,
                    "Coming Soon !",
                    Toast.LENGTH_SHORT
            ).show()
        })

        tvProfile.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                this,
                "Coming Soon !",
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    fun setFooters(positionsClicked : Int) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            when (positionsClicked) {
                0 -> {
                    tvHome.setTextColor(getColor(R.color.primary))
                    tvNotifications.setTextColor(getColor(R.color.black))
                    tvOrders.setTextColor(getColor(R.color.black))
                    tvProfile.setTextColor(getColor(R.color.black))

                    tvHome.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.homes, 0, 0);
                    tvNotifications.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.bell, 0, 0);
                    tvOrders.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.order, 0, 0);
                    tvProfile.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.profile, 0, 0);
                }
                1 -> {
                    tvHome.setTextColor(getColor(R.color.black))
                    tvNotifications.setTextColor(getColor(R.color.primary))
                    tvOrders.setTextColor(getColor(R.color.black))
                    tvProfile.setTextColor(getColor(R.color.black))

                    tvHome.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.home, 0, 0);
                    tvNotifications.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.bells, 0, 0);
                    tvOrders.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.order, 0, 0);
                    tvProfile.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.profile, 0, 0);
                }
                2 -> {
                    tvHome.setTextColor(getColor(R.color.black))
                    tvNotifications.setTextColor(getColor(R.color.black))
                    tvOrders.setTextColor(getColor(R.color.primary))
                    tvProfile.setTextColor(getColor(R.color.black))

                    tvHome.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.home, 0, 0);
                    tvNotifications.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.bell, 0, 0);
                    tvOrders.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.orders, 0, 0);
                    tvProfile.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.profile, 0, 0);
                }
                3 -> {
                    tvHome.setTextColor(getColor(R.color.black))
                    tvNotifications.setTextColor(getColor(R.color.black))
                    tvOrders.setTextColor(getColor(R.color.black))
                    tvProfile.setTextColor(getColor(R.color.primary))

                    tvHome.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.home, 0, 0);
                    tvNotifications.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.bell, 0, 0);
                    tvOrders.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.order, 0, 0);
                    tvProfile.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.profiles, 0, 0);
                }
            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true);
        System.exit(1);
    }

}