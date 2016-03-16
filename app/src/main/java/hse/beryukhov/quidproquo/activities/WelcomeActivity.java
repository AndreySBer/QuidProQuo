package hse.beryukhov.quidproquo.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
/*
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
*/
import hse.beryukhov.quidproquo.R;

/**
 * Created by Andrey on 02.01.2016.
 */
public class WelcomeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
// Sign up button click handler
        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new OnClickListener() {
                                           public void onClick(View v) {
                                               // Starts an intent for the sign up activity
                                               startActivity(new Intent(WelcomeActivity.this, LogInActivity.class));
                                           }
                                       }

        );
        // Sign up button click handler
        Button signupButton = (Button) findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new OnClickListener() {
                                            public void onClick(View v) {
                                                // Starts an intent for the sign up activity
                                                startActivity(new Intent(WelcomeActivity.this, SignUpActivity.class));
                                            }
                                        }

        );
/*
        Button vkButton = (Button) findViewById(R.id.vkButton);
        vkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VKSdk.login(WelcomeActivity.this, VKScope.EMAIL);

            }


        });*/
    }
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                VKRequest request = VKApi.users().get();
                request.start();

                Toast.makeText(WelcomeActivity.this, request.response.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }*/

}
