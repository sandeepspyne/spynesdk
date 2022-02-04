package com.spyneai.draft.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.ExperimentalPagingApi
import com.spyneai.R
import com.spyneai.base.BaseActivity
import com.spyneai.databinding.ActivityDraftSkuDetailsBinding
import com.spyneai.needs.AppConstants
import com.spyneai.showConnectionChangeView
import kotlinx.android.synthetic.main.activity_draft_sku_details.*

class DraftSkuDetailsActivity : BaseActivity() {

    lateinit var binding: ActivityDraftSkuDetailsBinding

    @OptIn(ExperimentalPagingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDraftSkuDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: "+intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0))

        supportFragmentManager.beginTransaction()
            .add(flContainer.id,DraftSkuDetailsFragment())
            .commit()
    }

    override fun onConnectionChange(isConnected: Boolean) {
        showConnectionChangeView(isConnected,binding.root)
    }
}