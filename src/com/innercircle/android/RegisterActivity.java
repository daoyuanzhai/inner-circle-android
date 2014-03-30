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
import com.innercircle.android.model.InnerCircleToken;
import com.innercircle.android.thread.HandlerThreadPoolManager;
import com.innercircle.android.utils.Constants;
import com.innercircle.android.utils.SharedPreferencesUtils;
import com.innercircle.android.utils.Utils;

public class RegisterActivity extends FragmentActivity{
    private static final String TAG = LoginActivity.class.getSimpleName();

    private Handler mainHandler;
    private HandlerThreadPoolManager handlerThreadPoolManager;
    private InnerCircleResponse response;
    private Runnable responseCallback;

    private EditText editTextRegisterEmail;
    private EditText editTextRegisterPassword;
    private EditText editTextVIPCode;
    private TextView textViewError;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove automatic focus on the EditText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_register);

        editTextRegisterEmail = (EditText) findViewById(R.id.editTextRegisterEmail);
        editTextRegisterPassword = (EditText) findViewById(R.id.editTextRegisterPassword);
        editTextVIPCode = (EditText) findViewById(R.id.editTextVIPCode);
        textViewError = (TextView) findViewById(R.id.textViewRegisterError);
        progressBar = (ProgressBar) findViewById(R.id.progressBarRegister);

        mainHandler = new Handler(this.getMainLooper());
        handlerThreadPoolManager = HandlerThreadPoolManager.getInstance();

        responseCallback = new Runnable(){
            @Override
            public void run() {
                final InnerCircleResponse.Status status = response.getStatus();
                Log.v(TAG, "register response status: " + status.toString());
                if (status == InnerCircleResponse.Status.SUCCESS) {
                    // save token to SharedPreferences
                    SharedPreferencesUtils.saveTokenToPreferences(getApplicationContext(), (InnerCircleToken) response.getData());

                    Intent registerIntent = new Intent(getApplicationContext(), CreateProfileActivity.class);
                    startActivityForResult(registerIntent, Constants.INTENT_CODE_CREATE_PROFILE);
                } else if (status == InnerCircleResponse.Status.EMAIL_EXISTS_ERROR) {
                    textViewError.setText(R.string.emailExists);
                    textViewError.setVisibility(View.VISIBLE);
                    Utils.hideSoftKeyboard(RegisterActivity.this, editTextRegisterEmail);
                } else {
                    textViewError.setText(R.string.registerError);
                    textViewError.setVisibility(View.VISIBLE);
                    Utils.hideSoftKeyboard(RegisterActivity.this, editTextRegisterEmail);
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

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "requestCode: " + requestCode);
        Log.v(TAG, "resultCode: " + resultCode);

        if (resultCode == RESULT_OK && null != data && data.getExtras().getBoolean(Constants.IS_FROM_VENEZIA)) {
            setResult(RESULT_OK);
            finish();
        }
    }
    public void onClickGoLogin(View v) {
        setResult(RESULT_OK);
        finish();
    }

    public void onClickRegister(View v) {
        final String email = editTextRegisterEmail.getText().toString();
        if (!Utils.isValidEmail(email)) {
            textViewError.setText(R.string.invalidEmail);
            textViewError.setVisibility(View.VISIBLE);
            Utils.hideSoftKeyboard(this, editTextRegisterEmail);
            return;
        }

        final String password = editTextRegisterPassword.getText().toString();
        if (password.isEmpty()) {
            textViewError.setText(R.string.enterPassword);
            textViewError.setVisibility(View.VISIBLE);
            Utils.hideSoftKeyboard(this, editTextRegisterEmail);
            return;
        }

        final String VIPCode = editTextVIPCode.getText().toString();
        if (VIPCode.isEmpty()) {
            textViewError.setText(R.string.enterVIPCode);
            textViewError.setVisibility(View.VISIBLE);
            Utils.hideSoftKeyboard(this, editTextRegisterEmail);
            return;
        }

        final Runnable registerRunnable = new Runnable(){
            @Override
            public void run(){
                final InnerCircleRequest request = (new InnerCircleRequest.Builder())
                        .setAPI(Constants.REGISTER_API)
                        .setNameValuePair(Constants.EMAIL, email)
                        .setNameValuePair(Constants.PASSWORD, password)
                        .setNameValuePair(Constants.VIP_CODE, VIPCode)
                        .build();
                response = HttpRequestUtils.registerRequest(RegisterActivity.this, request);
                mainHandler.post(responseCallback);
            }
        };
        progressBar.setVisibility(View.VISIBLE);
        handlerThreadPoolManager.submitToBack(registerRunnable);
    }
}
