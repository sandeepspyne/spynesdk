package com.spyneai.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spyneai.R
import com.spyneai.adapter.MarketAdapter
import com.spyneai.model.channels.Data

class BackgroundAddActivity : AppCompatActivity() {

    private lateinit var marketAdapter: MarketAdapter
    private lateinit var photoList: List<Data>
    var pos : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_background_add)
    }
}