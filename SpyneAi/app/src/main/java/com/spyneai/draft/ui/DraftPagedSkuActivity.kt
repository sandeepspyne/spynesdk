package com.spyneai.draft.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityDraftSkusBinding
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.draft.ui.adapter.SkuPagedAdapter
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.orders.data.paging.LoaderStateAdapter
import com.spyneai.shoot.repository.model.sku.Sku
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class DraftPagedSkuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDraftSkusBinding
    lateinit var viewModel: DraftViewModel
    var position = 0
    lateinit var adapter: SkuPagedAdapter
    val TAG = "DraftPagedSkuActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDraftSkusBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        adapter = SkuPagedAdapter(
            this,
            "draft"
        )

        binding.rvSkus.layoutManager =
            LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL,
                false
            )

        val loaderStateAdapter = LoaderStateAdapter { adapter.retry() }
        binding.rvSkus.adapter = adapter.withLoadStateFooter(loaderStateAdapter)

        binding.rvSkus.setOnClickListener {
            onBackPressed()
        }

        position = intent.getIntExtra("position", 0)!!

        binding.tvProjectName.text = intent.getStringExtra(AppConstants.PROJECT_NAME)
        binding.tvTotalSku.text = intent.getIntExtra(AppConstants.SKU_COUNT,0).toString()

        viewModel = ViewModelProvider(this, ViewModelFactory()).get(DraftViewModel::class.java)

        binding.shimmerCompletedSKU.startShimmer()

        fetchSkus()

        viewModel.syncImages.observe(this,{
            fetchSkus()
        })

        adapter.addLoadStateListener{
            Log.d(TAG, "onCreate: "+it.append.endOfPaginationReached)

            if (it.append.endOfPaginationReached){
                if (!isInternetActive() && (adapter.itemCount == 0 && intent.getIntExtra(AppConstants.SKU_COUNT,0) > adapter.itemCount)){
                    ImageNotSyncedDialog()
                        .apply {
                            arguments = Bundle().apply {
                                putBoolean("sku_sync",true)
                            }
                        }
                        .show(
                        supportFragmentManager,
                        "ImageNotSyncedDialog"
                    )
                }
            }
        }

        //load data from local
        //loadDataFromLocal()

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

    private fun fetchSkus(){
        GlobalScope.launch(Dispatchers.IO) {
            getSkus()
        }
    }

    @ExperimentalPagingApi
    private fun getSkus() {
        lifecycleScope.launch {
            viewModel.getSkus(
                intent.getStringExtra(AppConstants.PROJECT_ID),
                intent.getStringExtra(AppConstants.PROJECT_UUIID)!!
            ).distinctUntilChanged().collectLatest {
                if (!binding.rvSkus.isVisible) {
                    binding.shimmerCompletedSKU.stopShimmer()
                    binding.shimmerCompletedSKU.visibility = View.GONE
                    binding.rvSkus.visibility = View.VISIBLE
                }
                adapter.submitData(it)
            }
        }
    }

    private fun loadDataFromLocal() {
        GlobalScope.launch(Dispatchers.IO) {
            val skusList =
                viewModel.getSkusByProjectId(intent.getStringExtra(AppConstants.PROJECT_ID)!!) as ArrayList<Sku>

            GlobalScope.launch(Dispatchers.Main) {
                //val localSkusAdapter = LocalSkusAdapter(this@DraftSkusActivity, skusList)

//                tvProjectName.text = intent.getStringExtra(AppConstants.PROJECT_NAME)
//                tvTotalSku.text =  skusList.size.toString()
//
//                shimmerCompletedSKU.stopShimmer()
//                shimmerCompletedSKU.visibility = View.GONE
//                rvSkus.visibility = View.VISIBLE
//
//                rvSkus.adapter = localSkusAdapter
            }
        }

    }
}