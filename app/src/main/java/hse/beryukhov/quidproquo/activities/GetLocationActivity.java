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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class GetLocationActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private Location location;
    private GoogleApiClient locationClient;
    private Button findAddress;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);
        findAddress = (Button) findViewById(R.id.addressFindButton);
        findAddress.setEnabled(false);

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
        /*if (location == null) {
            backButton.setVisibility(View.INVISIBLE);
        }*/
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
                        Toast.makeText(GetLocationActivity.this, R.string.loc_access_settings_error, Toast.LENGTH_LONG).show();
                        findLocation.setClickable(false);
                        return;
                    }
                }
                location = LocationServices.FusedLocationApi.getLastLocation(locationClient);
                if (location != null) {
                    Log.i("GetLocActivity", "Finish after location!=null check");
                    Toast.makeText(GetLocationActivity.this, String.format("Location set: %1$f:%2$f", location.getLatitude(), location.getLongitude()), Toast.LENGTH_LONG).show();
                    //finish();

                    /*
                    *To save location changes you should click tick button
                    *
                    Intent intent = new Intent(GetLocationActivity.this, MainActivity.class);
                    intent.putExtra(Application.INTENT_EXTRA_LOCATION, location);
                    startActivity(intent);
                    */
                } else {
                    Toast.makeText(GetLocationActivity.this, R.string.loc_cant_get, Toast.LENGTH_LONG).show();
                }
            }
        });
        final EditText fATV = (EditText) findViewById(R.id.addressEditText);

        fATV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (fATV.getText().length()>0){
                    findAddress.setEnabled(true);
                }
                else
                {
                    findAddress.setEnabled(false);
                }
            }
        });
        findAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (fATV.getText().toString() == "" || fATV.getText() == null) {
                    Log.e("LocationByAddress","Null value while clicked button");
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

                            Toast.makeText(GetLocationActivity.this, String.format("Location set: %1$f:%2$f", location.getLatitude(), location.getLongitude()), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(GetLocationActivity.this, R.string.loc_geocoder_error, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(GetLocationActivity.this, R.string.loc_json, Toast.LENGTH_LONG).show();
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
                return sb.toString();
            } else {
                Log.e("RESP", "Ответ сервера: " + resp);
            }

        } catch (Exception e) {
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
