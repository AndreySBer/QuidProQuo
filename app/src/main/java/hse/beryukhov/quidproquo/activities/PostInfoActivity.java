package hse.beryukhov.quidproquo.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import hse.beryukhov.quidproquo.Application;
import hse.beryukhov.quidproquo.QuidComment;
import hse.beryukhov.quidproquo.QuidPost;
import hse.beryukhov.quidproquo.R;

import static hse.beryukhov.quidproquo.DataTransform.GetTimePassedTillNow;


public class PostInfoActivity extends Activity {
    TextView postName;
    ImageView stateImage;
    TextView authorText;
    TextView descripText;
    //TextView authorTextL;
    //TextView descripTextL;
    TextView date;
    ImageView authorImage;
    GridLayout menu;
    ImageButton share;
    Button yandexMapButton;
    //ShareActionProvider shareActionProvider;
    Location location;

    View[] laViews;

    String thisPostId;

    private ParseQueryAdapter<QuidComment> commentsQueryAdapter;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_info);

        thisPostId = getIntent().getStringExtra("id");


        postName = (TextView) findViewById(R.id.postNameTextView);
        stateImage = (ImageView) findViewById(R.id.postStateImageView);
        authorText = (TextView) findViewById(R.id.authorNameTextView);
        descripText = (TextView) findViewById(R.id.descriptionText);
        menu = (GridLayout) findViewById(R.id.menuLayout);
        date = (TextView) findViewById(R.id.postedDateTimeTextView);
        //authorTextL = (TextView) findViewById(R.id.authorTextView);
        //descripTextL = (TextView) findViewById(R.id.descriptionTextView);
        authorImage = (ImageView) findViewById(R.id.authorImageView);
        share = (ImageButton) findViewById(R.id.shareButton);
        yandexMapButton = (Button) findViewById(R.id.yandexButton);
        //shareActionProvider= (ShareActionProvider) share.getActionProvider();

        laViews = new View[]{postName, stateImage, authorText, descripText, menu, authorImage, date, share, yandexMapButton};

        //make views invisible untill content will be loaded
        for (View v : laViews) {
            v.setVisibility(View.INVISIBLE);
        }

        //launch content loading
        findPostByID(thisPostId);

        ImageButton backButton = (ImageButton) findViewById(R.id.backToMainButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        Button addCommentButton = (Button) findViewById(R.id.AddCommentButton);
        addCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addcomment();
            }
        });


        ListView commentsListView = (ListView) findViewById(R.id.commentListView);

        yandexMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (false) {
                    Intent intent = new Intent("ru.yandex.yandexmaps.action.SHOW_POINT_ON_MAP");
                    // Проверяем, установлено ли хотя бы одно приложение, способное выполнить это действие.
                    PackageManager packageManager = getPackageManager();
                    List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
                    boolean isIntentSafe = activities.size() > 0;
                    if (isIntentSafe) {
                        intent = new Intent("ru.yandex.yandexmaps.action.SHOW_POINT_ON_MAP");
                        intent.setPackage("ru.yandex.yandexmaps");
                        intent.putExtra("lat", location.getLatitude());
                        intent.putExtra("lon", location.getLongitude());
                        intent.putExtra("desc", postName.getText());
                        intent.putExtra("zoom", 16);
                        intent.putExtra("no‑balloon", true);
                        startActivity(intent);
                    } else {
                        // Открываем страницу приложения Яндекс.Карты в Google Play.
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=ru.yandex.yandexmaps"));
                        startActivity(intent);
                    }
                } else {
                    Uri uri = Uri.parse(String.format("geo:%1$f,%2$f?z=16", location.getLatitude(), location.getLongitude()));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });

        ParseQueryAdapter.QueryFactory<QuidComment> factory = new ParseQueryAdapter.QueryFactory<QuidComment>() {
            @Override
            public ParseQuery<QuidComment> create() {
                ParseQuery<QuidComment> query = QuidComment.getQuery();
                query.include("author");
                query.whereEqualTo("id_post", thisPostId);
                query.orderByAscending("createdAt");

                return query;
            }
        };
        commentsQueryAdapter = new ParseQueryAdapter<QuidComment>(this, factory) {
            @Override
            public View getItemView(QuidComment comment, View view, ViewGroup parent) {
                if (view == null) {
                    view = View.inflate(getContext(), R.layout.quid_comment_item, null);
                }

                TextView text = (TextView) view.findViewById(R.id.commTextView);
                TextView authorName = (TextView) view.findViewById(R.id.usernameTextView);
                TextView dateTime = (TextView) view.findViewById(R.id.dateTimeTextView);
                text.setText(comment.getText());
                try {
                    authorName.setText(comment.getAuthor().getUsername());
                } catch (Exception e) {
                    Log.e("No user in Table", e.getMessage());
                    authorName.setText("NA");
                }
                dateTime.setText(GetTimePassedTillNow(comment.getCreatedAt(), PostInfoActivity.this));
                final ImageView userPicIV = (ImageView) view.findViewById(R.id.userPicImageView);
                try{
                ParseFile photoFile = (ParseFile) comment.getAuthor().get(Application.USER_PHOTO);
                if (photoFile != null) {
                    photoFile.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            Bitmap img = BitmapFactory.decodeByteArray(data, 0, data.length);
                            Bitmap roundImg = MainActivity.ImageHelper.getRoundedCornerBitmap(img, img.getWidth() / 2);
                            userPicIV.setImageBitmap(roundImg);
                            userPicIV.setPadding(6, 6, 6, 6);
                        }
                    });
                }}
                catch (Exception e) {
                    Log.e("No user in Table", e.getMessage());
                }
                return view;
            }
        };

        commentsQueryAdapter.setAutoload(false);
        commentsQueryAdapter.setPaginationEnabled(false);

        commentsListView.setAdapter(commentsQueryAdapter);

        //listener for swipe to refresh

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeCommentRefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(true);
                doCommentsQuery();
                swipeRefresh.setRefreshing(false);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                //share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                //share.putExtra(Intent.EXTRA_SUBJECT, postName.getText());
                share.putExtra(Intent.EXTRA_TEXT, authorText.getText()
                        + " posted via QuidProQuo: "
                        + postName.getText() + "\n\r"
                        + descripText.getText()
                        + "\n\rLocation: " + getLocString()
                        + "\n\r"
                        + getLocHref());

                //share.putExtra(Intent.EXTRA_TITLE, "Qoo");
                //share.putExtra(Intent.CATEGORY_APP_MAPS, location.toString());

                startActivity(Intent.createChooser(share, "Share post"));
            }
        });

    }

    private String getLocHref() {
        return "https://maps.yandex.ru/?ll=" + location.getLongitude() + "%20" + location.getLatitude() + "&z=14";
    }

    private String getLocString() {
        return location.getLongitude() + " " + location.getLatitude();
    }

    private void addcomment() {
        try {
            if (!isOnline()) {
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
                return;
            }
            final EditText commentText = (EditText) findViewById(R.id.commentEditText);
            String text = commentText.getText().toString().trim();

            final ProgressDialog dialog = new ProgressDialog(PostInfoActivity.this);
            dialog.setMessage("Publishing your comment...");
            dialog.show();

            QuidComment comment = new QuidComment();

            comment.setAuthor(ParseUser.getCurrentUser());
            comment.setText(text);
            comment.setPostID(thisPostId);

            //acl
            ParseACL acl = new ParseACL();
            // Give public read access
            acl.setPublicReadAccess(true);
            comment.setACL(acl);

            comment.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    dialog.dismiss();
                    commentText.setText("");
                    doCommentsQuery();
                }
            });
        } catch (Exception e) {
            Toast.makeText(PostInfoActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void doCommentsQuery() {
        commentsQueryAdapter.loadObjects();
    }


    private void findPostByID(String thisPostId) {
        //Progress dialog setting
        final ProgressDialog dialog = new ProgressDialog(PostInfoActivity.this);
        dialog.setMessage("Loading...");
        dialog.show();
        ParseQuery<QuidPost> query = ParseQuery.getQuery(QuidPost.class);
        query.whereEqualTo("objectId", thisPostId);
        query.include("author");
        query.findInBackground(new FindCallback<QuidPost>() {
            @Override
            public void done(List<QuidPost> objects, ParseException e) {
                if (e == null) {
                    Log.i("objects", Integer.toString(objects.size()));
                    QuidPost thisPost = objects.get(0);
                    doCommentsQuery();

                    QuidPost.PostState state = thisPost.getState();
                    switch (state) {
                        case ASK: {
                            stateImage.setImageResource(R.drawable.ic_help_black_48dp);
                            break;
                        }
                        case SUGGEST: {
                            stateImage.setImageResource(R.drawable.ic_announcement_black_48dp);
                            break;
                        }
                        case ADVERT: {
                            stateImage.setImageResource(R.drawable.ic_grade_black_48dp);

                            menu.setBackgroundColor(0xFF3377FF);
                            break;
                        }
                    }

                    authorText.setText(thisPost.getAuthor().getUsername());
                    ParseFile photoFile = (ParseFile) thisPost.getAuthor().get(Application.USER_PHOTO);
                    if (photoFile != null) {
                        photoFile.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                Bitmap img = BitmapFactory.decodeByteArray(data, 0, data.length);
                                Bitmap roundImg = MainActivity.ImageHelper.getRoundedCornerBitmap(img, img.getWidth() / 2);
                                authorImage.setImageBitmap(roundImg);
                                authorImage.setPadding(6, 6, 6, 6);
                            }
                        });
                    }
                    postName.setText(thisPost.getName());
                    date.setText(GetTimePassedTillNow(thisPost.getCreatedAt(), PostInfoActivity.this));
                    descripText.setText(thisPost.getText());
                    location = new Location("");
                    location.setLatitude(thisPost.getLocation().getLatitude());
                    location.setLongitude(thisPost.getLocation().getLongitude());

                    //return visibility to the whole layout
                    for (View v : laViews) {
                        v.setVisibility(View.VISIBLE);
                    }
                    dialog.dismiss();

                } else {
                    Toast.makeText(PostInfoActivity.this, e.getMessage() + e.getCause().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
