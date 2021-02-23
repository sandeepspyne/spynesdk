package com.spyneai.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.spyneai.fragment.BackgroundFragment
import com.spyneai.fragment.CarBackgroundFragment
import com.spyneai.fragment.ChannelFragment
import com.spyneai.fragment.LogoFragment
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class FiltersAdapters(context: Context,fm: FragmentManager,tabCount : Int,
                      catId : String, subCatId : String,categoryName : String) : FragmentStatePagerAdapter(fm) {
    var numOftabs = tabCount
    var categoryId = catId
    var subCategoryId = subCatId
    var contexts = context
    var categoryNames = categoryName

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null

        if (Utilities.getPreference(contexts,AppConstants.CATEGORY_NAME)!!.equals("Automobiles"))
        {
            when (position) {
                0 -> fragment = CarBackgroundFragment(contexts,categoryId,subCategoryId,categoryNames)
                1 -> fragment = LogoFragment(contexts,categoryId,subCategoryId,categoryNames)
            }
        }
        else {
            when (position) {
                0 -> fragment = ChannelFragment(contexts, categoryId, subCategoryId, categoryNames)
                1 -> fragment = BackgroundFragment(contexts, categoryId, subCategoryId, categoryNames)
            }
        }
        return fragment!!
    }

    override fun getCount(): Int {
        return numOftabs
    }
}