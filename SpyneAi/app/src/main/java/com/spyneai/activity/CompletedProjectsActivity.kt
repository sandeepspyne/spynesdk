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
                        for (i in 0..response.body()!!.size - 1) {
                            if (response.body()!![i].current_frame == response.body()!![i].total_frames){
                                completedProjectList.addAll(response.body()!!)
                                completedProjectList.reverse()
                            }
                        }
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
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}