package com.spyneai.activity

import UploadPhotoResponse
import android.content.Intent
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

class OngoingActivity : AppCompatActivity() {
    lateinit var dashboardResponseAdapter: DashboardResponseAdapter
    lateinit var dashboardResponseList : ArrayList<Data>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ongoing)

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

                        Utilities.savePrefrence(this@OngoingActivity,
                                AppConstants.CATEGORY_NAME,dashboardResponseList[position].categoryName)
                        Utilities.savePrefrence(this@OngoingActivity,
                                AppConstants.SHOOT_ID,dashboardResponseList[position].shootId)
                        Utilities.savePrefrence(this@OngoingActivity,
                                AppConstants.SKU_ID,dashboardResponseList[position].skuId)

                        if (dashboardResponseList[position].stageInCompleted.equals(AppConstants.completed))
                            callOrder(position)
                        else
                            continueShoot(dashboardResponseList,position)
                    }
                },"Ongoing")

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvCompleted.setLayoutManager(layoutManager)
        rvCompleted.setAdapter(dashboardResponseAdapter)
    }

    private fun continueShoot(dashboardResponseList: ArrayList<Data>, position: Int) {
        Utilities.showProgressDialog(this)

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.getContinueShoot(
                Utilities.getPreference(this, AppConstants.tokenId),
                dashboardResponseList[position].skuId)

        call?.enqueue(object : Callback<UploadPhotoResponse> {
            override fun onResponse(call: Call<UploadPhotoResponse>,
                                    response: Response<UploadPhotoResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    if (response.body()?.payload?.data != null)
                    {
                        val intent = Intent(this@OngoingActivity, CameraActivity::class.java)
                        intent.putExtra(AppConstants.CATEGORY_ID, dashboardResponseList[position].catId)
                        intent.putExtra(AppConstants.CATEGORY_NAME, dashboardResponseList[position].categoryName)

                        intent.putExtra(AppConstants.SKU_NAME, response.body()?.payload!!.data.skuName)
                        intent.putExtra(AppConstants.FRAME, response.body()?.payload!!.data.currentFrame)
                        intent.putExtra(AppConstants.TOTAL_FRAME, response.body()?.payload!!.data.totalFrames)
                        intent.putExtra(AppConstants.FRAME_IMAGE, response.body()?.payload!!.data.frame.displayImage)

                        Utilities.savePrefrence(this@OngoingActivity, AppConstants.FROM, "AB")
                        startActivity(intent)
                    }
                }
            }
            override fun onFailure(call: Call<UploadPhotoResponse>, t: Throwable) {
                Utilities.hideProgressDialog()
                Toast.makeText(this@OngoingActivity,
                        "Server not responding!!!",
                        Toast.LENGTH_SHORT).show()
            }
        })

        //callCamera(this.dashboardResponseList,position)
    }


    //Open order screen
    private fun callOrder(position: Int) {
        val intent = Intent(this, OrderActivity::class.java)
        intent.putExtra(AppConstants.FROM_ACTIVITY,"Ongoing")
        startActivity(intent)
    }

    private fun fetchCompleted() {
        Utilities.showProgressDialog(this)
        dashboardResponseList.clear()

        val request = RetrofitClient.buildService(APiService::class.java)
        val call = request.getDashboardData(Utilities.getPreference(this, AppConstants.tokenId))

        call?.enqueue(object : Callback<DashboardResponse> {
            override fun onResponse(call: Call<DashboardResponse>,
                                    response: Response<DashboardResponse>
            ) {
                Utilities.hideProgressDialog()
                if (response.isSuccessful){
                    if (response.body()?.payload?.data?.size!! > 0)
                    {
                        for (i in 0..(response.body()?.payload?.data?.size!! -1)) {
                            if (response.body()?.payload?.data!![i].status.equals("ongoing")) {
                                if (!response.body()?.payload?.data!![i].shootId.equals("") &&
                                        !response.body()?.payload?.data!![i].prodId.equals("") &&
                                        !response.body()?.payload?.data!![i].skuId.equals(""))
                                    if(!response.body()?.payload?.data!![i].productName.equals(""))
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
                Toast.makeText(this@OngoingActivity,
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