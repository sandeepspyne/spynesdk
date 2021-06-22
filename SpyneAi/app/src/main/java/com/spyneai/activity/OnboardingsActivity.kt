package com.spyneai.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.captureEvent
import com.spyneai.extras.ZoomOutPageTransformer
import com.spyneai.fragment.OnboardingOneFragment
import com.spyneai.fragment.OnboardingThreeFragment
import com.spyneai.fragment.OnboardingTwoFragment
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.posthog.Events
import com.spyneai.service.log
import kotlinx.android.synthetic.main.activity_onboardings.*


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
        mPager.setPageTransformer(true, ZoomOutPageTransformer())

        tabLayout.setupWithViewPager(mPager, true)
        mPager.setCurrentItem(0)


        tvGet.setOnClickListener(View.OnClickListener {

            when (tabLayout.selectedTabPosition) {
                0 -> mPager.currentItem = 1
                1 -> mPager.currentItem = 2
                2 -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        })

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {

                captureEvent(
                    Events.SLIDE_CHANGE,
                    Properties().putValue("position",tab.position))

                when(tab.position) {
                    0,1 ->  tvGet.text = getString(R.string.get_started)
                    2 ->  tvGet.text = getString(R.string.start_shooting)

                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    override fun onBackPressed() {
        if (mPager.currentItem == 0) {
            super.onBackPressed()
        } else  {
            mPager.setCurrentItem(0)
        }
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {


        override fun getCount(): Int = NUM_PAGES

        override fun getItem(position: Int) : Fragment {
            when (position) {
                0 -> {
                    return OnboardingOneFragment()
                }
                1 -> {
                    return OnboardingTwoFragment()
                }
                2 -> {
                    return OnboardingThreeFragment()
                }
            }
            throw IllegalStateException("position $position is invalid for this viewpager")
        }
    }
}