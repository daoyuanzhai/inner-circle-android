package com.innercircle.android;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.android.camera.datastore.CropProfileImageAccesser;
import com.android.camera.datastore.ImageUtils;
import com.innercircle.android.model.InnerCircleToken;
import com.innercircle.android.utils.Constants;
import com.innercircle.android.utils.MediaStoreUtils;
import com.innercircle.android.utils.Utils;

public class CreateProfileActivity extends FragmentActivity{
    private static final String TAG = CreateProfileActivity.class.getSimpleName();

    private InnerCircleToken token;
    private CropProfileImageAccesser profileAccesser;

    private ImageButton imageButtonProfile;
    private CropImageIntentBuilder cropImage;
    private Uri cameraImageUri;

    private Point screenSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove automatic focus on the EditText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_create_profile);

        token = Utils.getTokenFromPreferences(this);
        profileAccesser = new CropProfileImageAccesser(this);
        profileAccesser.open();

        imageButtonProfile = (ImageButton) findViewById(R.id.imageButtonCreateProfile);
        getScreenSize();
        cropImage = new CropImageIntentBuilder(screenSize.x/4, screenSize.x/4);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "requestCode: " + requestCode);
        Log.v(TAG, "resultCode: " + resultCode);

        if (requestCode == Constants.REQUEST_PICTURE && resultCode == RESULT_OK) {
            cropImage.setSourceImage(data.getData());

            startActivityForResult(cropImage.getIntent(this, token.getUid()), Constants.CROP_PICTURE);

        } else if (requestCode == Constants.CROP_PICTURE && resultCode == RESULT_OK) {
            Log.v(TAG, "cropped image returned for uid: " + token.getUid());

            final byte[] imageData = profileAccesser.getImageByUID(token.getUid());
            final Drawable imageDrawable = ImageUtils.byteToDrawable(this, imageData);
            imageButtonProfile.setImageDrawable(imageDrawable);

        } else if (requestCode == Constants.REQUEST_CAMERA && resultCode == RESULT_OK) {
            Log.v(TAG, "picture taken for uid: " + token.getUid());

            // Store the picture to Android Media Store
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(cameraImageUri);
            sendBroadcast(intent);

            cropImage.setSourceImage(cameraImageUri);

            startActivityForResult(cropImage.getIntent(this, token.getUid()), Constants.CROP_PICTURE);
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
                    startActivityForResult(MediaStoreUtils.getPickImageIntent(CreateProfileActivity.this), Constants.REQUEST_PICTURE);
                    break;
                case 1:
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    File photo;
                    try {
                        // place where to store camera taken picture
                        photo = Utils.createTemporaryFile(CreateProfileActivity.this, "temp_picture", ".jpg");
                        photo.delete();
                        cameraImageUri = Uri.fromFile(photo);

                        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                        startActivityForResult(intent, Constants.REQUEST_CAMERA);
                    } catch(Exception e) {
                        Log.e(TAG, "Can't create file to take picture!");
                        Toast.makeText(CreateProfileActivity.this,
                                "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG).show();
                    }
                    break;
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getScreenSize() {
        final Display display = getWindowManager().getDefaultDisplay();
        screenSize = new Point();
        display.getSize(screenSize);
    }
}
