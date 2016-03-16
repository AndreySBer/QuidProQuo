package hse.beryukhov.quidproquo.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import hse.beryukhov.quidproquo.Application;
import hse.beryukhov.quidproquo.R;

public class SignUpActivity extends Activity {
    private EditText nameET;
    private EditText passwET;
    private EditText emailET;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        //Set up the signup form
        nameET = (EditText) findViewById(R.id.editName);
        passwET = (EditText) findViewById(R.id.editPassword);
        emailET = (EditText) findViewById(R.id.editEmail);
        // Sign up button click handler
        Button signupButton = (Button) findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                signup();
                                            }
                                        }

        );
        //Makes signup after pressing Enter in the last (password) field
        passwET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.edit_action_signup || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    signup();
                    return true;
                }
                return false;
            }
        });
    }

    private void signup() {
        String name = nameET.getText().toString();
        String passw = passwET.getText().toString();
        String email = emailET.getText().toString();
        String checkRes = check(name, passw, email);
        if (checkRes != "OK") {
            Toast.makeText(this, checkRes, Toast.LENGTH_LONG).show();
            return;
        }
        //else
        //Progress Dialog
        final ProgressDialog dialog = new ProgressDialog(SignUpActivity.this);
        dialog.setMessage("Signing you up...");
        dialog.show();
        ParseUser user = new ParseUser();
        user.setUsername(name);
        user.setPassword(passw);
        user.setEmail(email);
        user.put(Application.ISPREMIUM,false);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                if (e != null) {
                    Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    //to remove
                    Toast.makeText(SignUpActivity.this, "All good", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SignUpActivity.this, DispatchActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    private String check(String name, String passw, String email) {
        return "OK";
    }

}
