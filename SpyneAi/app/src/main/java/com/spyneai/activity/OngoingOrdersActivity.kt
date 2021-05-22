package com.spyneai.activity

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.adapter.OngoingProjectAdapter
import com.spyneai.dashboard.ui.dashboard.MainDashboardActivity
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.service.ProcessImagesService
import kotlinx.android.synthetic.main.activity_completed.*
import kotlinx.android.synthetic.main.activity_completed_projects.*
import kotlinx.android.synthetic.main.activity_completed_projects.imgBackCompleted
import kotlinx.android.synthetic.main.activity_ongoing_orders.*

class OngoingOrdersActivity : AppCompatActivity() {

    lateinit var ongoingProjectAdapter : OngoingProjectAdapter
    lateinit var ongoingProjectList : ArrayList<com.spyneai.model.processImageService.Task>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ongoing_orders)

        ongoingProjectList = ProcessImagesService.tasksInProgress


//        fatchCompletedProjects()
        showOngoingProjects()
        listeners()
    }

    private fun listeners() {
        imgBackCompleted.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })

        llViewCompletedOrders.setOnClickListener {
            val intent = Intent(this, CompletedProjectsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showOngoingProjects(){
        ongoingProjectAdapter = OngoingProjectAdapter(this@OngoingOrdersActivity,
            ongoingProjectList, object : OngoingProjectAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position cat", position.toString())
//                    Utilities.savePrefrence(this@OngoingOrdersActivity,
//                        AppConstants.SKU_ID,
//                        ongoingProjectList[position].skuId)
//                    val intent = Intent(this@OngoingOrdersActivity,
//                        ShowImagesActivity::class.java)
//                    startActivity(intent)

                }
            })

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this@OngoingOrdersActivity, LinearLayoutManager.VERTICAL, false)
        rv_ongoingActivity.setLayoutManager(layoutManager)
        rv_ongoingActivity.setAdapter(ongoingProjectAdapter)
        refreshList()
        showHideRecyclerView()

    }

    fun refreshList(){
        Handler(Looper.getMainLooper()).postDelayed({

//            ongoingProjectList.forEachIndexed { index, item ->
//                if (item.isCompleted){
//                    ongoingProjectList.remove(item)
//                    ongoingProjectAdapter.notifyItemChanged(index)
//                }
//                showHideRecyclerView()
//            }
            ongoingProjectAdapter.notifyDataSetChanged()
            refreshList()
        }, 3000)

    }

    fun showHideRecyclerView(){
        if (ongoingProjectList.size > 0)
        {
            rv_ongoingActivity.visibility = View.VISIBLE
            tvOngoingOrders.visibility = View.GONE
            llViewCompletedOrders.visibility = View.VISIBLE
        }
        else{
            rv_ongoingActivity.visibility = View.GONE
            llViewCompletedOrders.visibility = View.GONE
            tvOngoingOrders.visibility = View.VISIBLE
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}