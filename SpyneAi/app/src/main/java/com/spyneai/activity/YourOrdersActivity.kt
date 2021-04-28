package com.spyneai.activity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.spyneai.R
import com.spyneai.adapter.YourOrdersViewPagerAdapter
import com.spyneai.fragment.OngoingOrdersFragment
import kotlinx.android.synthetic.main.activity_your_orders.*


class YourOrdersActivity : AppCompatActivity() {

    private lateinit var yourOrderAdapter: YourOrdersViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_orders)

        tlYourOrders!!.addTab(tlYourOrders!!.newTab().setText("Ongoing"))
        tlYourOrders!!.addTab(tlYourOrders!!.newTab().setText("Completed"))
        tlYourOrders!!.tabGravity = TabLayout.GRAVITY_FILL

        yourOrderAdapter = YourOrdersViewPagerAdapter(this)
        vpYourOrders.adapter = yourOrderAdapter

        (tlYourOrders as TabLayout).setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        (tlYourOrders as TabLayout).setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.primary))




        tlYourOrders.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab!!.position){
                    0 -> {
                        tab.setText("Ongoing")
                    } 1 -> {
                    tab.setText("Completed")
                }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

    }
}