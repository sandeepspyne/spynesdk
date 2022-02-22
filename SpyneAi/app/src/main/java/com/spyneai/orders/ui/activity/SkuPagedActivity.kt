package com.spyneai.orders.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.R
import com.spyneai.base.BaseActivity
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityCompletedSkusBinding
import com.spyneai.databinding.ActivityDraftSkusBinding
import com.spyneai.databinding.ActivitySkuPagedBinding
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.draft.ui.ImageNotSyncedDialog
import com.spyneai.draft.ui.adapter.SkuPagedAdapter
import com.spyneai.handleFirstPageError
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.orders.data.paging.LoaderStateAdapter
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.fragment.MyOrdersFragment
import com.spyneai.orders.ui.fragment.SkuPagedFragment
import com.spyneai.showConnectionChangeView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class SkuPagedActivity : BaseActivity() {

    private lateinit var binding: ActivitySkuPagedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySkuPagedBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportFragmentManager.beginTransaction()
            .add(R.id.flContainer, SkuPagedFragment())
            .commit()

    }


    override fun onConnectionChange(isConnected: Boolean) {
        showConnectionChangeView(isConnected,binding.root)
    }
}