package com.spyneai.videorecording.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter

class ThreeSixtyShootDemoAdapter(fa: FragmentActivity,val fragmentList : ArrayList<Fragment>)  : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return 3
    }


    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        return fragmentList.get(position)
    }

}