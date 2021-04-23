package com.spyneai.videorecording.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class FragmentTestPagerAdapter(fa: FragmentManager,val fragmentList : ArrayList<Fragment>) : FragmentPagerAdapter(fa) {
    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList.get(position)
    }
}