package com.spyneai.orders.ui.fragment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityCompletedBinding
import com.spyneai.databinding.ActivityCompletedSkusBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.SkusAdapter
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.utils.log
import kotlinx.android.synthetic.main.activity_completed_skus.*

class CompletedSkusActivity : AppCompatActivity() {

    lateinit var viewModel : MyOrdersViewModel

    lateinit var skusAdapter: SkusAdapter
    val status = "completed"
    var refreshData = true
    lateinit var handler: Handler
    lateinit var runnable: Runnable
    lateinit var skuList: ArrayList<GetProjectsResponse.Sku>
    var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val binding = ActivityCompletedSkusBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_completed_skus)

        position = intent.getIntExtra("position", 0)!!

        viewModel = ViewModelProvider(this, ViewModelFactory()).get(MyOrdersViewModel::class.java)

        rvSkus.apply {
            layoutManager =
                LinearLayoutManager(
                    this@CompletedSkusActivity, LinearLayoutManager.VERTICAL,
                    false
                )
        }

        skuList = ArrayList<GetProjectsResponse.Sku>()


        shimmerCompletedSKU.startShimmer()
        repeatRefreshData()


        log("Completed SKUs(auth key): "+ Utilities.getPreference(this, AppConstants.AUTH_KEY))
        viewModel.getProjectsResponse.observe(
            this, Observer {
                when (it) {
                    is Resource.Success -> {
                        shimmerCompletedSKU.stopShimmer()
                        shimmerCompletedSKU.visibility = View.GONE
                        rvSkus.visibility = View.VISIBLE

                        if (it.value.data.project_data.isNullOrEmpty())
                            refreshData = false

                        if (it.value.data != null){

                            tvTotalSku.text = it.value.data.total_skus.toString()

                            skuList.clear()

                                skuList.addAll(it.value.data.project_data[position].sku)
                                tvProjectName.text = it.value.data.project_data[position].project_name

                            skusAdapter = SkusAdapter(this,
                                it.value.data.project_data, viewModel, skuList
                            )

                            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                            rvSkus.setLayoutManager(layoutManager)
                            rvSkus.setAdapter(skusAdapter)
                        }
                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Failure -> {
                        refreshData = false
                        shimmerCompletedSKU.stopShimmer()
                        shimmerCompletedSKU.visibility = View.GONE

                        if (it.errorCode == 404){
                            rvSkus.visibility = View.GONE
                        }else{
                            this.captureFailureEvent(
                                Events.GET_COMPLETED_ORDERS_FAILED, Properties(),
                                it.errorMessage!!
                            )

                        }
                    }

                }
            }
        )

    }

    fun repeatRefreshData(){
        viewModel.getProjects(Utilities.getPreference(this, AppConstants.AUTH_KEY).toString(), status)
        handler = Handler()
        runnable = Runnable {
            if (refreshData)
                repeatRefreshData()  }
        handler.postDelayed(runnable,15000)
    }

    override fun onPause() {
        handler.removeCallbacks(runnable)
        super.onPause()
    }

}