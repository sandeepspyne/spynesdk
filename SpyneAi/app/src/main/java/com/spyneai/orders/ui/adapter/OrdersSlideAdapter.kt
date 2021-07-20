package com.spyneai.orders.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.spyneai.orders.ui.fragment.CompletedProjectsFragment
import com.spyneai.orders.ui.fragment.OngoingProjectsFragment

class OrdersSlideAdapter (fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment =
        if (position == 0) OngoingProjectsFragment() else CompletedProjectsFragment()
}