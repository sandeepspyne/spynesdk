package com.spyneai.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.spyneai.OnboardTwoActivity
import com.spyneai.R
import com.spyneai.adapter.HomeFragment
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.android.synthetic.main.activity_dashboard.*


class DashboardActivity : AppCompatActivity() {
    var fragment: Fragment? = null
    var fragmentManager: FragmentManager? = null
    var fragmentTransaction: FragmentTransaction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        showCoachMarks()
        Utilities.savePrefrence(
                this,
                AppConstants.FRAME_SHOOOTS,
                ""
        )
        Utilities.savePrefrence(this,
                AppConstants.SKU_ID,
                "")
/*
        Utilities.savePrefrence(
            this,
            AppConstants.tokenId,
            "C1i19DFuH"
        )
*/

        finishAllBacks()
        listeners()
        tvHome.performClick()
    }

    private fun showCoachMarks() {

    }

    //var viewListener = ViewListener { layoutInflater.inflate(R.layout.view_custom, null) }
    private fun finishAllBacks() {
        SplashActivity().finish()
        OnboardOneActivity().finish()
        OnboardTwoActivity().finish()
        OnboardThreeActivity().finish()
        SignInActivity().finish()
    }

    private fun listeners() {
        ivClicks.setOnClickListener(View.OnClickListener {
            val intent = Intent(applicationContext, CategoriesActivity::class.java)
            startActivity(intent)
        })

        tvHome.setOnClickListener(View.OnClickListener {
            fragment = HomeFragment(this)
            fragmentManager = supportFragmentManager
            fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction!!.replace(R.id.fragment_container_view, fragment!!)
            fragmentTransaction!!.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            fragmentTransaction!!.commit()
            setFooters(0)
        })

        tvNotifications.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                this,
                "Coming Soon!",
                Toast.LENGTH_SHORT
            ).show()
        })

        tvOrders.setOnClickListener(View.OnClickListener {
           /* fragment = OrdersFragment(this)
            fragmentManager = supportFragmentManager
            fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction!!.replace(R.id.fragment_container_view, fragment!!)
            fragmentTransaction!!.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            fragmentTransaction!!.commit()
            setFooters(2)*/
         //   setFooters(2)

            Toast.makeText(
                    this,
                    "Coming Soon !",
                    Toast.LENGTH_SHORT
            ).show()
        })

        tvProfile.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                this,
                "Coming Soon !",
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    fun setFooters(positionsClicked : Int) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            when (positionsClicked) {
                0 -> {
                    tvHome.setTextColor(getColor(R.color.primary))
                    tvNotifications.setTextColor(getColor(R.color.black))
                    tvOrders.setTextColor(getColor(R.color.black))
                    tvProfile.setTextColor(getColor(R.color.black))

                    tvHome.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.homes, 0, 0);
                    tvNotifications.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.bell, 0, 0);
                    tvOrders.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.order, 0, 0);
                    tvProfile.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.profile, 0, 0);
                }
                1 -> {
                    tvHome.setTextColor(getColor(R.color.black))
                    tvNotifications.setTextColor(getColor(R.color.primary))
                    tvOrders.setTextColor(getColor(R.color.black))
                    tvProfile.setTextColor(getColor(R.color.black))

                    tvHome.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.home, 0, 0);
                    tvNotifications.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.bells, 0, 0);
                    tvOrders.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.order, 0, 0);
                    tvProfile.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.profile, 0, 0);
                }
                2 -> {
                    tvHome.setTextColor(getColor(R.color.black))
                    tvNotifications.setTextColor(getColor(R.color.black))
                    tvOrders.setTextColor(getColor(R.color.primary))
                    tvProfile.setTextColor(getColor(R.color.black))

                    tvHome.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.home, 0, 0);
                    tvNotifications.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.bell, 0, 0);
                    tvOrders.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.orders, 0, 0);
                    tvProfile.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.profile, 0, 0);
                }
                3 -> {
                    tvHome.setTextColor(getColor(R.color.black))
                    tvNotifications.setTextColor(getColor(R.color.black))
                    tvOrders.setTextColor(getColor(R.color.black))
                    tvProfile.setTextColor(getColor(R.color.primary))

                    tvHome.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.home, 0, 0);
                    tvNotifications.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.bell, 0, 0);
                    tvOrders.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.order, 0, 0);
                    tvProfile.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.profiles, 0, 0);
                }
            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true);
        System.exit(1);
    }

}