package hse.beryukhov.quidproquo.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;

import hse.beryukhov.quidproquo.Application;
import hse.beryukhov.quidproquo.R;

public class UserPageActivityNew extends Activity {
    private ParseImageView userPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page_new);


        EditText userName = (EditText) findViewById(R.id.userNametextView);
        userName.setText(ParseUser.getCurrentUser().getUsername());

        EditText userEmail = (EditText) findViewById(R.id.userEmailtextView);
        userEmail.setText(ParseUser.getCurrentUser().getEmail());

        ImageButton backButton = (ImageButton) findViewById(R.id.backToMainButtonU);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
        // Log out button click handler

        ImageView logoutButton = (ImageView) findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(UserPageActivityNew.this);
                                                builder.setMessage(R.string.dialog_logout_message)
                                                        .setTitle(R.string.dialog_logout_title)
                                                        .setPositiveButton(R.string.logOutButton, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                ParseUser.logOut();
                                                                dialog.dismiss();
                                                                Intent intent = new Intent(UserPageActivityNew.this, DispatchActivity.class);
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                startActivity(intent);
                                                            }
                                                        })
                                                        .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        });
                                                AlertDialog dialog = builder.create();
                                                dialog.show();
                                            }
                                        }
        );
        final EditText telegram = (EditText) findViewById(R.id.telegramEditText);
        String telegram_acc=(String)ParseUser.getCurrentUser().get(Application.TELEGRAM_ACCOUNT);
        if (telegram_acc!=null){
            telegram.setText(telegram_acc);
        }

        ImageView tick = (ImageView) findViewById(R.id.tickImageView);
        tick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo
                if (telegram.getText().toString() != "") {
                    ParseUser.getCurrentUser().put(Application.TELEGRAM_ACCOUNT, telegram.getText().toString());
                    ParseUser.getCurrentUser().saveInBackground();
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });

        userPic = (ParseImageView) findViewById(R.id.userPicImageViewfr);
        ParseFile photoFile = (ParseFile) ParseUser.getCurrentUser().get(Application.USER_PHOTO);
        if (photoFile != null) {
            userPic.setParseFile(photoFile);
            userPic.loadInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    userPic.setVisibility(View.VISIBLE);
                }
            });
        }
        userPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Button userPic", "Pushed");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


                //startCamera();
                Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(photoIntent, 1);
            }

        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //todo check of cancel
            Bitmap srcBmp = (Bitmap) data.getExtras().get("data");
            Bitmap dstBmp;
            if (srcBmp.getWidth() >= srcBmp.getHeight()) {

                dstBmp = Bitmap.createBitmap(
                        srcBmp,
                        srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
                        0,
                        srcBmp.getHeight(),
                        srcBmp.getHeight()
                );

            } else {

                dstBmp = Bitmap.createBitmap(
                        srcBmp,
                        0,
                        srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                        srcBmp.getWidth(),
                        srcBmp.getWidth()
                );
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            dstBmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            final ParseFile photoFile = new ParseFile("user_photo.png", byteArray);
            userPic.setParseFile(photoFile);
            userPic.setVisibility(View.VISIBLE);
            ParseUser.getCurrentUser().put(Application.USER_PHOTO, photoFile);
            photoFile.saveInBackground();
            ParseUser.getCurrentUser().saveInBackground();
            //костыль для обновления юзерпика
            finish();
        }

    }


}
