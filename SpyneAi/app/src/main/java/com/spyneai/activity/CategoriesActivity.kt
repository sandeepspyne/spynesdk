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
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.model.beforeafter.BeforeAfterResponse
import com.spyneai.model.categories.CategoriesResponse
import com.spyneai.model.categories.Data
import com.spyneai.model.shoot.CreateCollectionRequest
import com.spyneai.model.shoot.CreateCollectionResponse
import com.spyneai.model.shoot.UpdateShootCategoryRequest
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_categories.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class CategoriesActivity : AppCompatActivity(),CategoriesAdapter.BtnClickListener {
    private lateinit var beforeAfterResponser: BeforeAfterResponse
    private lateinit var beforeAfterData: com.spyneai.model.beforeafter.Data
    lateinit var categoriesResponseList : ArrayList<Data>
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
        setRecycler();
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
        Log.e("Token Mine" , Utilities.getPreference(this, AppConstants.tokenId).toString())
        rv_categories = findViewById(R.id.rv_categories)
        categoriesResponseList = ArrayList<Data>()
        categoriesAdapter = CategoriesAdapter(this, categoriesResponseList,
                object : CategoriesAdapter.BtnClickListener {
                    override fun onBtnClick(position: Int) {
                        Log.e("position cat", position.toString())
                        if (position < 2)
                          setShoot(categoriesResponseList,position)
                        else
                            Toast.makeText(this@CategoriesActivity,
                                    "Coming Soon !",
                                    Toast.LENGTH_SHORT).show()
                    }
                },before,after)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_categories.setLayoutManager(layoutManager)
        rv_categories.setAdapter(categoriesAdapter)
    }

    private fun fetchCategories() {
        Utilities.showProgressDialog(this)
        categoriesResponseList.clear()

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.getCategories(Utilities.getPreference(this, AppConstants.tokenId))

        call?.enqueue(object : Callback<CategoriesResponse> {
            override fun onResponse(call: Call<CategoriesResponse>,
                                    response: Response<CategoriesResponse>) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    if (response.body()?.payload?.data?.size!! > 0)
                    {
                        categoriesResponseList.addAll(response.body()?.payload?.data!!)
                        //fetchBeforeAfter()

//                        categoriesResponseList.removeAt(4)
                    }
//                    categoriesResponseList.removeAt(0)
  //                  categoriesResponseList.removeAt(2)
    //                categoriesResponseList.removeAt(1)
      //              categoriesResponseList.removeAt(1)
                    categoriesAdapter.notifyDataSetChanged()
                }
            }
            override fun onFailure(call: Call<CategoriesResponse>, t: Throwable) {
                Log.e("ok", "no way")
                Utilities.hideProgressDialog()
                Toast.makeText(this@CategoriesActivity,
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onBtnClick(position: Int) {
        Log.e("position", position.toString())
    }

    //Fetch shootId
    private fun setShoot(categoriesResponseList: ArrayList<Data>, position: Int) {
        Utilities.showProgressDialog(this)
        val createCollectionRequest = CreateCollectionRequest("Spyne Shoot");

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.createCollection(Utilities.getPreference(this,AppConstants.tokenId),createCollectionRequest)

        call?.enqueue(object : Callback<CreateCollectionResponse>{
            override fun onResponse(call: Call<CreateCollectionResponse>, response: Response<CreateCollectionResponse>) {
                if (response.isSuccessful){
                    Log.e("ok", response.body()?.payload?.data?.shootId.toString())
                    Utilities.savePrefrence(this@CategoriesActivity,
                            AppConstants.SHOOT_ID,
                            response.body()?.payload?.data?.shootId.toString())
                    setCategoryMap(response.body()?.payload?.data?.shootId.toString(),position)
                }
            }
            override fun onFailure(call: Call<CreateCollectionResponse>, t: Throwable) {
                Log.e("ok", "no way")
            }
        })
    }

    private fun setCategoryMap(shootId: String, position: Int) {

        val updateShootCategoryRequest = UpdateShootCategoryRequest(shootId,
                categoriesResponseList[position].catId,
                categoriesResponseList[position].displayName)

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.updateShootCategory(
                Utilities.getPreference(this,AppConstants.tokenId),
                updateShootCategoryRequest)

        call?.enqueue(object : Callback<CreateCollectionResponse>{
            override fun onResponse(call: Call<CreateCollectionResponse>, response: Response<CreateCollectionResponse>) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    val intent = Intent(this@CategoriesActivity, BeforeAfterActivity::class.java)
                    intent.putExtra(AppConstants.CATEGORY_ID,categoriesResponseList[position].catId)
                    intent.putExtra(AppConstants.CATEGORY_NAME,categoriesResponseList[position].displayName)
                    intent.putExtra(AppConstants.IMAGE_URL,categoriesResponseList[position].displayThumbnail)
                    intent.putExtra(AppConstants.DESCRIPTION,categoriesResponseList[position].description)
                    intent.putExtra(AppConstants.COLOR,categoriesResponseList[position].colorCode)
                    startActivity(intent)
                    Log.e("Category map",categoriesResponseList[position].catId+" " + response.body()!!.msgInfo.msgDescription)
                }
            }
            override fun onFailure(call: Call<CreateCollectionResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Log.e("ok", "no way")

            }
        })
    }
}