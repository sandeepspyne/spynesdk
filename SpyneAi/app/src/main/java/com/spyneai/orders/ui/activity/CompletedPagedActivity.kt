package com.spyneai.orders.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityCompletedSkusBinding
import com.spyneai.databinding.ActivityDraftSkusBinding
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.draft.ui.adapter.SkuPagedAdapter
import com.spyneai.needs.AppConstants
import com.spyneai.orders.data.paging.LoaderStateAdapter
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class CompletedPagedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompletedSkusBinding

    lateinit var viewModel: MyOrdersViewModel
    var position = 0
    lateinit var adapter: SkuPagedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompletedSkusBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        adapter = SkuPagedAdapter(
            this,
            "completed"
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

        viewModel = ViewModelProvider(this, ViewModelFactory()).get(MyOrdersViewModel::class.java)

        binding.shimmerCompletedSKU.startShimmer()

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