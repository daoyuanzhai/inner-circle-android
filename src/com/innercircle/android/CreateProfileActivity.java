package com.innercircle.android;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

public class CreateProfileActivity extends FragmentActivity{
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove automatic focus on the EditText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_create_profile);
	}
}
