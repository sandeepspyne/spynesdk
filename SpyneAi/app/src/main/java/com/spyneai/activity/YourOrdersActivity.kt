package com.spyneai.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.spyneai.R
import com.spyneai.adapter.YourOrdersViewPagerAdapter
import kotlinx.android.synthetic.main.activity_your_orders.*


class YourOrdersActivity : AppCompatActivity() {

    private lateinit var yourOrderAdapter: YourOrdersViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_orders)
        yourOrderAdapter = YourOrdersViewPagerAdapter(this)
        vpYourOrders.adapter = yourOrderAdapter

    }
}