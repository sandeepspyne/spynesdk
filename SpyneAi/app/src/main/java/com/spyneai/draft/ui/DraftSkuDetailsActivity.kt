package com.spyneai.draft.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.spyneai.R
import com.spyneai.needs.AppConstants
import kotlinx.android.synthetic.main.activity_draft_sku_details.*

class DraftSkuDetailsActivity : AppCompatActivity() {
    val TAG = "DraftSkuDetailsActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draft_sku_details)

        Log.d(TAG, "onCreate: "+intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0))

        supportFragmentManager.beginTransaction()
            .add(flContainer.id,DraftSkuDetailsFragment())
            .commit()
    }
}