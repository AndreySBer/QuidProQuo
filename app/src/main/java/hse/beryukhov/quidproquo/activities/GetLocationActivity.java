package hse.beryukhov.quidproquo.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import hse.beryukhov.quidproquo.Application;
import hse.beryukhov.quidproquo.R;

//import org.json.*;

public class GetLocationActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private Location location;
    private GoogleApiClient locationClient;
    private Button findAddress;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Intent intent = getIntent();
        location = intent.getParcelableExtra(Application.INTENT_EXTRA_LOCATION);

        locationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        ImageView backButton = (ImageView) findViewById(R.id.backToMainImageView);
        if (location == null) {
            backButton.setVisibility(View.INVISIBLE);
        }
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setSelection(2);

        final Button findLocation = (Button) findViewById(R.id.findLocationButton);
        findLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(GetLocationActivity.this);

                if (ConnectionResult.SUCCESS != resultCode) {
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, GetLocationActivity.this, 0);
                    dialog.show();
                    findLocation.setClickable(false);
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(GetLocationActivity.this, "Can't get your location due to access settings", Toast.LENGTH_LONG).show();
                        findLocation.setClickable(false);
                        return;
                    }
                }
                location = LocationServices.FusedLocationApi.getLastLocation(locationClient);
                if (location != null) {
                    Log.i("GetLocActivity", "Finish after location!=null check");
                    Toast.makeText(GetLocationActivity.this, String.format("Location setted: %1$f:%2$f", location.getLatitude(), location.getLongitude()), Toast.LENGTH_LONG).show();
                    //finish();

                    /*
                    *To save location changes you should click tick button
                    *
                    Intent intent = new Intent(GetLocationActivity.this, MainActivity.class);
                    intent.putExtra(Application.INTENT_EXTRA_LOCATION, location);
                    startActivity(intent);
                    */
                } else {
                    Toast.makeText(GetLocationActivity.this, "Can't get your location. Please try later or by address", Toast.LENGTH_LONG).show();
                    //findLocation.setClickable(false);
                }
            }
        });
        final TextView fATV = (TextView) findViewById(R.id.addressEditText);

        findAddress = (Button) findViewById(R.id.addressFindButton);
        findAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //deprecated
                //Tambov and hardcode are my enemies
                //location = new Location("");
                //location.setLongitude(41.452238);
                //location.setLatitude(52.721246);
                //Toast.makeText(GetLocationActivity.this, String.format("Location setted: %1$f:%2$f", location.getLatitude(), location.getLongitude()), Toast.LENGTH_LONG).show();
                //JsonObjectRequest
                //Toast.makeText(GetLocationActivity.this,getJSON("https://geocode-maps.yandex.ru/1.x/?geocode=%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0,+%D0%A2%D0%B2%D0%B5%D1%80%D1%81%D0%BA%D0%B0%D1%8F+%D1%83%D0%BB%D0%B8%D1%86%D0%B0,+%D0%B4%D0%BE%D0%BC+7&format=json"),Toast.LENGTH_LONG).show();
                if (fATV.getText().toString() == "" || fATV.getText() == null) {
                    Toast.makeText(GetLocationActivity.this, "Please complete the address field.", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    try {
                        String request = getJSON("https://geocode-maps.yandex.ru/1.x/?geocode="
                                + fATV.getText().toString()
                                + "&format=json&results=1");

                        JSONObject ob = new JSONObject(request);
                        int found = ob.getJSONObject("response")
                                .getJSONObject("GeoObjectCollection")
                                .getJSONObject("metaDataProperty")
                                .getJSONObject("GeocoderResponseMetaData")
                                .getInt("found");

                        int results = ob.getJSONObject("response")
                                .getJSONObject("GeoObjectCollection")
                                .getJSONObject("metaDataProperty")
                                .getJSONObject("GeocoderResponseMetaData")
                                .getInt("results");

                        Log.i("found", String.valueOf(found));
                        Log.i("results", String.valueOf(results));

                        if (found > 0 && results > 0) {
                            String pos = ob.getJSONObject("response")
                                    .getJSONObject("GeoObjectCollection")
                                    .getJSONArray("featureMember")
                                    .getJSONObject(0)
                                    .getJSONObject("GeoObject")
                                    .getJSONObject("Point")
                                    .getString("pos");

                            location = new Location("");
                            location.setLongitude(Double.parseDouble(pos.split(" ")[0]));
                            location.setLatitude(Double.parseDouble(pos.split(" ")[1]));

                            Toast.makeText(GetLocationActivity.this, String.format("Location setted: %1$f:%2$f", location.getLatitude(), location.getLongitude()), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(GetLocationActivity.this, "Can't find this address. Try to refine the query.", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(GetLocationActivity.this, "Something went wrong...", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView saveLocButton = (ImageView) findViewById(R.id.saveLocImageView);
        saveLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GetLocationActivity.this, MainActivity.class);
                intent.putExtra(Application.INTENT_EXTRA_LOCATION, location);
                //Log.i(Application.INTENT_EXTRA_SEARCH_DISTANCE,spinner.getSelectedItem().toString());
                intent.putExtra(Application.INTENT_EXTRA_SEARCH_DISTANCE, spinner.getSelectedItem().toString());
                startActivity(intent);
            }
        });

    }

    public String getJSON(String urle) {

        try {
            URL url = new URL(urle);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("Content-length", "0");
            con.setConnectTimeout(30000);

            con.connect();

            int resp = con.getResponseCode();
            if (resp == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                br.close();
                //findAddress.setBackgroundColor(Color.RED);
                return sb.toString();
            } else {
                Log.e("RESP", "Ответ сервера: " + resp);
            }

        } catch (Exception e) {
            //Log.i("JSON","here");
            e.printStackTrace();
        }

        return null;
    }
@Override
    protected void onStart() {
        locationClient.connect();
        super.onStart();
    }
@Override
    protected void onStop() {
        locationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
