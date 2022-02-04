package com.spyneai.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.adapter.CategoriesAdapter
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.base.BaseActivity
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.dashboard.ui.DashboardViewModel
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.ActivityCategoriesBinding
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.ui.StartShootActivity
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.shoot.ui.base.ShootPortraitActivity
import com.spyneai.showConnectionChangeView
import kotlinx.android.synthetic.main.activity_categories.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoriesActivity : BaseActivity(){
    lateinit var binding: ActivityCategoriesBinding
    private var viewModel: DashboardViewModel? = null
    lateinit var categoriesResponseList : ArrayList<NewCategoriesResponse.Category>
    lateinit var categoriesAdapter : CategoriesAdapter
    lateinit var rv_categories : RecyclerView
    var before = ""
    var after = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        viewModel = ViewModelProvider(this, ViewModelFactory()).get(DashboardViewModel::class.java)

        setPreferences()
        setListeners()
        setRecycler()
        fetchCategories()
        observeCategories()
    }

    override fun onConnectionChange(isConnected: Boolean) {
        showConnectionChangeView(isConnected,binding.root)
    }

    private fun setPreferences() {
        Utilities.savePrefrence(this,AppConstants.SHOOT_ID,"")
        Utilities.savePrefrence(this,AppConstants.SKU_NAME,"")
        Utilities.savePrefrence(this,AppConstants.SKU_ID,"")
    }

    private fun setListeners() {
        imgBack.setOnClickListener(View.OnClickListener { finish() })
    }

    override fun onResume() {
        super.onResume()
        setPreferences()
    }


    //Set Recycler
    private fun setRecycler() {
        rv_categories = findViewById(R.id.rv_categories)
        categoriesResponseList = ArrayList()

        categoriesAdapter = CategoriesAdapter(this, categoriesResponseList,
                object : CategoriesAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int) {

                        val catId = categoriesResponseList[position].prod_cat_id
                        val displayName = categoriesResponseList[position].prod_cat_name
                        val displayThumbnail = categoriesResponseList[position].display_thumbnail
                        val description = categoriesResponseList[position].description
                        val colorCode = categoriesResponseList[position].color_code

                        Utilities.savePrefrence(this@CategoriesActivity, AppConstants.CATEGORY_ID, catId)
                        Utilities.savePrefrence(this@CategoriesActivity, AppConstants.CATEGORY_NAME, displayName)

                        when(catId){
                            AppConstants.CARS_CATEGORY_ID -> {
                                val intent = Intent(this@CategoriesActivity, StartShootActivity::class.java)
                                intent.putExtra(
                                    AppConstants.CATEGORY_NAME,
                                    displayName
                                )
                                intent.putExtra(
                                    AppConstants.CATEGORY_ID,
                                    catId
                                )
                                intent.putExtra(
                                    AppConstants.IMAGE_URL,
                                    displayThumbnail
                                )
                                intent.putExtra(
                                    AppConstants.DESCRIPTION,
                                    description
                                )
                                intent.putExtra(AppConstants.COLOR, colorCode)
                                startActivity(intent)
                            }
                            AppConstants.BIKES_CATEGORY_ID -> {
                                val intent = Intent(this@CategoriesActivity, ShootActivity::class.java)
                                intent.putExtra(
                                    AppConstants.CATEGORY_NAME,
                                    displayName
                                )
                                intent.putExtra(
                                    AppConstants.CATEGORY_ID,
                                    catId
                                )
                                intent.putExtra(
                                    AppConstants.IMAGE_URL,
                                    displayThumbnail
                                )
                                intent.putExtra(
                                    AppConstants.DESCRIPTION,
                                    description
                                )
                                intent.putExtra(AppConstants.COLOR, colorCode)
                                startActivity(intent)
                            }
                            AppConstants.ECOM_CATEGORY_ID,
                            AppConstants.FOOTWEAR_CATEGORY_ID,
                            AppConstants.FOOD_AND_BEV_CATEGORY_ID,
                            AppConstants.HEALTH_AND_BEAUTY_CATEGORY_ID,
                            AppConstants.ACCESSORIES_CATEGORY_ID,
                            AppConstants.WOMENS_FASHION_CATEGORY_ID,
                            AppConstants.MENS_FASHION_CATEGORY_ID,
                            AppConstants.CAPS_CATEGORY_ID,
                            AppConstants.FASHION_CATEGORY_ID,
                            AppConstants.PHOTO_BOX_CATEGORY_ID-> {
                                val intent = Intent(this@CategoriesActivity, ShootPortraitActivity::class.java)
                                intent.putExtra(
                                    AppConstants.CATEGORY_NAME,
                                    displayName
                                )
                                intent.putExtra(
                                    AppConstants.CATEGORY_ID,
                                    catId
                                )
                                intent.putExtra(
                                    AppConstants.IMAGE_URL,
                                    displayThumbnail
                                )
                                intent.putExtra(
                                    AppConstants.DESCRIPTION,
                                    description
                                )
                                intent.putExtra(AppConstants.COLOR, colorCode)
                                startActivity(intent)
                            }

                            else -> {
                                Toast.makeText(
                                    this@CategoriesActivity,
                                    "Coming Soon !",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    }
                },before,after)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_categories.setLayoutManager(layoutManager)
        rv_categories.setAdapter(categoriesAdapter)
    }

    private fun fetchCategories() {
        Utilities.showProgressDialog(this)
        categoriesResponseList.clear()

        viewModel?.getCategories(
            Utilities.getPreference(this, AppConstants.AUTH_KEY).toString()
        )
    }

    private fun observeCategories() {
        viewModel!!.categoriesResponse.observe(this, Observer {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    categoriesResponseList.addAll(it.value.data)

                    categoriesAdapter.notifyDataSetChanged()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    captureFailureEvent(
                        Events.GET_CATEGORIES_FAILED, HashMap<String, Any?>(),
                        it.errorMessage!!
                    )
                    handleApiError(it) { fetchCategories() }
                }
            }
        })
    }

}