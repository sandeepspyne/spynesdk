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
}