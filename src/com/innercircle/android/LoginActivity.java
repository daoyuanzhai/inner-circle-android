package com.innercircle.android;

import java.util.LinkedList;
import java.util.List;

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
import com.innercircle.android.model.InnerCircleCounter;
import com.innercircle.android.model.InnerCircleRequest;
import com.innercircle.android.model.InnerCircleResponse;
import com.innercircle.android.model.InnerCircleToken;
import com.innercircle.android.model.InnerCircleUser;
import com.innercircle.android.model.InnerCircleUserList;
import com.innercircle.android.thread.HandlerThreadPoolManager;
import com.innercircle.android.utils.Constants;
import com.innercircle.android.utils.SharedPreferencesUtils;
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

    private InnerCircleUser user;
    private InnerCircleToken token;
    private int newsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove automatic focus on the EditText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_login);

        editTextLoginEmail = (EditText) findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = (EditText) findViewById(R.id.editTextLoginPassword);
        textViewError = (TextView) findViewById(R.id.textViewLoginError);
        progressBar = (ProgressBar) findViewById(R.id.progressBarLogin);

        mainHandler = new Handler(this.getMainLooper());
        handlerThreadPoolManager = HandlerThreadPoolManager.getInstance();

        responseCallback = new Runnable(){
            @Override
            public void run() {
                final InnerCircleResponse.Status status = response.getStatus();
                Log.v(TAG, "getUserAccount response status: " + status.toString());
                if (status == InnerCircleResponse.Status.SUCCESS) {
                    newsCount = ((InnerCircleCounter) response.getData()).getCount();

                    SharedPreferencesUtils.saveUserToPreferences(getApplicationContext(), user);
                    SharedPreferencesUtils.saveTokenToPreferences(getApplicationContext(), token);
                    SharedPreferencesUtils.saveNewsCountToPreferences(getApplicationContext(), newsCount);

                    launchNextActivity();
                } else if (status == InnerCircleResponse.Status.EMAIL_PASSWORD_MISMATCH) {
                    textViewError.setText(R.string.emailPasswordMismatch);
                    textViewError.setVisibility(View.VISIBLE);
                    Utils.hideSoftKeyboard(getApplicationContext(), editTextLoginEmail);
                } else {
                    textViewError.setText(R.string.loginError);
                    textViewError.setVisibility(View.VISIBLE);
                    Utils.hideSoftKeyboard(getApplicationContext(), editTextLoginEmail);
                }
                progressBar.setVisibility(View.GONE);
            }
        };

        final boolean isLogin = SharedPreferencesUtils.getLoginStatuFromPreferences(getApplicationContext());
        if (isLogin) {
            final Intent intent = new Intent(getApplicationContext(), VeneziaActivity.class);
            startActivityForResult(intent, Constants.INTENT_CODE_VENEZIA);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        textViewError.setVisibility(View.INVISIBLE);

        // if a user profile already exists, populate the email so save user some typing
        user = SharedPreferencesUtils.getUserFromPreferences(getApplicationContext());
        if (null == user) {
            user = SharedPreferencesUtils.getUserFromPreferences(getApplicationContext());
        }
        if (null != user.getEmail()) {
            editTextLoginEmail.setText(user.getEmail());
        }
        editTextLoginPassword.setText(null);
    }

    public void onClickGoRegister(View v) {
        Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivityForResult(registerIntent, Constants.INTENT_CODE_REGISTER);
    }

    public void onClickLogin(View v) {
        final String email = editTextLoginEmail.getText().toString();
        if (!Utils.isValidEmail(email)) {
                textViewError.setText(R.string.invalidEmail);
                textViewError.setVisibility(View.VISIBLE);
                Utils.hideSoftKeyboard(getApplicationContext(), editTextLoginEmail);
                return;
        }
        final String password = editTextLoginPassword.getText().toString();
        if (password.isEmpty()) {
            textViewError.setText(R.string.enterPassword);
            textViewError.setVisibility(View.VISIBLE);
            Utils.hideSoftKeyboard(getApplicationContext(), editTextLoginEmail);
            return;
        }

        final Runnable loginRunnable = new Runnable(){
            @Override
            public void run(){
                final InnerCircleRequest request = (new InnerCircleRequest.Builder())
                        .setAPI(Constants.LOGIN_API)
                        .setNameValuePair(Constants.EMAIL, email)
                        .setNameValuePair(Constants.PASSWORD, password)
                        .build();
                response = HttpRequestUtils.loginRequest(getApplicationContext(), request);
                Log.v(TAG, "login response status: " + response.getStatus().toString());

                if (response.getStatus() == InnerCircleResponse.Status.SUCCESS) {
                    token = (InnerCircleToken) response.getData();

                    // after getting the token, fire a second call to get user profile
                    request.setAPI(Constants.GET_USER_ACCOUNT_API);
                    request.setNameValuePair(Constants.UID, token.getUid());
                    request.setNameValuePair(Constants.ACCESS_TOKEN, token.getAccessToken());

                    final List<String> uidList = new LinkedList<String>();
                    uidList.add(token.getUid());
                    request.setNameValuePair(Constants.OTHER_UIDS, Utils.uidJSONArrayBuilder(uidList));

                    response = HttpRequestUtils.getUserAccountsRequest(getApplicationContext(), request);
                    Log.v(TAG, "getUserAccount response status: " + response.getStatus().toString());

                    if (response.getStatus() == InnerCircleResponse.Status.SUCCESS) {
                        user = ((InnerCircleUserList) response.getData()).getUserList().get(0);

                        // after getting the user profile, fire a third call to get user news count
                        request.setAPI(Constants.GET_COUNTER_API);
                        request.setNameValuePair(Constants.RECEIVER_UID, "");
                        response = HttpRequestUtils.getCounterRequest(getApplicationContext(), request);
                    }
                }
                mainHandler.post(responseCallback);
            }
        };
        progressBar.setVisibility(View.VISIBLE);
        handlerThreadPoolManager.submitToBack(loginRunnable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "requestCode: " + requestCode);
        Log.v(TAG, "resultCode: " + resultCode);

        final boolean isLogin = SharedPreferencesUtils.getLoginStatuFromPreferences(getApplicationContext());
        if (resultCode == RESULT_OK && isLogin) {
            finish();
        }
    }

    private void launchNextActivity() {
        Intent intent;
        if ((user.getGender() == Constants.MALE || user.getGender() == Constants.FEMALE) &&
                (null !=user.getUsername() && !user.getUsername().isEmpty())) {
            Log.v(TAG, "user profile is complete, launching VeneziaActivity...");

            // only after the profile is complete, is the user considered login
            SharedPreferencesUtils.saveLoginStatusToPreferences(getApplicationContext(), true);

            intent = new Intent(getApplicationContext(), VeneziaActivity.class);
            startActivityForResult(intent, Constants.INTENT_CODE_VENEZIA);
        } else {
            Log.v(TAG, "gender/username is(are) missing, launching CreateProfileActivity...");

            intent = new Intent(getApplicationContext(), CreateProfileActivity.class);
            startActivityForResult(intent, Constants.INTENT_CODE_CREATE_PROFILE);
        }
    }
}
