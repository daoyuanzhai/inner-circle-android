package com.innercircle.android;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.innercircle.android.http.HttpRequestUtils;
import com.innercircle.android.model.InnerCircleRequest;
import com.innercircle.android.model.InnerCircleResponse;
import com.innercircle.android.thread.HandlerThreadPoolManager;
import com.innercircle.android.utils.Constants;
import com.innercircle.android.utils.Utils;

public class MainActivity extends FragmentActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private MainFragment loginFragment;
    private MainFragment registerFragment;

    private Handler mainHandler;
    private HandlerThreadPoolManager handlerThreadPoolManager;

    private EditText editTextRegisterEmail;
    private EditText editTextRegisterPassword;
    private EditText editTextVIPCode;

    private TextView textViewInvalidEmail;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove automatic focus on the EditText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_main);

        mainHandler = new Handler(this.getMainLooper());
        handlerThreadPoolManager = HandlerThreadPoolManager.getInstance();

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

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume called.");
    }

    public void onClickGoRegister(View v) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(loginFragment);
        transaction.add(R.id.layoutMainDetails, registerFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onClickGoLogin(View v) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(registerFragment);
        transaction.add(R.id.layoutMainDetails, loginFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onClickRegister(View v) {
        editTextRegisterEmail = (EditText) findViewById(R.id.editTextRegisterEmail);
        final String email = editTextRegisterEmail.getText().toString();

        if (Utils.isValidEmail(email)) {
            editTextRegisterPassword = (EditText) findViewById(R.id.editTextRegisterPassword);
            editTextVIPCode = (EditText) findViewById(R.id.editTextVIPCode);

            final String password = editTextRegisterPassword.getText().toString();
            final String VIPCode = editTextVIPCode.getText().toString();

            final Runnable registerRunnable = new Runnable(){
                @Override
                public void run(){
                    final InnerCircleRequest request = (new InnerCircleRequest.Builder())
                            .setAPI(Constants.REGISTER_API)
                            .setNameValuePair(Constants.EMAIL, email)
                            .setNameValuePair(Constants.PASSWORD, password)
                            .setNameValuePair(Constants.VIPCode, VIPCode)
                            .build();
                    final InnerCircleResponse response = HttpRequestUtils.registerRequest(request);
                    final InnerCircleResponse.Status status = response.getStatus();

                    if (status == InnerCircleResponse.Status.SUCCESS) {
                        Log.v(TAG, "successful");
                    } else if (status == InnerCircleResponse.Status.EMAIL_EXISTS_ERROR) {
                        Log.v(TAG, "this email already exists");
                    } else {
                        Log.v(TAG, "an error has occurred");
                    }
                }
            };
            handlerThreadPoolManager.submitToBack(registerRunnable);
        } else {
            textViewInvalidEmail = (TextView) findViewById(R.id.textViewInvalidEmail);
            textViewInvalidEmail.setVisibility(View.VISIBLE);

            // hide soft keyboard
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editTextRegisterEmail.getWindowToken(), 0);
        }
    }
}
