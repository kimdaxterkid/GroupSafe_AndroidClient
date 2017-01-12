package edu.vt.scm.groupsafe;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {

    private int mNumOfTabs;
    private MainActivity activity;

    public PagerAdapter(MainActivity activity, FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.activity = activity;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                GroupFragment tab1 = new GroupFragment();
                activity.mGroupFragment = tab1;
                return tab1;
            case 1:
                MapFragment tab2 = new MapFragment();
                activity.mMapFragment = tab2;
                return tab2;
            case 2:
                ChatFragment tab3 = new ChatFragment();
                activity.mChatFragment = tab3;
                return tab3;
            case 3:
                SettingsFragment tab4 = new SettingsFragment();
                activity.mSettingsFragment = tab4;
                return tab4;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}