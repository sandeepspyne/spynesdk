package com.spyneai.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.adapter.DashboardResponseAdapter
import com.spyneai.interfaces.APiService
import com.spyneai.interfaces.RetrofitClient
import com.spyneai.model.dashboard.DashboardResponse
import com.spyneai.model.dashboard.Data
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_completed.*

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CompletedActivity : AppCompatActivity() {
    lateinit var dashboardResponseAdapter: DashboardResponseAdapter
    lateinit var dashboardResponseList : ArrayList<Data>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed)

        setRecycler()
        if (Utilities.isNetworkAvailable(applicationContext))
            fetchCompleted()
        else
            Toast.makeText(this,
                "Please check your internet connection",
                Toast.LENGTH_SHORT).show()
        listeners()
    }

    private fun setRecycler() {
        dashboardResponseList = ArrayList<Data>()
        dashboardResponseAdapter = DashboardResponseAdapter(this, dashboardResponseList,
            object : DashboardResponseAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {

                }
            },"Completed")

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvCompleted.setLayoutManager(layoutManager)
        rvCompleted.setAdapter(dashboardResponseAdapter)
    }

    private fun fetchCompleted() {
        Utilities.showProgressDialog(this)
        dashboardResponseList.clear()

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.getDashboardData(Utilities.getPreference(this, AppConstants.TOKEN_ID))

        call?.enqueue(object : Callback<DashboardResponse> {
            override fun onResponse(call: Call<DashboardResponse>,
                                    response: Response<DashboardResponse>) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    if (response.body()?.payload?.data?.size != null)
                    {
                        for (i in 0..response.body()?.payload?.data!!.size-1) {
                            if (response.body()?.payload?.data!![i].status.equals("completed")) {
                                dashboardResponseList.add(response.body()?.payload?.data!![i])
                            }
                        }
                    }

                    if (dashboardResponseList.size > 0)
                    {
                        rvCompleted.visibility = View.VISIBLE
                        tvError.visibility = View.GONE
                    }
                    else{
                        rvCompleted.visibility = View.GONE
                        tvError.visibility = View.VISIBLE
                    }
                    dashboardResponseAdapter.notifyDataSetChanged()
                }
            }
            override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(this@CompletedActivity,
                    "Server not responding!!!",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun listeners() {
        ivBackCompleted.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}