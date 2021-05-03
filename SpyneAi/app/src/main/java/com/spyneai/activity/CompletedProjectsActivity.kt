package com.spyneai.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.adapter.CompletedProjectAdapter
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClients
import com.spyneai.model.projects.CompletedProjectResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_completed_projects.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CompletedProjectsActivity : AppCompatActivity() {

    lateinit var completedProjectList : ArrayList<CompletedProjectResponse>
    lateinit var completedProjectAdapter : CompletedProjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_projects)

        fatchCompletedProjects()
        listeners()
    }

    private fun listeners() {
        imgBackCompleted.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
    }


    private fun fatchCompletedProjects(){
        completedProjectList = ArrayList<CompletedProjectResponse>()

        completedProjectAdapter = CompletedProjectAdapter(this@CompletedProjectsActivity,
                completedProjectList, object : CompletedProjectAdapter.BtnClickListener {
            override fun onBtnClick(position: Int) {
                Log.e("position cat", position.toString())
                Utilities.savePrefrence(this@CompletedProjectsActivity,
                        AppConstants.SKU_ID,
                        completedProjectList[position].sku_id)
                val intent = Intent(this@CompletedProjectsActivity,
                        ShowImagesActivity::class.java)
                startActivity(intent)

            }
        })

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this@CompletedProjectsActivity, LinearLayoutManager.VERTICAL, false)
        rv_completedActivity.setLayoutManager(layoutManager)
        rv_completedActivity.setAdapter(completedProjectAdapter)


        Utilities.showProgressDialog(this)

        val request = RetrofitClients.buildService(APiService::class.java)

        Utilities.savePrefrence(this,AppConstants.tokenId,
                Utilities.getPreference(this,AppConstants.tokenId))
        val userId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.tokenId)!!)

        val call = request.getCompletedProjects(userId)

        call?.enqueue(object : Callback<List<CompletedProjectResponse>> {
            override fun onResponse(call: Call<List<CompletedProjectResponse>>,
                                    response: Response<List<CompletedProjectResponse>>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    if (response.body()!!.size > 0)
                    {
                        completedProjectList.addAll(response.body()!!)
                        completedProjectList.reverse()
                    }
                    else{
                        Toast.makeText(this@CompletedProjectsActivity ,
                                "No projects found !", Toast.LENGTH_SHORT).show()
                    }

                    completedProjectAdapter.notifyDataSetChanged()
                }
            }
            override fun onFailure(call: Call<List<CompletedProjectResponse>>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(this@CompletedProjectsActivity , "Server not responding!!!", Toast.LENGTH_SHORT).show()
            }
        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }



/*
    fun fetchGif(skuIds: String)
    {
        Utilities.savePrefrence(this,AppConstants.SKU_ID,skuIds)
        Utilities.showProgressDialog(this)

        val request = RetrofitClients.buildService(APiService::class.java)
*/
/*
        Utilities.savePrefrence(this,AppConstants.tokenId,
                Utilities.getPreference(this,AppConstants.tokenId))
*//*


        val userId = RequestBody.create(
                MultipartBody.FORM,
                Utilities.getPreference(this, AppConstants.tokenId)!!)

        val skuId = RequestBody.create(
                MultipartBody.FORM,
                skuIds)

        val call = request.fetchUserGif(userId,skuId)

        call?.enqueue(object : Callback<List<GifFetchResponse>> {
            override fun onResponse(call: Call<List<GifFetchResponse>>,
                                    response: Response<List<GifFetchResponse>>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful){

                    if (response.body() != null) {
                        val intent = Intent(this@CompletedProjectsActivity,
                                ShowGifActivity::class.java)
                        intent.putExtra(AppConstants.GIF, response.body()!![0].gif_url)
                        startActivity(intent)
                    }
                    else
                    {
                        Toast.makeText(this@CompletedProjectsActivity ,
                                "Unable to fetch project details currently. Please try again later !", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<List<GifFetchResponse>>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(this@CompletedProjectsActivity ,
                        "No details found for this project. Please try again later !", Toast.LENGTH_SHORT).show()
            }
        })
    }
*/
}