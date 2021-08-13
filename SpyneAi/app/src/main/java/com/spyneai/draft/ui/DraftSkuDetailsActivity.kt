package com.spyneai.draft.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spyneai.R
import kotlinx.android.synthetic.main.activity_draft_sku_details.*

class DraftSkuDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draft_sku_details)

        supportFragmentManager.beginTransaction()
            .add(flContainer.id,DraftSkuDetailsFragment())
            .commit()
    }
}