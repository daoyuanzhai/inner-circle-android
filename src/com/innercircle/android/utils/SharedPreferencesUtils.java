package com.innercircle.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.innercircle.android.model.InnerCircleToken;
import com.innercircle.android.model.InnerCircleUser;

public class SharedPreferencesUtils {
    private static final String TAG = SharedPreferencesUtils.class.getSimpleName();
    private SharedPreferencesUtils() {}

    public static void saveLoginStatusToPreferences(final Context context, boolean isLogin) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.USER_PREFERENCE, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.LOGIN_STATUS, isLogin);
        editor.commit();
        Log.v(TAG, "login status " + String.valueOf(isLogin) + " has been saved in the SharedPreferneces");
    }

    public static boolean getLoginStatuFromPreferences(final Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.USER_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constants.LOGIN_STATUS, false);
    }

    public static void saveUserToPreferences(final Context context, final InnerCircleUser user) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.USER_PREFERENCE, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(Constants.UID, user.getId());
        editor.putString(Constants.EMAIL, user.getEmail());
        editor.putString(Constants.PASSWORD, user.getPassword());
        editor.putString(Constants.GENDER, String.valueOf(user.getGender()));
        editor.putString(Constants.USERNAME, user.getUsername());
        editor.putString(Constants.VIP_CODE, user.getVIPCode());
        editor.commit();
        Log.v(TAG, "user profile has been saved in the SharedPreferneces");
    }

    public static InnerCircleUser getUserFromPreferences(final Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.USER_PREFERENCE, Context.MODE_PRIVATE);
        final String uid = sharedPreferences.getString(Constants.UID, null);
        final String email = sharedPreferences.getString(Constants.EMAIL, null);
        final String password = sharedPreferences.getString(Constants.PASSWORD, null);
        final char gender = sharedPreferences.getString(Constants.GENDER, "?").charAt(0);
        final String username = sharedPreferences.getString(Constants.USERNAME, null);
        final String VIPCode = sharedPreferences.getString(Constants.VIP_CODE, null);

        final InnerCircleUser user = (new InnerCircleUser.Builder())
                .setId(uid)
                .setEmail(email)
                .setPassword(password)
                .setGender(gender)
                .setVIPCode(VIPCode)
                .setUsername(username)
                .build();
        return user;
    }

    public static void saveTokenToPreferences(final Context context, final InnerCircleToken token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TOKEN_PREFERENCE, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(Constants.UID, token.getUid());
        editor.putString(Constants.ACCESS_TOKEN, token.getAccessToken());
        editor.putString(Constants.REFRESH_TOKEN, token.getRefreshToken());
        editor.putLong(Constants.TIMESTAMP, token.getTimestamp());
        editor.commit();
        Log.v(TAG, "user token has been saved in the SharedPreferneces");
    }

    public static InnerCircleToken getTokenFromPreferences(final Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TOKEN_PREFERENCE, Context.MODE_PRIVATE);
        final String uid = sharedPreferences.getString(Constants.UID, null);
        final String accessToken = sharedPreferences.getString(Constants.ACCESS_TOKEN, null);
        final String refreshToken = sharedPreferences.getString(Constants.REFRESH_TOKEN, null);
        final long timestamp = sharedPreferences.getLong(Constants.TIMESTAMP, 0);

        final InnerCircleToken token = (new InnerCircleToken.Builder())
                .setUid(uid)
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .setTimestamp(timestamp)
                .build();
        return token;
    }

    public static void saveNewsCountToPreferences(final Context context, final int count) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.COUNTER_PREFERENCE, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.COUNT, count);
        editor.commit();
        Log.v(TAG, "user news count has been saved in the SharedPreferneces");
    }

    public static int getNewsCountFromPreferences(final Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.COUNTER_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Constants.COUNT, 0);
    }
}
