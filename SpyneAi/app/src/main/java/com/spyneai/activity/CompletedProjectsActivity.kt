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

        shimmerCompletedProjects.startShimmer()

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
                if (response.isSuccessful){
                    shimmerCompletedProjects.stopShimmer()
                    shimmerCompletedProjects.visibility = View.GONE
                    rv_completedActivity.visibility = View.VISIBLE
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
                shimmerCompletedProjects.stopShimmer()
                shimmerCompletedProjects.visibility = View.GONE
                rv_completedActivity.visibility = View.VISIBLE
                Toast.makeText(this@CompletedProjectsActivity , "Server not responding!!!", Toast.LENGTH_SHORT).show()
            }
        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}