package com.innercircle.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.innercircle.android.http.HttpRequestUtils;
import com.innercircle.android.model.InnerCircleRequest;
import com.innercircle.android.model.InnerCircleResponse;
import com.innercircle.android.thread.HandlerThreadPoolManager;
import com.innercircle.android.utils.Constants;
import com.innercircle.android.utils.Utils;

public class LoginActivity extends FragmentActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private Handler mainHandler;
    private HandlerThreadPoolManager handlerThreadPoolManager;
    private InnerCircleResponse response;
    private Runnable responseCallback;

    private EditText editTextLoginEmail;
    private EditText editTextLoginPassword;

    private TextView textViewError;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove automatic focus on the EditText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_login);

        textViewError = (TextView) findViewById(R.id.textViewLoginError);
        progressBar = (ProgressBar) findViewById(R.id.progressBarLogin);

        editTextLoginEmail = (EditText) findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = (EditText) findViewById(R.id.editTextLoginPassword);

        mainHandler = new Handler(this.getMainLooper());
        handlerThreadPoolManager = HandlerThreadPoolManager.getInstance();

        responseCallback = new Runnable(){
            @Override
            public void run() {
                final InnerCircleResponse.Status status = response.getStatus();
                Log.v(TAG, "login response status: " + status.toString());
                if (status == InnerCircleResponse.Status.SUCCESS) {
                    Intent registerIntent = new Intent(LoginActivity.this, CreateProfileActivity.class);
                    startActivity(registerIntent);
                } else if (status == InnerCircleResponse.Status.EMAIL_PASSWORD_MISMATCH) {
                    textViewError.setText(R.string.emailPasswordMismatch);
                    textViewError.setVisibility(View.VISIBLE);
                    Utils.hideSoftKeyboard(LoginActivity.this, editTextLoginEmail);
                } else {
                    textViewError.setText(R.string.loginError);
                    textViewError.setVisibility(View.VISIBLE);
                    Utils.hideSoftKeyboard(LoginActivity.this, editTextLoginEmail);
                }
                progressBar.setVisibility(View.GONE);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        textViewError.setVisibility(View.INVISIBLE);
    }

    public void onClickGoRegister(View v) {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    public void onClickLogin(View v) {
    	final String email = editTextLoginEmail.getText().toString();

        if (Utils.isValidEmail(email)) {
            final String password = editTextLoginPassword.getText().toString();

            final Runnable registerRunnable = new Runnable(){
                @Override
                public void run(){
                    final InnerCircleRequest request = (new InnerCircleRequest.Builder())
                            .setAPI(Constants.LOGIN_API)
                            .setNameValuePair(Constants.EMAIL, email)
                            .setNameValuePair(Constants.PASSWORD, password)
                            .build();
                    response = HttpRequestUtils.loginRequest(LoginActivity.this, request);
                    mainHandler.post(responseCallback);
                }
            };
            progressBar.setVisibility(View.VISIBLE);
            handlerThreadPoolManager.submitToBack(registerRunnable);
        } else {
            textViewError.setText(R.string.invalidEmail);
            textViewError.setVisibility(View.VISIBLE);
            Utils.hideSoftKeyboard(this, editTextLoginEmail);
        }
    }
}
