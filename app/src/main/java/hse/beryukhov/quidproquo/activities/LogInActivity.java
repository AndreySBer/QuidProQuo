package hse.beryukhov.quidproquo.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import hse.beryukhov.quidproquo.R;

/**
 * Created by Andrey on 03.01.2016.
 */
public class LogInActivity extends Activity {
    private EditText nameET;
    private EditText passwET;
    private TextView reqPW;

    private EditText emailET;
    private Button changePW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Set up the login form
        nameET = (EditText) findViewById(R.id.editName);
        passwET = (EditText) findViewById(R.id.editPassword);

        emailET = (EditText) findViewById(R.id.editEmailForPW);
        changePW = (Button) findViewById(R.id.changePWButton);
        emailET.setVisibility(View.INVISIBLE);
        changePW.setVisibility(View.INVISIBLE);

        // Log in button click handler
        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
                                           public void onClick(View v) {
                                               login();
                                           }
                                       }

        );
        //Makes login after pressing Enter in the last (password) field
        passwET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.edit_action_login || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    login();
                    return true;
                }
                return false;
            }
        });

        reqPW = (TextView) findViewById(R.id.reqPWtextView);
        reqPW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                emailET.setVisibility(View.VISIBLE);
                changePW.setVisibility(View.VISIBLE);
            }
        });

        changePW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ParseUser.requestPasswordResetInBackground(emailET.getText().toString(), new RequestPasswordResetCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null)
                            Toast.makeText(LogInActivity.this, R.string.email_sent, Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(LogInActivity.this, R.string.email_not_found, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void login() {
        String name = nameET.getText().toString();
        String passw = passwET.getText().toString();
        String checkRes = check(name, passw);
        if (checkRes != "OK") {
            Toast.makeText(this, checkRes, Toast.LENGTH_LONG).show();
            return;
        }
        if (!isOnline()){
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
            return;
        }
            //else
        //Progress dialog setting
        final ProgressDialog dialog = new ProgressDialog(LogInActivity.this);
        dialog.setMessage("Logging you in...");
        dialog.show();
        //Parse login
        ParseUser.logInInBackground(name, passw, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                dialog.dismiss();
                if (e != null) {
                    if (isOnline()) {
                        Toast.makeText(LogInActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LogInActivity.this, R.string.no_internet, Toast.LENGTH_LONG).show();
                    }
                } else

                {
                    Intent intent = new Intent(LogInActivity.this, DispatchActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            }


        });
    }

    private String check(String name, String passw) {
        return "OK";
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
