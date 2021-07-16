package com.spyneai.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.extras.BeforeAfterActivity
import com.spyneai.R
import com.spyneai.adapter.CategoriesAdapter
import com.spyneai.dashboard.response.Data
import com.spyneai.dashboard.response.NewCategoriesResponse
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.beforeafter.BeforeAfterResponse
import com.spyneai.model.categories.CategoriesResponse

import com.spyneai.model.shoot.CreateCollectionRequest
import com.spyneai.model.shoot.CreateCollectionResponse
import com.spyneai.model.shoot.UpdateShootCategoryRequest
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.ui.ShootActivity
import kotlinx.android.synthetic.main.activity_categories.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class CategoriesActivity : AppCompatActivity(){
    lateinit var categoriesResponseList : ArrayList<NewCategoriesResponse.Data>
    lateinit var categoriesAdapter : CategoriesAdapter
    lateinit var rv_categories : RecyclerView
    var before = ""
    var after = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setPreferences()
        setListeners()
        setRecycler()

        if (Utilities.isNetworkAvailable(applicationContext))
            fetchCategories()
        else
            Toast.makeText(this,
                    "Please check your internet connection",
                    Toast.LENGTH_SHORT).show()

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
                        Log.e("position cat", position.toString())
                        when(position) {
                            0,1 -> {
                                Utilities.savePrefrence(
                                    this@CategoriesActivity,
                                    AppConstants.CATEGORY_NAME,
                                    categoriesResponseList[position].prod_cat_name
                                )

                                val intent = Intent(this@CategoriesActivity, ShootActivity::class.java)
                                intent.putExtra(AppConstants.CATEGORY_ID,categoriesResponseList[position].prod_cat_id)
                                intent.putExtra(AppConstants.CATEGORY_NAME,categoriesResponseList[position].prod_cat_name)
                                intent.putExtra(AppConstants.IMAGE_URL,categoriesResponseList[position].display_thumbnail)
                                intent.putExtra(AppConstants.DESCRIPTION,categoriesResponseList[position].description)
                                intent.putExtra(AppConstants.COLOR,categoriesResponseList[position].color_code)
                                startActivity(intent)
                            }
                            2 -> {
                                Utilities.savePrefrence(
                                    this@CategoriesActivity,
                                    AppConstants.CATEGORY_NAME,
                                    categoriesResponseList[position].prod_cat_name
                                )

                                val intent = Intent(this@CategoriesActivity, ShootActivity::class.java)
                                intent.putExtra(AppConstants.CATEGORY_ID,categoriesResponseList[position].prod_cat_id)
                                intent.putExtra(AppConstants.CATEGORY_NAME,categoriesResponseList[position].prod_cat_name)
                                intent.putExtra(AppConstants.IMAGE_URL,categoriesResponseList[position].display_thumbnail)
                                intent.putExtra(AppConstants.DESCRIPTION,categoriesResponseList[position].description)
                                intent.putExtra(AppConstants.COLOR,categoriesResponseList[position].color_code)
                                startActivity(intent)
                            }
                            else ->{
                                Toast.makeText(this@CategoriesActivity,
                                    "Coming Soon !",
                                    Toast.LENGTH_SHORT).show() 
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

        val request = RetrofitClients.buildService(APiService::class.java)
        val call = request.getCategories(Utilities.getPreference(this,AppConstants.AUTH_KEY).toString())

        call?.enqueue(object : Callback<NewCategoriesResponse> {
            override fun onResponse(call: Call<NewCategoriesResponse>,
                                    response: Response<NewCategoriesResponse>) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    if (response.isSuccessful) {

                        var categoriesResponse = response.body()

                        if (categoriesResponse?.status == 200 && categoriesResponse.data.isNotEmpty()){
                            categoriesResponseList.addAll(categoriesResponse.data)

                            categoriesAdapter.notifyDataSetChanged()
                        }else{
                            Toast.makeText(
                                this@CategoriesActivity,
                                categoriesResponse?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                }
            }
            override fun onFailure(call: Call<NewCategoriesResponse>, t: Throwable) {
                Log.e("ok", "no way")
                Utilities.hideProgressDialog()
                Toast.makeText(this@CategoriesActivity,
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT).show()
            }
        })
    }

}