package hse.beryukhov.quidproquo.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import hse.beryukhov.quidproquo.Application;
import hse.beryukhov.quidproquo.QuidPost;
import hse.beryukhov.quidproquo.R;

public class NewPostActivity extends Activity {
    private EditText postNameEditText;
    private EditText postTextEditText;

    private static final int NAME_MAX_LENGTH = 50;

    private ParseGeoPoint geoPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        Intent intent = getIntent();
        Location location = intent.getParcelableExtra(Application.INTENT_EXTRA_LOCATION);
        geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

        postNameEditText = (EditText) findViewById(R.id.newPostNameEditText);
        postTextEditText = (EditText) findViewById(R.id.newPostTextEditText);
        Button postButton = (Button) findViewById(R.id.new_post_button);
        postButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                post();
            }
        });

        ImageView tick = (ImageView) findViewById(R.id.imageView3);
        tick.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                post();
            }
        });

        ImageView backToMenu = (ImageView) findViewById(R.id.imageView2);
        backToMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void post() {
        //try {
        String name = postNameEditText.getText().toString().trim();
        String text = postTextEditText.getText().toString().trim();
        //filter on name length
        if (name.length() > NAME_MAX_LENGTH) {
            Toast.makeText(NewPostActivity.this, R.string.postNameLengthTooLong, Toast.LENGTH_LONG).show();
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(NewPostActivity.this);
        dialog.setMessage("Publishing your post...");
        dialog.show();

        QuidPost post = new QuidPost();

        post.setName(name);
        post.setText(text);
        post.setAuthor(ParseUser.getCurrentUser());

        //acl
        ParseACL acl = new ParseACL();
        // Give public read access
        acl.setPublicReadAccess(true);
        post.setACL(acl);

        Location location = new Location("");
            /*
            //it is hardcoded for future realisation
            *location.setLatitude(52.721246);
            *location.setLongitude(41.452238);
            *post.setLocation(new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
            //upd: future is now
            */
        post.setLocation(geoPoint);
        Spinner spinner = (Spinner) findViewById(R.id.stateSpinner);
        String selected = spinner.getSelectedItem().toString().toUpperCase();
        post.setState(QuidPost.PostState.valueOf(QuidPost.PostState.class, selected));
        // Save the post
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                finish();
            }
        });
        //} catch (Exception e) {
        //    Toast.makeText(NewPostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        //}
    }
}
