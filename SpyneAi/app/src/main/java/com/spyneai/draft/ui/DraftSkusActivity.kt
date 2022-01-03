package com.spyneai.draft.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.R
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.draft.ui.adapter.DraftSkusAdapter
import com.spyneai.draft.ui.adapter.LocalSkusAdapter
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.posthog.Events
import com.spyneai.shoot.repository.model.sku.Sku
import com.spyneai.shoot.utils.log
import kotlinx.android.synthetic.main.activity_completed_skus.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DraftSkusActivity : AppCompatActivity() {
    lateinit var viewModel: DraftViewModel
    lateinit var skusAdapter: DraftSkusAdapter
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

        skuList = ArrayList()

        shimmerCompletedSKU.startShimmer()

        //load data from local
        loadDataFromLocal()

//        if (intent.getBooleanExtra(AppConstants.FROM_LOCAL_DB,false)){
//            val skusList = viewModel.getSkusByProjectId(intent.getStringExtra(AppConstants.PROJECT_ID)!!)
//            val localSkusAdapter = LocalSkusAdapter(this,skusList)
//
//            tvProjectName.text = intent.getStringExtra(AppConstants.PROJECT_NAME)
//            tvTotalSku.text =  skusList.size.toString()
//
//            shimmerCompletedSKU.stopShimmer()
//            shimmerCompletedSKU.visibility = View.GONE
//            rvSkus.visibility = View.VISIBLE
//
//            rvSkus.adapter = localSkusAdapter
//        }else {
//            viewModel.getDrafts(
//                Utilities.getPreference(this, AppConstants.AUTH_KEY).toString()
//            )
//
//            log("Completed SKUs(auth key): " + Utilities.getPreference(this, AppConstants.AUTH_KEY))
//            viewModel.draftResponse.observe(
//                this,  {
//                    when (it) {
//                        is Resource.Success -> {
//                            shimmerCompletedSKU.stopShimmer()
//                            shimmerCompletedSKU.visibility = View.GONE
//                            rvSkus.visibility = View.VISIBLE
//
//                            if (!it.value.data.project_data.isNullOrEmpty()) {
//                                val localSkuList = viewModel.getSkusByProjectId(intent.getStringExtra(AppConstants.PROJECT_ID)!!)
//
//                                if (localSkuList.size >= it.value.data.project_data[position].sku.size) {
//                                    val localSkusAdapter = LocalSkusAdapter(this,localSkuList)
//
//                                    tvProjectName.text = intent.getStringExtra(AppConstants.PROJECT_NAME)
//                                    tvTotalSku.text =  localSkuList.size.toString()
//
//                                    rvSkus.adapter = localSkusAdapter
//                                }
//                                else {
//                                    skuList.clear()
//                                    skuList.addAll(it.value.data.project_data[position].sku)
//                                    tvProjectName.text = it.value.data.project_data[position].project_name
//                                    tvTotalSku.text = it.value.data.project_data[position].sku.size.toString()
//
//                                    skusAdapter = DraftSkusAdapter(
//                                        this,
//                                        it.value.data.project_data[position].project_id,
//                                        it.value.data.project_data[position].category,
//                                        it.value.data.project_data[position].categoryId,
//                                        skuList
//                                    )
//
//                                    val layoutManager: RecyclerView.LayoutManager =
//                                        LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//                                    rvSkus.setLayoutManager(layoutManager)
//                                    rvSkus.setAdapter(skusAdapter)
//                                }
//                            }
//                        }
//                        is Resource.Loading -> {
//
//                        }
//                        is Resource.Failure -> {
//                            shimmerCompletedSKU.stopShimmer()
//                            shimmerCompletedSKU.visibility = View.GONE
//
//                            if (it.errorCode == 404) {
//                                rvSkus.visibility = View.GONE
//                            } else {
//                                this.captureFailureEvent(
//                                    Events.GET_COMPLETED_ORDERS_FAILED, HashMap<String,Any?>(),
//                                    it.errorMessage!!
//                                )
//
//                            }
//                        }
//
//                    }
//                }
//            )
//        }

    }

    private fun loadDataFromLocal() {
        GlobalScope.launch(Dispatchers.IO) {
            val skusList = viewModel.getSkusByProjectId(intent.getStringExtra(AppConstants.PROJECT_ID)!!) as ArrayList<Sku>

            GlobalScope.launch(Dispatchers.Main) {
                val localSkusAdapter = LocalSkusAdapter(this@DraftSkusActivity, skusList)

                tvProjectName.text = intent.getStringExtra(AppConstants.PROJECT_NAME)
                tvTotalSku.text =  skusList.size.toString()

                shimmerCompletedSKU.stopShimmer()
                shimmerCompletedSKU.visibility = View.GONE
                rvSkus.visibility = View.VISIBLE

                rvSkus.adapter = localSkusAdapter
            }
        }

    }
}