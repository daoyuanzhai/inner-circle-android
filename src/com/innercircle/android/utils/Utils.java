package com.innercircle.android.utils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.innercircle.android.model.InnerCircleToken;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    private Utils() {}

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            inSampleSize = 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((height / inSampleSize) > reqHeight
                    && (width / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
            int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static boolean isValidEmail(final String email) {
        final Pattern p = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        final Matcher m = p.matcher(email);
        return m.matches();
    }

    public static void saveTokenToPreferences(final Context context, final InnerCircleToken token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.TOKEN_PREFERENCE, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putString(Constants.UID, token.getUid());
        editor.putString(Constants.ACCESS_TOKEN, token.getAccessToken());
        editor.putString(Constants.REFRESH_TOKEN, token.getRefreshToken());
        editor.putLong(Constants.TIMESTAMP, token.getTimestamp());
        editor.commit();
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

    public static File createTemporaryFile(Context context, String part, String ext) throws Exception {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists()) {
            tempDir.mkdir();
        }
        return File.createTempFile(part, ext, tempDir);
    }

    public static Bitmap grabImageBitmap(Context context, Uri imageUri) {
        context.getContentResolver().notifyChange(imageUri, null);
        ContentResolver cr = context.getContentResolver();
        Bitmap bitmap;
        try {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, imageUri);
            return bitmap;
        } catch (Exception e) {
            Toast.makeText(context, "Failed to load", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to load " + imageUri.toString() + ": " + e.toString());
            return null;
        }
    }

    public static void hideSoftKeyboard(final Context context, final EditText editText){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
