package com.innercircle.android;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

public class VeneziaActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_venezia);
    }

    @Override
    public void onBackPressed(){
        setResult(RESULT_OK);
        finish();
    }
}
