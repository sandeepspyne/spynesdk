package com.spyneai.draft.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.draft.ui.adapter.DraftSkusAdapter
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.SkusAdapter
import com.spyneai.posthog.Events
import com.spyneai.shoot.utils.log
import kotlinx.android.synthetic.main.activity_completed_skus.*

class DraftSkusActivity : AppCompatActivity() {
    lateinit var viewModel: DraftViewModel

    lateinit var skusAdapter: DraftSkusAdapter
    val status = "completed"
    var refreshData = true
    lateinit var handler: Handler
    lateinit var runnable: Runnable
    lateinit var skuList: ArrayList<GetProjectsResponse.Sku>
    var position = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draft_skus)

        ivBack.setOnClickListener {
            onBackPressed()
        }

        position = intent.getIntExtra("position", 0)!!

        viewModel = ViewModelProvider(this, ViewModelFactory()).get(DraftViewModel::class.java)

        rvSkus.apply {
            layoutManager =
                LinearLayoutManager(
                    this@DraftSkusActivity, LinearLayoutManager.VERTICAL,
                    false
                )
        }

        skuList = ArrayList<GetProjectsResponse.Sku>()


        shimmerCompletedSKU.startShimmer()

        viewModel.getDrafts(
            Utilities.getPreference(this, AppConstants.AUTH_KEY).toString()
        )


        log("Completed SKUs(auth key): " + Utilities.getPreference(this, AppConstants.AUTH_KEY))
        viewModel.draftResponse.observe(
            this,  {
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

                            skusAdapter = DraftSkusAdapter(
                                this,
                                it.value.data.project_data[position].project_id,
                                skuList
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
                                Events.GET_COMPLETED_ORDERS_FAILED, Properties(),
                                it.errorMessage!!
                            )

                        }
                    }

                }
            }
        )
    }
}