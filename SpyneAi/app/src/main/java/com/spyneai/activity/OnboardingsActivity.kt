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
import com.spyneai.R
import com.spyneai.extras.ZoomOutPageTransformer
import com.spyneai.fragment.OnboardingOneFragment
import com.spyneai.fragment.OnboardingThreeFragment
import com.spyneai.fragment.OnboardingTwoFragment
import com.spyneai.loginsignup.activity.LoginActivity
import com.spyneai.service.log
import kotlinx.android.synthetic.main.activity_onboardings.*


class OnboardingsActivity : AppCompatActivity() {

    private val NUM_PAGES = 3
    private lateinit var mPager: ViewPager
    private lateinit var tabLayout: TabLayout

    private var counts: Int = 0
    private var number: Int = 0

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
        log("countbeforeclick: "+counts)

        tvGet.setOnClickListener(View.OnClickListener {
            log("countonclick: "+counts)
            if (number == 0)
                counts=0
            when (counts) {
                0 -> {
                    mPager.setCurrentItem(1)
                    log("0")
                    log("count1:" +counts)
                    counts = 1
                    number++
                    //tvGet.setText(getString(R.string.get_started))
                }
                1 -> {
                    mPager.setCurrentItem(2)
                    log("1")
                    log("count2:" +counts)
                    counts = 2
                    tvGet.setText(getString(R.string.start_shooting))

                    //tvGet.setText(getString(R.string.get_started))
                }
                2 -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            }

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
                    counts = 0
                    tvGet.setText(getString(R.string.get_started))
                    return OnboardingOneFragment()
                }
                1 ->{
                    tvGet.setText(getString(R.string.get_started))
                    return OnboardingTwoFragment()
                }
                2 -> {
                    counts = 2
                    return OnboardingThreeFragment()
                }
            }
            throw IllegalStateException("position $position is invalid for this viewpager")
        }
    }
}