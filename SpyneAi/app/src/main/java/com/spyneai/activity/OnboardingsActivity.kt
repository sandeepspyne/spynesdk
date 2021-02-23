package com.spyneai.activity

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.spyneai.R
import com.spyneai.fragment.OnboardingOneFragment
import com.spyneai.fragment.OnboardingThreeFragment
import com.spyneai.fragment.OnboardingTwoFragment
import com.spyneai.needs.AppConstants


class OnboardingsActivity : AppCompatActivity() {

    private val NUM_PAGES = 3
    private lateinit var mPager: ViewPager
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboardings)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setPager();
    }

    private fun setPager() {
        mPager = findViewById(R.id.pager)
        tabLayout = findViewById(R.id.tab_layout)

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        mPager.adapter = pagerAdapter
      //  mPager.setPageTransformer(true, ZoomOutPageTransformer())

        tabLayout.setupWithViewPager(mPager, true)

        mPager.setCurrentItem(0)
        if (intent.getIntExtra(AppConstants.ONBOARD,0) == 2)
            mPager.setCurrentItem(1)
        else if (intent.getIntExtra(AppConstants.ONBOARD,0) == 3)
            mPager.setCurrentItem(2)
       /* else if (intent.getIntExtra(AppConstants.ONBOARD,0) == 1)
            mPager.setCurrentItem(0)*/

    }

    override fun onBackPressed() {
        if (mPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else  {
            mPager.setCurrentItem(0)
           /* if (intent.getIntExtra(AppConstants.ONBOARD,0) == 2)
            {
                mPager.setCurrentItem(1)
            }//mPager.setCurrentItem(0)
            else if (intent.getIntExtra(AppConstants.ONBOARD,0) == 3)
            {
                val intent = Intent(this, OnboardingsActivity::class.java)
                intent.putExtra(AppConstants.ONBOARD,2)
                startActivity(intent)
            }*/
                //mPager.setCurrentItem(1)
            // Otherwise, select the previous step.
           // mPager.currentItem = mPager.currentItem - 1
        }
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getCount(): Int = NUM_PAGES

        override fun getItem(position: Int) : Fragment {
            when (position) {
                0 -> return OnboardingOneFragment()
                1 -> return OnboardingTwoFragment()
                2 -> return OnboardingThreeFragment()
            }
            throw IllegalStateException("position $position is invalid for this viewpager")
        }
    }
}