package com.spyneai.orders.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.base.BaseFragment
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.FragmentOngoingProjectsBinding
import com.spyneai.databinding.FragmentSkuPagedBinding
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.draft.ui.ImageNotSyncedDialog
import com.spyneai.draft.ui.adapter.SkuPagedAdapter
import com.spyneai.handleFirstPageError
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.orders.data.paging.LoaderStateAdapter
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class SkuPagedFragment : BaseFragment<DraftViewModel, FragmentSkuPagedBinding>() {

    lateinit var intent : Intent
    var position = 0
    lateinit var adapter: SkuPagedAdapter
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intent = requireActivity().intent
        
        intent.getStringExtra(AppConstants.STATUS)?.let {
            adapter = SkuPagedAdapter(
                requireContext(),
                it
            )
        }

        binding.rvSkus.layoutManager =
            LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL,
                false
            )

        val loaderStateAdapter = LoaderStateAdapter { adapter.retry() }
        binding.rvSkus.adapter = adapter.withLoadStateFooter(loaderStateAdapter)

        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        position = intent.getIntExtra("position", 0)!!

        binding.tvProjectName.text = intent.getStringExtra(AppConstants.PROJECT_NAME)
        binding.tvTotalSku.text = intent.getIntExtra(AppConstants.SKU_COUNT,0).toString()

        binding.shimmerCompletedSKU.startShimmer()

        fetchSkus()

        viewModel.syncImages.observe(viewLifecycleOwner,{
            fetchSkus()
        })

        adapter.addLoadStateListener{

            adapter.addLoadStateListener { loadState ->
                if (adapter.itemCount == 0){
                    handleFirstPageError(loadState){adapter.retry()}
                }
            }

            if (it.append.endOfPaginationReached){
                if (!requireContext().isInternetActive() && (adapter.itemCount == 0 && intent.getIntExtra(AppConstants.SKU_COUNT,0) > adapter.itemCount)){
                    ImageNotSyncedDialog()
                        .apply {
                            arguments = Bundle().apply {
                                putBoolean("sku_sync",true)
                            }
                        }
                        .show(
                            requireActivity().supportFragmentManager,
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
                intent.getStringExtra(AppConstants.PROJECT_UUIID)!!,
                1
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
    
    override fun getViewModel()= DraftViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSkuPagedBinding.inflate(inflater, container, false)
}