package com.spyneai.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.spyneai.activity.YourOrdersActivity
import com.spyneai.fragment.CompletedOrdersFragment
import com.spyneai.fragment.OngoingOrdersFragment

public class YourOrdersViewPagerAdapter(fragment: YourOrdersActivity) : FragmentStateAdapter(
    fragment
) {

    val CARD_ITEM_SIZE: Int = 2

    override fun getItemCount(): Int {
        return CARD_ITEM_SIZE

    }

    override fun createFragment(position: Int): Fragment {
        if (position == 0)
            return OngoingOrdersFragment()
        else
            return CompletedOrdersFragment()
    }
}