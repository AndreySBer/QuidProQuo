package hse.beryukhov.quidproquo.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.parse.ParseException;
import com.parse.ParseUser;

import hse.beryukhov.quidproquo.Application;
import hse.beryukhov.quidproquo.R;

public class UserPageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        EditText userName = (EditText) findViewById(R.id.userNametextView);
        userName.setText(ParseUser.getCurrentUser().getUsername());

        EditText userEmail = (EditText) findViewById(R.id.userEmailtextView);
        userEmail.setText(ParseUser.getCurrentUser().getEmail());

        ImageButton backButton = (ImageButton) findViewById(R.id.backToMainButtonU);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        // Log out button click handler

        ImageView logoutButton = (ImageView) findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(UserPageActivity.this);
                                                builder.setMessage(R.string.dialog_logout_message)
                                                        .setTitle(R.string.dialog_logout_title)
                                                        .setPositiveButton(R.string.logOutButton, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                ParseUser.logOut();
                                                                dialog.dismiss();
                                                                Intent intent = new Intent(UserPageActivity.this, DispatchActivity.class);
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
        final EditText telegram=(EditText) findViewById(R.id.telegramEditText);


        ImageView tick=(ImageView) findViewById(R.id.tickImageView);
        tick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo
                if (telegram.getText().toString()!=""){
                    ParseUser.getCurrentUser().put(Application.TELEGRAM_ACCOUNT,telegram.getText().toString());
                    try {
                        ParseUser.getCurrentUser().fetchFromLocalDatastore();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
