package com.spyneai.threesixty.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.spyneai.threesixty.ui.fragments.ThreeSixtySampleFragment

class ThreeSixtySampleAdapter (fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> ThreeSixtySampleFragment()
            1 -> ThreeSixtySampleFragment()
            2 -> ThreeSixtySampleFragment()
            else ->  ThreeSixtySampleFragment()
        }
    }

}