package com.innercircle.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

public class MainFragment extends Fragment {
    private int layoutId;
    private ScrollView scrollView;

    public void setLayoutId(final int id) {
        this.layoutId = id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(this.layoutId, container, false);
        if (this.layoutId == R.layout.layout_login) {
            scrollView = (ScrollView) v.findViewById(R.id.scrollViewLogin);
        } else {
            scrollView = (ScrollView) v.findViewById(R.id.scrollViewRegister);
        }
        if (null == scrollView) {
            Log.v("test", "scrollView is null");
        }
        return v;
    }

    public ScrollView getScollView() {
        return this.scrollView;
    }
}
