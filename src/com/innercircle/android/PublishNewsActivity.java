package com.innercircle.android;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.innercircle.android.model.InnerCircleToken;
import com.innercircle.android.utils.Constants;
import com.innercircle.android.utils.SharedPreferencesUtils;
import com.innercircle.android.utils.Utils;

public class PublishNewsActivity extends FragmentActivity {
    private static final String TAG = PublishNewsActivity.class.getSimpleName();

    private EditText editTextNewsContent;
    private TextView textViewError;
    private ImageButton[] imageButtonArray;
    private int imageIndex;
    private InnerCircleToken token;
    private int newsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Remove automatic focus on the EditText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_publish_news);

        editTextNewsContent = (EditText) findViewById(R.id.editTextNewsContent);
        imageButtonArray = new ImageButton[6];
        imageButtonArray[0] = (ImageButton) findViewById(R.id.imageButtonPic1);
        imageButtonArray[1] = (ImageButton) findViewById(R.id.imageButtonPic2);
        imageButtonArray[2] = (ImageButton) findViewById(R.id.imageButtonPic3);
        imageButtonArray[3] = (ImageButton) findViewById(R.id.imageButtonPic4);
        imageButtonArray[4] = (ImageButton) findViewById(R.id.imageButtonPic5);
        imageButtonArray[5] = (ImageButton) findViewById(R.id.imageButtonPic6);
        imageIndex = 0;

        token = SharedPreferencesUtils.getTokenFromPreferences(getApplicationContext());
        newsCount = SharedPreferencesUtils.getNewsCountFromPreferences(getApplicationContext());
    }

    @Override
    public void onBackPressed(){
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "requestCode: " + requestCode);
        Log.v(TAG, "resultCode: " + resultCode);

        if (resultCode == RESULT_OK || requestCode == Constants.INTENT_CODE_REQUEST_PICTURE) {
        	Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            imageButtonArray[imageIndex++].setImageBitmap(BitmapFactory.decodeFile(picturePath));
            imageButtonArray[imageIndex].setVisibility(View.VISIBLE);
        } else if (resultCode == RESULT_OK || requestCode == Constants.INTENT_CODE_REQUEST_PICTURE) {
        	
        }
    }

    public void onClickAddNewsPic(View v) {
        final String[] options = new String[]{getResources().getString(R.string.existingPics),
                getResources().getString(R.string.useCamera)};

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, options);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	Intent intent;
                switch (which) {
                case 0:
                    intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, Constants.INTENT_CODE_REQUEST_PICTURE);
                    break;
                case 1:
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, Constants.INTENT_CODE_REQUEST_CAMERA);
                    break;
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
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
            Utils.hideSoftKeyboard(getApplicationContext(), editTextNewsContent);
            return null;
        }
    }
}
