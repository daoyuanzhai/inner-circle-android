package com.innercircle.android;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.innercircle.android.utils.ImageUtils;

public class MainActivity extends FragmentActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private RelativeLayout layoutMainBackround;
    private Point screenSize;
    private Drawable drawableBackground;

    private MainFragment loginFragment;
    private MainFragment registerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Remove notification bar
        // this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Remove notification bar
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        final Display display = getWindowManager().getDefaultDisplay();
        screenSize = new Point();
        display.getSize(screenSize);
        Log.v(TAG, "Screen width: " + screenSize.x);
        Log.v(TAG, "Screen height: " + screenSize.y);

        final Bitmap background = ImageUtils.decodeSampledBitmapFromResource(getResources(),
                R.drawable.pch_sunset_background, screenSize.x, screenSize.y);
        Log.v(TAG, "Background width: " + background.getWidth());
        Log.v(TAG, "Background height: " + background.getHeight());

        drawableBackground = new BitmapDrawable(getResources(), background);

        setContentView(R.layout.activity_main);
        layoutMainBackround = (RelativeLayout) findViewById(R.id.layoutMainBackground);
        layoutMainBackround.setBackground(drawableBackground);

        loginFragment = new MainFragment();
        loginFragment.setLayoutId(R.layout.layout_login);

        registerFragment = new MainFragment();
        registerFragment.setLayoutId(R.layout.layout_register);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.layoutMainDetails) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                // return;
            }

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            loginFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.layoutMainDetails, loginFragment).commit();
        }
    }

    public void onClickGoRegister(View v) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.layoutMainDetails, registerFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
}
