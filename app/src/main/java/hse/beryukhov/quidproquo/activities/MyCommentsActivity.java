package hse.beryukhov.quidproquo.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import hse.beryukhov.quidproquo.Application;
import hse.beryukhov.quidproquo.QuidComment;
import hse.beryukhov.quidproquo.QuidPost;
import hse.beryukhov.quidproquo.QuidPost.PostState;
import hse.beryukhov.quidproquo.R;

import static hse.beryukhov.quidproquo.DataTransform.GetTimePassedTillNow;


public class MyCommentsActivity extends Activity {

    private static final int MAX_POST_SEARCH_RESULTS = 50;
    private static final float METERS_PER_KILOMETER = 1000;
    private ParseQueryAdapter<QuidComment> commentsQueryAdapter;
    private String selectedPostObjectId;
    private SwipeRefreshLayout swipeRefresh;
    private Location currentLocation;

    // Fields for the search radius in meters
    private int radius = 1000;
    private ListView postsListView;
    private TextView warningTextView;
    private ProgressDialog dialog;
    private ImageView userpic;
    private DrawerLayout drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_comments_blind);


        warningTextView = (TextView) findViewById(R.id.warningTextView3);
        warningTextView.setVisibility(View.INVISIBLE);

        dialog = new ProgressDialog(MyCommentsActivity.this);
        dialog.setMessage("Loading posts...");

        //userpic to load new activity click handler
        ImageView userpicImageView = (ImageView) findViewById(R.id.settingsButton3);

        postsListView = (ListView) this.findViewById(R.id.posts_listview3);


        ParseQueryAdapter.QueryFactory<QuidComment> factory =
                new ParseQueryAdapter.QueryFactory<QuidComment>() {
                    public ParseQuery<QuidComment> create() {
                        ParseQuery<QuidComment> query = QuidComment.getQuery();
                        query.include("author");
                        query.orderByDescending("createdAt");
                        query.whereEqualTo("author", ParseUser.getCurrentUser());

                        query.setLimit(MAX_POST_SEARCH_RESULTS);
                        return query;
                    }
                };
        commentsQueryAdapter = new ParseQueryAdapter<QuidComment>(this, factory) {
            @Override
            public View getItemView(QuidComment comment, View view, ViewGroup parent) {
                //Toast.makeText(MainActivity.this, "new Adapter", Toast.LENGTH_LONG).show();
                if (view == null) {
                    view = View.inflate(getContext(), R.layout.quid_comment_item, null);
                }
                TextView text = (TextView) view.findViewById(R.id.commTextView);
                TextView authorName = (TextView) view.findViewById(R.id.usernameTextView);
                TextView dateTime = (TextView) view.findViewById(R.id.dateTimeTextView);
                text.setText(comment.getText());
                authorName.setText(comment.getAuthor().getUsername());
                dateTime.setText(GetTimePassedTillNow(comment.getCreatedAt(), MyCommentsActivity.this));
                final ImageView userPicIV = (ImageView) view.findViewById(R.id.userPicImageView);
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
                }
                //Toast.makeText(MainActivity.this, "return Adapter", Toast.LENGTH_LONG).show();
                warningTextView.setVisibility(View.INVISIBLE);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                return view;
            }
        };
        commentsQueryAdapter.setAutoload(false);
        commentsQueryAdapter.setPaginationEnabled(false);

        postsListView.setAdapter(commentsQueryAdapter);

        postsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final QuidComment item = commentsQueryAdapter.getItem(position);
                selectedPostObjectId = item.getPostID();
                //Toast.makeText(MainActivity.this, selectedPostObjectId, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MyCommentsActivity.this, PostInfoActivity.class);
                Log.i("item", item.getPostID());
                intent.putExtra("id", item.getPostID());
                startActivity(intent);
            }
        });

        //listener for swipe to refresh
        swipeRefresh = (SwipeRefreshLayout)
                findViewById(R.id.swipeRefresh3);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                              @Override
                                              public void onRefresh() {
                                                  swipeRefresh.setRefreshing(true);
                                                  doListQuery();
                                                  swipeRefresh.setRefreshing(false);
                                              }
                                          }
        );
        // Lookup navigation view
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        // Inflate the header view at runtime
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        // We can now look up items within the header if needed
        userpic = (ImageView) headerLayout.findViewById(R.id.userPic);
        ((TextView) headerLayout.findViewById(R.id.userName)).setText(ParseUser.getCurrentUser().getUsername());

        userpic.setVisibility(View.INVISIBLE);
        ParseFile photoFile = (ParseFile) ParseUser.getCurrentUser().get(Application.USER_PHOTO);
        if (photoFile != null) {
            photoFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Bitmap img = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Bitmap roundImg = MainActivity.ImageHelper.getRoundedCornerBitmap(img, img.getWidth() / 2);
                    userpic.setImageBitmap(roundImg);

                    userpic.setVisibility(View.VISIBLE);
                    Log.i("done", "done");
                }
            });
        } else {
            userpic.setVisibility(View.VISIBLE);
        }
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        //toggle.syncState();

        userpicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this, UserPageActivityNew.class));
                drawer.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                // Handle navigation view item clicks here.
                int id = item.getItemId();
                if (id == R.id.nav_posts) {
                    startActivity(new Intent(MyCommentsActivity.this, MainActivity.class));
                } else if (id == R.id.nav_my_posts) {
                    startActivity(new Intent(MyCommentsActivity.this, MyPostsActivity.class));
                } else if (id == R.id.nav_about) {
                    about();
                } else if (id == R.id.nav_comments) {
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_manage) {
                    startActivity(new Intent(MyCommentsActivity.this, UserPageActivityNew.class));
                } else if (id == R.id.nav_share) {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    //share.putExtra(Intent.EXTRA_TEXT, Html.fromHtml("I'm already using QuidProQuo. Try yourself:" + " <a href = http://andreyber.pythonanywhere.com/qpq/>".replace("/", "\\") + " http://andreyber.pythonanywhere.com/qpq/</a>"));
                    share.putExtra(Intent.EXTRA_TEXT, "I'm already using QuidProQuo. Try yourself: http://andreyber.pythonanywhere.com/qpq/");
                    startActivity(Intent.createChooser(share, "Share post"));
                }

                drawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });

    }

    private void about() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyCommentsActivity.this);
        builder.setMessage(R.string.dialog_message)
                .setTitle(R.string.nav_about)
                .setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void doListQuery() {

        // Refreshes the list view with new data based
        // usually on updated location data.

        dialog.show();
        commentsQueryAdapter.loadObjects();
        if (postsListView.getCount() == 0) {
            warningTextView.setText(R.string.warning_no_user_comments);
            warningTextView.setVisibility(View.VISIBLE);
            dialog.dismiss();
        } else {
            warningTextView.setVisibility(View.INVISIBLE);
        }

    }


    //there is  a code for minimizing the app on back pressing
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            startActivity(new Intent(MyCommentsActivity.this, MainActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onStart();
        doListQuery();
    }

    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

}
