package com.spyneai.orders.ui.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.paging.ExperimentalPagingApi
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.spyneai.orders.data.paging.PagedFragment

@ExperimentalPagingApi
class OrdersSlideAdapter (fa: FragmentActivity) : FragmentStateAdapter(fa) {


    override fun getItemCount(): Int = 3


    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0-> getFragment("draft")
            1-> getFragment("ongoing")
            else -> getFragment("completed")
        }
    }


    fun getFragment(status : String) = PagedFragment()
        .apply {
            arguments = Bundle().apply {
                putString("status",status)
            }
        }

}