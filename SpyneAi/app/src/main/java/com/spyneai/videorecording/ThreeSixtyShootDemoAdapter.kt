package com.spyneai.videorecording

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter

class ThreeSixtyShootDemoAdapter(fa: FragmentActivity,val fragmentList : ArrayList<Fragment>)  : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return 2;
    }


    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        if (position == 0){
            return fragmentList.get(0)
        }else{
            return fragmentList.get(1)
        }
    }

}