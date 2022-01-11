package com.spyneai.orders.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.paging.ExperimentalPagingApi
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.spyneai.draft.ui.DraftProjectsFragment
import com.spyneai.orders.data.paging.PagedFragment
import com.spyneai.orders.ui.fragment.CompletedProjectsFragment
import com.spyneai.orders.ui.fragment.OngoingProjectsFragment

class OrdersSlideAdapter (fa: FragmentActivity) : FragmentStateAdapter(fa) {


    override fun getItemCount(): Int = 3

    @ExperimentalPagingApi
    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0-> PagedFragment()
            1-> OngoingProjectsFragment()
            else -> CompletedProjectsFragment()
        }
    }

}