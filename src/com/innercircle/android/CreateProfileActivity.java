package com.innercircle.android;

import java.io.File;
import java.io.FileOutputStream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.camera.CropImageIntentBuilder;
import com.android.camera.datastore.CropProfileImageAccesser;
import com.android.camera.datastore.ImageUtils;
import com.innercircle.android.http.HttpRequestUtils;
import com.innercircle.android.model.InnerCircleRequest;
import com.innercircle.android.model.InnerCircleResponse;
import com.innercircle.android.model.InnerCircleToken;
import com.innercircle.android.model.InnerCircleUser;
import com.innercircle.android.thread.HandlerThreadPoolManager;
import com.innercircle.android.utils.Constants;
import com.innercircle.android.utils.MediaStoreUtils;
import com.innercircle.android.utils.SharedPreferencesUtils;
import com.innercircle.android.utils.Utils;

public class CreateProfileActivity extends FragmentActivity{
    private static final String TAG = CreateProfileActivity.class.getSimpleName();

    private InnerCircleToken token;
    private InnerCircleUser user;
    private CropProfileImageAccesser profileAccesser;

    private CropImageIntentBuilder cropImage;
    private Uri cameraImageUri;

    private Point screenSize;

    private ImageButton imageButtonProfile;
    private CheckBox checkBoxMale;
    private CheckBox checkBoxFemale;
    private TextView textViewError;
    private EditText editTextUsername;
    private ProgressBar progressBar;

    private Handler mainHandler;
    private HandlerThreadPoolManager handlerThreadPoolManager;
    private InnerCircleResponse response;
    private Runnable responseCallback;

    private String username;
    private char gender;

