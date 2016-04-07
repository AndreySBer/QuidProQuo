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
import android.os.Build;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import hse.beryukhov.quidproquo.Application;
import hse.beryukhov.quidproquo.QuidPost;
import hse.beryukhov.quidproquo.QuidPost.PostState;
import hse.beryukhov.quidproquo.R;

import static hse.beryukhov.quidproquo.DataTransform.GetTimePassedTillNow;


public class MainActivity extends Activity {

    private static final int MAX_POST_SEARCH_RESULTS = 50;
    private static final float METERS_PER_KILOMETER = 1000;
    private ParseQueryAdapter<QuidPost> postsQueryAdapter;
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
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_blind);


        Intent intent = getIntent();
        currentLocation = intent.getParcelableExtra(Application.INTENT_EXTRA_LOCATION);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (currentLocation == null) {
            fab.setVisibility(View.INVISIBLE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
        String searchDistance = intent.getStringExtra(Application.INTENT_EXTRA_SEARCH_DISTANCE);
        if (searchDistance != null)
            try {
                radius = Integer.parseInt(searchDistance.substring(0, searchDistance.length() - 2));
            } catch (NumberFormatException e) {
            }
        warningTextView = (TextView) findViewById(R.id.warningTextView);
        warningTextView.setVisibility(View.INVISIBLE);

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Loading posts...");

        //userpic to load new activity click handler
        ImageView userpicImageView = (ImageView) findViewById(R.id.userImageView);


        //Show UserName in Menu String
        //TextView userNameTextView = (TextView) findViewById(R.id.userNameTextView);
        //userNameTextView.setText(ParseUser.getCurrentUser().getUsername());
        ImageButton setLocationButton = (ImageButton) findViewById(R.id.set_location_button);
        setLocationButton.setOnClickListener(new View.OnClickListener()

                                             {
                                                 @Override
                                                 public void onClick(View v) {
                                                     startLocationActivity();
                                                 }
                                             }

        );


        postsListView = (ListView) this.findViewById(R.id.posts_listview);

        fab.attachToListView(postsListView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location myLoc = currentLocation;
                if (myLoc == null) {
                    Toast.makeText(MainActivity.this,
                            "We can't find your location. Please try later.", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, NewPostActivity.class);
                intent.putExtra(Application.INTENT_EXTRA_LOCATION, myLoc);
                startActivity(intent);
            }
        });
        ParseQueryAdapter.QueryFactory<QuidPost> factory =
                new ParseQueryAdapter.QueryFactory<QuidPost>() {
                    public ParseQuery<QuidPost> create() {
                        ParseQuery<QuidPost> query = QuidPost.getQuery();
                        query.include("author");
                        query.orderByDescending("createdAt");
                        query.whereWithinKilometers("location", geoPointFromLocation(currentLocation), radius / METERS_PER_KILOMETER);

                        query.setLimit(MAX_POST_SEARCH_RESULTS);
                        return query;
                    }
                };
        postsQueryAdapter = new ParseQueryAdapter<QuidPost>(this, factory) {
            @Override
            public View getItemView(QuidPost post, View view, ViewGroup parent) {
                //Toast.makeText(MainActivity.this, "new Adapter", Toast.LENGTH_LONG).show();
                if (view == null) {
                    view = View.inflate(getContext(), R.layout.quid_post_item, null);
                }
                TextView contentView = (TextView) view.findViewById(R.id.content_view);
                TextView usernameView = (TextView) view.findViewById(R.id.username_view);

                contentView.setText(post.getName());
                usernameView.setText(post.getAuthor().getUsername());

                TextView datePosted = (TextView) view.findViewById(R.id.dateposted_view);
                datePosted.setText(GetTimePassedTillNow(post.getCreatedAt(), MainActivity.this));
                    /*if (post.getAuthor() == ParseUser.getCurrentUser()) {
                        ImageButton settings = (ImageButton) view.findViewById(R.id.myPostSettingsImageButton);
                        settings.setVisibility(View.VISIBLE);
                    }*/
                ImageView stateImage = (ImageView) view.findViewById(R.id.postStateImageView);
                PostState state = post.getState();
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
                        break;
                    }
                }
                //Toast.makeText(MainActivity.this, "return Adapter", Toast.LENGTH_LONG).show();
                warningTextView.setVisibility(View.INVISIBLE);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                return view;
            }
        };
        postsQueryAdapter.setAutoload(false);
        postsQueryAdapter.setPaginationEnabled(false);

        postsListView.setAdapter(postsQueryAdapter);

        postsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final QuidPost item = postsQueryAdapter.getItem(position);
                selectedPostObjectId = item.getObjectId();
                //Toast.makeText(MainActivity.this, selectedPostObjectId, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, PostInfoActivity.class);
                Log.i("item", item.getObjectId());
                intent.putExtra("id", item.getObjectId());
                startActivity(intent);
            }
        });

        //listener for swipe to refresh
        swipeRefresh = (SwipeRefreshLayout)
                findViewById(R.id.swipeRefresh);
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
                    Bitmap roundImg = ImageHelper.getRoundedCornerBitmap(img, img.getWidth() / 2);
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
        toggle.syncState();

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

                if (id == R.id.nav_my_posts) {

                } else if (id == R.id.nav_about) {
                    about();
                } else if (id == R.id.nav_comments) {

                } else if (id == R.id.nav_manage) {
                    startActivity(new Intent(MainActivity.this, UserPageActivityNew.class));
                } else if (id == R.id.nav_share) {
                    Intent share = new Intent(android.content.Intent.ACTION_SEND);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.dialog_message)
                .setTitle(R.string.nav_about)
                .setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void doListQuery() {
        if (currentLocation != null) {
            // Refreshes the list view with new data based
            // usually on updated location data.

            dialog.show();
            postsQueryAdapter.loadObjects();
            if (postsListView.getCount() == 0) {
                warningTextView.setText(R.string.warning_no_records);
                warningTextView.setVisibility(View.VISIBLE);
                dialog.dismiss();
            } else {
                warningTextView.setVisibility(View.INVISIBLE);
            }
        } else {
            warningTextView.setText(R.string.warning_no_location);
            warningTextView.setVisibility(View.VISIBLE);
        }
    }

    //decomposition one love
    private void startLocationActivity() {
        Intent intent = new Intent(MainActivity.this, GetLocationActivity.class);
        intent.putExtra(Application.INTENT_EXTRA_LOCATION, currentLocation);
        startActivity(intent);
    }

    //there is  a code for minimizing the app on back pressing
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        } else {
            MainActivity.super.onBackPressed();
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

    public static class ImageHelper {
        public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                    .getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, pixels, pixels, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return output;
        }
    }

}
