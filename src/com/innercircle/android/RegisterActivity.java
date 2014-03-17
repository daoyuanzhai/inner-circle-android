package com.innercircle.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
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
import com.innercircle.android.model.InnerCircleToken;
import com.innercircle.android.thread.HandlerThreadPoolManager;
import com.innercircle.android.utils.Constants;
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

        mainHandler = new Handler(this.getMainLooper());
        handlerThreadPoolManager = HandlerThreadPoolManager.getInstance();

        textViewError = (TextView) findViewById(R.id.textViewRegisterError);
        progressBar = (ProgressBar) findViewById(R.id.progressBarRegister);

        editTextRegisterEmail = (EditText) findViewById(R.id.editTextRegisterEmail);
        editTextRegisterPassword = (EditText) findViewById(R.id.editTextRegisterPassword);
        editTextVIPCode = (EditText) findViewById(R.id.editTextVIPCode);

        responseCallback = new Runnable(){
            @Override
            public void run() {
                final InnerCircleResponse.Status status = response.getStatus();
                Log.v(TAG, "register response status: " + status.toString());
                if (status == InnerCircleResponse.Status.SUCCESS) {
                    final InnerCircleToken token = (InnerCircleToken) response.getData();
                    Utils.saveTokenToPreferences(RegisterActivity.this, token);

                    Intent registerIntent = new Intent(RegisterActivity.this, CreateProfileActivity.class);
                    startActivity(registerIntent);
                } else if (status == InnerCircleResponse.Status.EMAIL_EXISTS_ERROR) {
                    textViewError.setText(R.string.emailExists);
                    textViewError.setVisibility(View.VISIBLE);
                    hideSoftKeyboard();
                } else {
                    textViewError.setText(R.string.registerError);
                    textViewError.setVisibility(View.VISIBLE);
                    hideSoftKeyboard();
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
        finish();
    }

    public void onClickGoLogin(View v) {
        finish();
    }

    public void onClickRegister(View v) {
        final String email = editTextRegisterEmail.getText().toString();

        if (Utils.isValidEmail(email)) {
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
                    response = HttpRequestUtils.registerRequest(request);
                    mainHandler.post(responseCallback);
                }
            };
            progressBar.setVisibility(View.VISIBLE);
            handlerThreadPoolManager.submitToBack(registerRunnable);
        } else {
            textViewError.setText(R.string.invalidEmail);
            textViewError.setVisibility(View.VISIBLE);
            hideSoftKeyboard();
        }
    }

    private void hideSoftKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextRegisterEmail.getWindowToken(), 0);
    }
}
