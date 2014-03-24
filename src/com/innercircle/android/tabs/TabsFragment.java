package com.innercircle.android.tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.innercircle.android.R;
import com.innercircle.android.utils.Constants;

public class TabsFragment extends Fragment implements OnTabChangeListener {
	private static final String TAG = TabsFragment.class.getSimpleName();

    private View root;
    private TabHost tabHost;
    private int currentTab;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * it is the root of the inflated XML file
         * in this case, just the fragment_tabs.xml
         */
        root = inflater.inflate(R.layout.fragment_tabs, null);
        /*
         * This object holds two children: a set of tab labels that the user clicks to select a specific tab,
         * and a FrameLayout object that displays the contents of that page.
         *
         * The individual elements are typically controlled using this container object,
         * rather than setting values on the child elements themselves.
         */
        tabHost = (TabHost) root.findViewById(android.R.id.tabhost);

        setupTabs();
        return root;
    }

    private void setupTabs() {
        // you must call this before adding your tabs!
        tabHost.setup();
        tabHost.addTab(newTab(Constants.NEWS_TAG, R.string.news_tab_label, R.drawable.news_icon, R.id.newsTab));
        tabHost.addTab(newTab(Constants.TALKS_TAG, R.string.talks_tab_label, R.drawable.talks_icon, R.id.talksTab));
        tabHost.addTab(newTab(Constants.DISCOVERY_TAG, R.string.discovery_tab_label, R.drawable.discovery_icon, R.id.discoveryTab));
        tabHost.addTab(newTab(Constants.SETTINGS_TAG, R.string.me_tab_label, R.drawable.settings_icon, R.id.settingsTab));
    }

    private TabSpec newTab (String tag, int labelId, int iconId, int tabContentId) {
        Log.d(TAG, "buildTab(): tag=" + tag);
        View indicator = LayoutInflater.from(getActivity())
                .inflate(R.layout.layout_tabs, (ViewGroup) root.findViewById(android.R.id.tabs), false);

        /*
         * ensure the control we're inflating is laid out properly. this will
         * cause our tab titles to be placed evenly weighted across the top.
         */
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        indicator.setLayoutParams(layoutParams);

        final TextView textViewTabText = (TextView) indicator.findViewById(R.id.text);
        textViewTabText.setText(labelId);
        textViewTabText.setGravity(Gravity.CENTER_HORIZONTAL);
        textViewTabText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, iconId);

        TabSpec tabSpec = tabHost.newTabSpec(tag);
        
        // Specify a label as the tab indicator.
        tabSpec.setIndicator(indicator);
        // Specify the id of the view that should be used as the content of the tab.
        tabSpec.setContent(tabContentId);
        return tabSpec;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        tabHost.setOnTabChangedListener(this);
        tabHost.setCurrentTab(currentTab);

        // manually start loading stuff in the first tab
        updateTab(Constants.SETTINGS_TAG, R.id.settingsTab);
    }

    @Override
    public void onTabChanged(String tabId) {
        Log.d(TAG, "onTabChanged(): tabId=" + tabId);
        if (Constants.NEWS_TAG.equals(tabId)) {
            updateTab(tabId, R.id.newsTab);
            currentTab = 0;
            return;
        }
        if (Constants.TALKS_TAG.equals(tabId)) {
            updateTab(tabId, R.id.talksTab);
            currentTab = 1;
            return;
        }
        if (Constants.DISCOVERY_TAG.equals(tabId)) {
            updateTab(tabId, R.id.discoveryTab);
            currentTab = 2;
            return;
        }
        if (Constants.SETTINGS_TAG.equals(tabId)) {
            updateTab(tabId, R.id.settingsTab);
            currentTab = 3;
            // refresh notification listView
            // ((NotificationFragment) FragmentFactory.getFragmentInstance(NOTIFICATION_TAG)).refreshListView();
            return;
        }
    }

    private void updateTab (String tabId, int placeHolder) {
        FragmentManager fm = getFragmentManager();
        if (null == fm.findFragmentByTag(tabId)) {
            fm.beginTransaction().replace(placeHolder, FragmentFactory.getFragmentInstance(tabId), tabId).commit();
        }
    }
}
