package com.innercircle.android.tabs;

import android.support.v4.app.Fragment;

import com.innercircle.android.utils.Constants;

public class FragmentFactory {
    private static final NewsFragment newsFragment = new NewsFragment();
    private static final TalksFragment talksFragment = new TalksFragment();
    private static final DiscoveryFragment discoveryFragment = new DiscoveryFragment();
    private static final SettingsFragment settingsFragment = new SettingsFragment();

    private FragmentFactory () {
    }

    public static Fragment getFragmentInstance (final String tag) {
        Fragment instance = null;
        if (tag.equals(Constants.NEWS_TAG)) {
            instance = newsFragment;
        } else if (tag.equals(Constants.TALKS_TAG)) {
            instance = talksFragment;
        } else if (tag.equals(Constants.DISCOVERY_TAG)) {
            instance = discoveryFragment;
        } else if (tag.equals(Constants.SETTINGS_TAG)) {
            instance = settingsFragment;
        }
        return instance;
    }
}