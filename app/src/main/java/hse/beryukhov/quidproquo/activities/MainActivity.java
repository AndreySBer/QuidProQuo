package hse.beryukhov.quidproquo.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.melnykov.fab.FloatingActionButton;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import hse.beryukhov.quidproquo.Application;
import hse.beryukhov.quidproquo.QuidPost;
import hse.beryukhov.quidproquo.QuidPost.PostState;
import hse.beryukhov.quidproquo.R;

import static hse.beryukhov.quidproquo.DataTransform.GetTimePassedTillNow;


public class MainActivity extends FragmentActivity //implements LocationListener,
        //GoogleApiClient.ConnectionCallbacks,
        //GoogleApiClient.OnConnectionFailedListener
{

    private static final int MAX_POST_SEARCH_RESULTS = 50;
    private static final int UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final int FAST_INTERVAL_CEILING_IN_MILLISECONDS = 1000;
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final float METERS_PER_KILOMETER = 1000;
    private ParseQueryAdapter<QuidPost> postsQueryAdapter;
    private String selectedPostObjectId;
    private SwipeRefreshLayout swipeRefresh;

    private LocationRequest locationRequest;
    private GoogleApiClient locationClient;
    private Location currentLocation;
    private Location lastLocation;
    // Fields for the search radius in meters
    private int radius = 1000;

    private ListView postsListView;
    private TextView warningTextView;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        currentLocation = intent.getParcelableExtra(Application.INTENT_EXTRA_LOCATION);
        String searchDistance = intent.getStringExtra(Application.INTENT_EXTRA_SEARCH_DISTANCE);
        if (searchDistance != null)
            try {
                radius = Integer.parseInt(searchDistance.substring(0, searchDistance.length() - 2));
                //Log.i("parseInt search dist", String.format("%1$d",radius));
            } catch (NumberFormatException e) {
                //Log.e("parseInt search dist", searchDistance);
            }
        //else{
            //Log.e("parseInt search dist", "null");
        //}

/*
        if (currentLocation == null) {
            startLocationActivity();
        }
*/
        warningTextView = (TextView) findViewById(R.id.warningTextView);
        warningTextView.setVisibility(View.INVISIBLE);

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Loading posts...");

        //userpic to load new activity click handler
        ImageView userpicImageView = (ImageView) findViewById(R.id.userImageView);
        userpicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UserPageActivity.class));
            }
        });

        //Show UserName in Menu String
        TextView userNameTextView = (TextView) findViewById(R.id.userNameTextView);
        userNameTextView.setText(ParseUser.getCurrentUser().getUsername());
/*
        //New Post button click handler
        ImageButton newPostButton = (ImageButton) findViewById(R.id.post_button);
        newPostButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
                        if (myLoc == null) {
                            Toast.makeText(MainActivity.this,
                                    "We can't find your location. Please try later.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Intent intent = new Intent(MainActivity.this, NewPostActivity.class);
                        intent.putExtra(Application.INTENT_EXTRA_LOCATION, myLoc);
                        startActivity(intent);
                    }
                }
        );
*/
        ImageButton setLocationButton = (ImageButton) findViewById(R.id.set_location_button);
        setLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationActivity();
            }
        });

        {

            postsListView = (ListView) this.findViewById(R.id.posts_listview);
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.attachToListView(postsListView);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
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
                            //Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
                            ParseQuery<QuidPost> query = QuidPost.getQuery();
                            query.include("author");

                            query.orderByDescending("createdAt");
                            //if (myLoc != null) {
                            query.whereWithinKilometers("location", geoPointFromLocation(currentLocation), radius / METERS_PER_KILOMETER);
                            //}
                            //query.setLimit(MAX_POST_SEARCH_RESULTS);
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
                    datePosted.setText(GetTimePassedTillNow(post.getCreatedAt()));
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
        }

        //listener for swipe to refresh

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(true);
                doListQuery();
                swipeRefresh.setRefreshing(false);
            }
        });
    /*
    * There starts first block of code, which is not obvious,
    * but it seems to be need for finding location in this mortal world
    *

        // Create a new global location parameters object
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Create a new location client, using the enclosing class to handle callbacks.
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        /*
    * There ends first block of code, which is not obvious,
    * but it seems to be need for finding location in this mortal world
    */
    }

    private void doListQuery() {
        //Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        // If location info is available, load the data
        if (currentLocation != null) {
            // Refreshes the list view with new data based
            // usually on updated location data.
            //Toast.makeText(MainActivity.this, "do query", Toast.LENGTH_LONG).show();

            dialog.show();
            postsQueryAdapter.loadObjects();
            if (postsListView.getCount() == 0) {
                warningTextView.setText(R.string.warning_no_records);
                warningTextView.setVisibility(View.VISIBLE);
            } else {
                warningTextView.setVisibility(View.INVISIBLE);
            }
        } else {
            //Toast.makeText(MainActivity.this, "Can't find your location. Please try later.", Toast.LENGTH_LONG).show();
            warningTextView.setText(R.string.warning_no_location);
            warningTextView.setVisibility(View.VISIBLE);
            //startLocationActivity();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        } else {
            MainActivity.super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.i("Resume", "1");
        onStart();
        //Log.i("Resume", "2");
        //Log.i("GeoPoint",currentLocation.toString());
        doListQuery();
    }

    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }
    /*
        * There starts second large block of code, which is not obvious,
        * but it seems to be need for finding location in this mortal world
        *
    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            dialog.show();
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("Location changed", "OnLocationChanged()");
        currentLocation = location;
        if (lastLocation != null
                && geoPointFromLocation(location)
                .distanceInKilometersTo(geoPointFromLocation(lastLocation)) < 0.01) {
            return;
        }
        lastLocation = location;
        doListQuery();
    }



    @Override
    public void onConnected(Bundle bundle) {
        Log.i("Location changed", "onConnected()");
        currentLocation = getLocation();
        startPeriodicUpdates();
        doListQuery();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private void showErrorDialog(int errorCode) {
        // Get the error dialog from Google Play services
        Dialog errorDialog =
                GooglePlayServicesUtil.getErrorDialog(errorCode, this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            errorDialog.show();
        }
    }

    private void startPeriodicUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(locationClient, locationRequest, this);
    }

    private void stopPeriodicUpdates() {
        locationClient.disconnect();
    }

    private Location getLocation() {
        if (servicesConnected()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return null;
                }
            }
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        } else {
            return null;
        }
    }

    @Override
    public void onStop() {
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        locationClient.disconnect();

        super.onStop();
    }

    @Override
    public void onStart(){
        Log.i("Start", "Start");
        super.onStart();

        locationClient.connect();
    }
    /*
    * There ENDs large block of code, which is not obvious,
    * but it seems to be need for finding location in this mortal world
    */

}
