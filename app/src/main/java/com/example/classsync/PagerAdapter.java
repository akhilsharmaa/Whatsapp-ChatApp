package com.example.classsync;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {

    int tabCount;


    public PagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);

        tabCount = behavior;

    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return new ChatFragment();
            case 1:
                return new StatusFragment();
            case 2:
                return new CallFragment();

            default: return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
