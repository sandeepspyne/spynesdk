/*
package com.spyneai.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.spyneai.fragment.BackgroundFragment;
import com.spyneai.fragment.ChannelFragment;

import org.jetbrains.annotations.NotNull;

public class FilterAdapter extends FragmentStatePagerAdapter {
    int numOftabs;
    public FilterAdapter(FragmentManager supportFragmentManager, int tabCount ) {
        super(supportFragmentManager);
        numOftabs=tabCount;
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment=null;
        switch (position){
            case 0:
                fragment= new ChannelFragment(categoryId, subCategoryId);
                break;
            case 1:
                fragment= new BackgroundFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return numOftabs;
    }
}
*/
