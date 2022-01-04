package com.spyneai.orders.ui.activity

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.adapter.CategoriesAdapter
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.SkusAdapter
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ImageLocalRepository
import com.spyneai.shoot.utils.log
import kotlinx.android.synthetic.main.activity_completed_skus.*

class CompletedSkusActivity : AppCompatActivity() {

    lateinit var viewModel: MyOrdersViewModel

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

        ivBack.setOnClickListener {
            onBackPressed()
        }

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
        viewModel.getCompletedProjects(
            Utilities.getPreference(this, AppConstants.AUTH_KEY).toString(), status
        )


        log("Completed SKUs(auth key): " + Utilities.getPreference(this, AppConstants.AUTH_KEY))
        viewModel.getCompletedProjectsResponse.observe(
            this, Observer {
                when (it) {
                    is Resource.Success -> {
                        shimmerCompletedSKU.stopShimmer()
                        shimmerCompletedSKU.visibility = View.GONE
                        rvSkus.visibility = View.VISIBLE

                        if (it.value.data.project_data.isNullOrEmpty())
                            refreshData = false

                        if (it.value.data != null) {
                            skuList.clear()
                            skuList.addAll(it.value.data.project_data[position].sku)
                            tvProjectName.text = it.value.data.project_data[position].project_name
                            tvTotalSku.text = it.value.data.project_data[position].sku.size.toString()

                            skusAdapter = SkusAdapter(
                                this,
                                viewModel, skuList,
                                it.value.data.project_data[position].project_id,
                                object : SkusAdapter.BtnClickListener{
                                    override fun onBtnClick(
                                        position: Int,
                                        sku: GetProjectsResponse.Sku
                                    ) {
                                        //show delete sku dialog
                                        val imagesPathList = ImageLocalRepository().getImagesPathBySkuId()
                                    }

                                }
                            )

                            val layoutManager: RecyclerView.LayoutManager =
                                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
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

                        if (it.errorCode == 404) {
                            rvSkus.visibility = View.GONE
                        } else {
                            this.captureFailureEvent(
                                Events.GET_COMPLETED_ORDERS_FAILED, HashMap<String,Any?>(),
                                it.errorMessage!!
                            )

                        }
                    }

                }
            }
        )

    }
}