    private boolean newPicSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove automatic focus on the EditText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_create_profile);

        imageButtonProfile = (ImageButton) findViewById(R.id.imageButtonCreateProfile);
        checkBoxMale = (CheckBox) findViewById(R.id.checkBoxMale);
        checkBoxFemale = (CheckBox) findViewById(R.id.checkBoxFemale);
        textViewError = (TextView) findViewById(R.id.textViewGoAppError);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        progressBar = (ProgressBar) findViewById(R.id.progressBarCreateProfile);

        token = SharedPreferencesUtils.getTokenFromPreferences(getApplicationContext());

        profileAccesser = new CropProfileImageAccesser(getApplicationContext());
        profileAccesser.open();

        newPicSelected = false;
        getScreenSize();
        cropImage = new CropImageIntentBuilder(screenSize.x/4, screenSize.x/4);

        // Set profile picture if already saved in SDCard
        byte[] imageData = profileAccesser.getImageByUID(token.getUid());
        if (null != imageData && imageData.length >0) {
            Drawable imageDrawable = ImageUtils.byteToDrawable(getApplicationContext(), imageData);
            imageButtonProfile.setImageDrawable(imageDrawable);
            imageDrawable = null;
        }
        imageData = null;
        cameraImageUri = getTempFileUri();
        Log.v(TAG, "temp file path: " + cameraImageUri.getPath());

        final OnCheckedChangeListener listener = new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                if(isChecked){
                    switch(arg0.getId()) {
                    case R.id.checkBoxMale:
                        checkBoxMale.setChecked(true);
                        checkBoxFemale.setChecked(false);
                        break;
                    case R.id.checkBoxFemale:
                        checkBoxMale.setChecked(false);
                        checkBoxFemale.setChecked(true);
                        break;
                    }
                }
            }
        };
        checkBoxMale.setOnCheckedChangeListener(listener);
        checkBoxFemale.setOnCheckedChangeListener(listener);

        mainHandler = new Handler(this.getMainLooper());
        handlerThreadPoolManager = HandlerThreadPoolManager.getInstance();

        responseCallback = new Runnable(){
            @Override
            public void run() {
                final InnerCircleResponse.Status status = response.getStatus();
                if (status == InnerCircleResponse.Status.SUCCESS) {
                    Log.v(TAG, "profile created successfully");

                    Intent registerIntent = new Intent(getApplicationContext(), VeneziaActivity.class);
                    startActivityForResult(registerIntent, Constants.INTENT_CODE_VENEZIA);
                } else {
                    textViewError.setText(R.string.createProfileError);
                    textViewError.setVisibility(View.VISIBLE);
                    Utils.hideSoftKeyboard(getApplicationContext(), editTextUsername);
                }
                progressBar.setVisibility(View.GONE);
            }
        };
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "requestCode: " + requestCode);
        Log.v(TAG, "resultCode: " + resultCode);

        final boolean isLogin = SharedPreferencesUtils.getLoginStatuFromPreferences(getApplicationContext());
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.INTENT_CODE_VENEZIA && isLogin) {
                setResult(RESULT_OK);
                finish();

            } else if (requestCode == Constants.INTENT_CODE_REQUEST_CAMERA) {
                Log.v(TAG, "a picture has been taken for uid: " + token.getUid());

                // Store the picture to Android Media Store
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(cameraImageUri);
                sendBroadcast(intent);

                // start the CropImage Activity to crop the taken picture
                cropImage.setSourceImage(cameraImageUri);
                startActivityForResult(cropImage.getIntent(getApplicationContext(), token.getUid()), Constants.INTENT_CODE_CROP_PICTURE);

            } else if (requestCode == Constants.INTENT_CODE_REQUEST_PICTURE) {
                // start the CropImage Activity to crop the selected picture
                cropImage.setSourceImage(data.getData());
                startActivityForResult(cropImage.getIntent(getApplicationContext(), token.getUid()), Constants.INTENT_CODE_CROP_PICTURE);

            } else if (requestCode == Constants.INTENT_CODE_CROP_PICTURE) {
                Log.v(TAG, "a cropped image has been returned for uid: " + token.getUid());

                byte[] imageData = profileAccesser.getImageByUID(token.getUid());
                Drawable imageDrawable = ImageUtils.byteToDrawable(getApplicationContext(), imageData);
                imageButtonProfile.setImageDrawable(imageDrawable);

                // store the cropped picture on a temporary file so that it can be uploaded later
                final File file = new File(cameraImageUri.getPath());
                FileOutputStream outStream;
                try {
                    outStream = new FileOutputStream(file);
                    outStream.write(imageData);
                    outStream.flush();
                    outStream.close();
                } catch (Exception e) {
                    Log.v(TAG, e.toString());
                }

                // release resources
                imageData = null;
                imageDrawable = null;

                newPicSelected = true;
            }
        }
    }

    public void onClickAddProfileImage(View v) {
        final String[] options = new String[]{getResources().getString(R.string.existingPics),
                getResources().getString(R.string.useCamera)};

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, options);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                case 0:
                    startActivityForResult(MediaStoreUtils.getPickImageIntent(CreateProfileActivity.this), Constants.INTENT_CODE_REQUEST_PICTURE);
                    break;
                case 1:
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                    startActivityForResult(intent, Constants.INTENT_CODE_REQUEST_CAMERA);
                    break;
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onClickGoApp(View v) {
        if (checkBoxMale.isChecked()) {
            gender = Constants.MALE;
        } else if (checkBoxFemale.isChecked()) {
            gender = Constants.FEMALE;
        } else {
            textViewError.setText(getResources().getString(R.string.genderEmpty));
            textViewError.setVisibility(View.VISIBLE);
            Utils.hideSoftKeyboard(getApplicationContext(), editTextUsername);
            return;
        }

        username = editTextUsername.getText().toString();
        if (username.isEmpty()) {
            textViewError.setText(getResources().getString(R.string.nicknameEmpty));
            textViewError.setVisibility(View.VISIBLE);
            Utils.hideSoftKeyboard(getApplicationContext(), editTextUsername);
            return;
        }

        final Runnable createProfileRunnable = new Runnable(){
            @Override
            public void run(){
                final InnerCircleRequest request = (new InnerCircleRequest.Builder())
                        .setAPI(Constants.SET_GENDER_API)
                        .setNameValuePair(Constants.UID, token.getUid())
                        .setNameValuePair(Constants.ACCESS_TOKEN, token.getAccessToken())
                        .setNameValuePair(Constants.GENDER, String.valueOf(gender))
                        .build();
                // setGender request
                response = HttpRequestUtils.setGenderRequest(getApplicationContext(), request);

                // setUsername request
                if (response.getStatus() == InnerCircleResponse.Status.SUCCESS) {
                    request.setAPI(Constants.SET_USERNAME_API);
                    request.setNameValuePair(Constants.USERNAME, username);
                    response = HttpRequestUtils.setUsernameRequest(getApplicationContext(), request);

                    if (response.getStatus() == InnerCircleResponse.Status.SUCCESS) {
                        // user profile should be complete by now
                        user = (InnerCircleUser) response.getData();
                        SharedPreferencesUtils.saveUserToPreferences(getApplicationContext(), user);
                        SharedPreferencesUtils.saveLoginStatusToPreferences(getApplicationContext(), true);

                        if (newPicSelected) {
                            request.setAPI(Constants.FILE_UPLOAD_API);
                            request.setNameValuePair(Constants.FILENAME, token.getUid());
                            request.setNameValuePair(Constants.IMAGE_USAGE, Constants.IMAGE_USAGE_FOR_SETTINGS);
                            response = HttpRequestUtils.fileUploadRequest(getApplicationContext(), request, cameraImageUri);
                        }
                        newPicSelected = false;
                    }
                }
                mainHandler.post(responseCallback);
            }
        };
        progressBar.setVisibility(View.VISIBLE);
        handlerThreadPoolManager.submitToBack(createProfileRunnable);
    }

    // can't move this to Utils class
    private void getScreenSize() {
        final Display display = getWindowManager().getDefaultDisplay();
        screenSize = new Point();
        display.getSize(screenSize);
    }

    private Uri getTempFileUri() {
        try {
            // place where to store taken/selected picture
            final File photo = Utils.createTemporaryFile(getApplicationContext(), token.getUid(), ".png");
            photo.delete();
            return Uri.fromFile(photo);

        } catch(Exception e) {
            Log.e(TAG, "Can't create file to manipulate photos!");
            textViewError.setText(R.string.insertSDCard);
            textViewError.setVisibility(View.VISIBLE);
            Utils.hideSoftKeyboard(getApplicationContext(), editTextUsername);
            return null;
        }
    }
}
