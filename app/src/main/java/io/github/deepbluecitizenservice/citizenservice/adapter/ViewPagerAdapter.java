package io.github.deepbluecitizenservice.citizenservice.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;

import java.util.ArrayList;
import java.util.List;

import io.github.deepbluecitizenservice.citizenservice.fragments.AllViewFragment;
import io.github.deepbluecitizenservice.citizenservice.fragments.HomeFragment;
import io.github.deepbluecitizenservice.citizenservice.fragments.PhotoFragment;
import io.github.deepbluecitizenservice.citizenservice.fragments.SettingsFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragmentList = new ArrayList<>();
    private Fragment currentFragment;
    private AHBottomNavigation bottomNavigation;

    public ViewPagerAdapter(FragmentManager fm, AHBottomNavigation navigation) {
        super(fm);

        this.bottomNavigation = navigation;

        fragmentList.clear();
        fragmentList.add(new HomeFragment());
        fragmentList.add(new AllViewFragment());
        fragmentList.add(new PhotoFragment());
        fragmentList.add(new SettingsFragment());
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (currentFragment != object) {
            currentFragment = (Fragment) object;
        }
        bottomNavigation.setCurrentItem(position);
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
