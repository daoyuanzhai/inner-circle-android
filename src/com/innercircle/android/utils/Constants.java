package com.innercircle.android.utils;

public class Constants {
    private Constants(){}

    public enum Gender {
        M, F
    }

    public static final String LAYOUT_ID = "layoutId";

    // API names
    public static final String REGISTER_API = "register";
    public static final String LOGIN_API = "login";
    public static final String REFRESH_ACCESS_TOKEN_API = "refreshAccessToken";
    public static final String SET_GENDER_API = "setGender";

    // HTTP Request Params
    public static final String SERVICES_CONSOLE = "http://192.241.202.169:8080/ServicesConsole/";
    public static final String UID = "uid";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String TIMESTAMP = "timestamp";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String VIPCode = "VIPCode";
    public static final String GENDER = "gender";

    // HTTP Response Params
    public static final String STATUS = "status";
    public static final String DATA = "data";

    // SharedPreferences
    public static final String TOKEN_PREFERENCE = "tokenPreference";

    // Images
    public static int REQUEST_PICTURE = 1;
    public static int CROP_PICTURE = 2;
    public static int REQUEST_CAMERA = 3;
}
