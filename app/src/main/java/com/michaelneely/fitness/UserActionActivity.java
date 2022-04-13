package com.michaelneely.fitness;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER;
import static com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT;

public class UserActionActivity extends AppCompatActivity {
    String authenticatedName = null;
    TextView textViewMainUserAction;
    TextView textViewDistanceDisplay;
    TextView textViewCurrentLocationDisplay;
    //to allow today summary to make a static reference
    static UserActionActivity instance;
    Context userActionActivityContext;
    private View linearLayoutUserStatistics;
    private View linearLayoutTodaySummary;
    private final String AUTHENTICATED_USER = "AUTH_USER_NAME";
    private final String AUTHENTICATED_USER_BACK = "AUTH_USER_NAME_BACK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_action);
        initUserActionPage();
        setupUserActions();


    }

    private void setupUserActions() {
        initializeDatabaseForHandlingLocationUpdates();
        setupHandlerForUserStatistics();
        setupHandlerForTodaySummary();
        LocationConfig.currentTime = new Timer();
        initializeTimerTask();
        beginLocationUpdates();
    }

    private void initUserActionPage() {
        instance = this;
        textViewMainUserAction = findViewById(R.id.text_view_main_user_action);
        textViewMainUserAction.setMovementMethod(new ScrollingMovementMethod());
        textViewDistanceDisplay = findViewById(R.id.textViewDis);
        textViewCurrentLocationDisplay = findViewById(R.id.text_view_current_distance);
        linearLayoutUserStatistics = findViewById(R.id.linear_layout_view_statistics);
        linearLayoutTodaySummary = findViewById(R.id.lineearlayout_summary);
        userActionActivityContext = this;
    }

    public void initializeDatabaseForHandlingLocationUpdates() {
        obtainAuthenticatedNamePassedAndSetupDate();
        obtainTheLocationIfAlreadyFoundInTheDatabase();
        handleFirstTimeUser();


    }

    private void handleFirstTimeUser() {
        if (LocationConfig.distanceDatabaseHelper.checkDateExist(LocationConfig.fitnessAppDateFormatted)) {
            LocationConfig.fitnessAppDistance = LocationConfig.distanceDatabaseHelper.getDistance(LocationConfig.fitnessAppDateFormatted);
            textViewDistanceDisplay.setText("DISTANCE COVERED: " + LocationConfig.fitnessAppDistance + " feet(s)");
            LocationConfig.fitnessAppDistanceStored = LocationConfig.fitnessAppDistance;
            LocationConfig.fitnessAppMilesConverter = (int) LocationConfig.fitnessAppDistance / 1000;
            Toast.makeText(UserActionActivity.this, "Today Data has been retrieved", Toast.LENGTH_LONG).show();
        } else {
            boolean isDistanceInsertedIntoDatabase = LocationConfig.distanceDatabaseHelper.insertDistance(LocationConfig.fitnessAppDateFormatted, 0.0f);
            if (!isDistanceInsertedIntoDatabase) {
                Toast.makeText(UserActionActivity.this, "Could not insert Data", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(UserActionActivity.this, "Recording started", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void obtainTheLocationIfAlreadyFoundInTheDatabase() {

        if (LocationConfig.distanceDatabaseHelper.checkDateExist(LocationConfig.latitudeSearchStringFromDatabase)) {
            LocationConfig.currentLocationLatitude = (double) LocationConfig.distanceDatabaseHelper.getDistance(LocationConfig.latitudeSearchStringFromDatabase);
            LocationConfig.currentLocationLongitude = (double) LocationConfig.distanceDatabaseHelper.getDistance(LocationConfig.longitudeSearchStringFromDatabase);
            LocationConfig.currentLocation = new Location("");
            LocationConfig.currentLocation.setLatitude(LocationConfig.currentLocationLatitude);
            LocationConfig.currentLocation.setLongitude(LocationConfig.currentLocationLongitude);
        }
    }

    private void obtainAuthenticatedNamePassedAndSetupDate() {
        Intent userActionPageIntent = getIntent();
        authenticatedName = userActionPageIntent.getStringExtra(AUTHENTICATED_USER);
        LocationConfig.distanceDatabaseHelper = new DistanceDatabaseHelper(UserActionActivity.this, authenticatedName);
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        LocationConfig.fitnessAppDateFormatted = dateFormat.format(currentTime);
    }

    public void setupHandlerForUserStatistics() {
        linearLayoutUserStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = LocationConfig.distanceDatabaseHelper.getAllData();
                if (res.getCount() == 0) {
                    Toast.makeText(userActionActivityContext, "No data recorded for this user", Toast.LENGTH_LONG).show();
                }
                while (res.moveToNext()) {
                    String date = res.getString(0);
                    if (date.equals(LocationConfig.latitudeSearchStringFromDatabase) || date.equals(LocationConfig.longitudeSearchStringFromDatabase)) {

                    } else {
                        textViewMainUserAction.append("Date: " + res.getString(0) + "      Distance(feet): " + res.getFloat(1) + "\n");
                        Toast.makeText(UserActionActivity.this, "Current statistics is up to date", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    public void setupHandlerForTodaySummary() {
        linearLayoutTodaySummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToTodaySummary = new Intent(UserActionActivity.this, TodaySummaryActivity.class);
                intentToTodaySummary.putExtra(AUTHENTICATED_USER, authenticatedName);
                startActivityForResult(intentToTodaySummary, 1);
            }
        });
    }

    public void beginLocationUpdates() {
        LocationConfig.fitnessApLocationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        checkPermission();
        LocationConfig.fitnessAppLastLocationRecorded = LocationConfig.fitnessApLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //determine if the start location is the current location
        if (LocationConfig.currentLocation != null && LocationConfig.fitnessAppLastLocationRecorded != null) {
            float distanceFromOffice = LocationConfig.fitnessAppLastLocationRecorded.distanceTo(LocationConfig.currentLocation);
            if (distanceFromOffice < 100) {
                LocationConfig.isUserInSameLocation = true;
                currentPositionAlert(GEOFENCE_TRANSITION_ENTER);
            }
        }
        //update location when user moves
        LocationConfig.fitnessAppLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                float meter = location.distanceTo(LocationConfig.fitnessAppLastLocationRecorded);
                LocationConfig.fitnessAppDistance += meter * 3.28084;
                textViewDistanceDisplay.setText("DISTANCE COVERED: " + LocationConfig.fitnessAppDistance + " feet(s)");
                LocationConfig.fitnessAppLastLocationRecorded = location;
                if (LocationConfig.currentLocation != null) {
                    float distanceFromOffice = LocationConfig.fitnessAppLastLocationRecorded.distanceTo(LocationConfig.currentLocation);
                    if (distanceFromOffice <= 100 && (!LocationConfig.isUserInSameLocation)) {
                        currentPositionAlert(GEOFENCE_TRANSITION_ENTER);
                        LocationConfig.isUserInSameLocation = true;
                    } else if (distanceFromOffice > 100 && LocationConfig.isUserInSameLocation) {
                        currentPositionAlert(GEOFENCE_TRANSITION_EXIT);
                        LocationConfig.isUserInSameLocation = false;
                    }
                }
                checkMilestones();
                updateDistanceInDB();

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        LocationConfig.fitnessApLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, LocationConfig.fitnessAppLocationListener);
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                //get permission from the user
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
        } else {
            //ignore because this means permission is granted
        }
    }

    public void checkMilestones() {
        if ((LocationConfig.fitnessAppDistance / 1000 - LocationConfig.fitnessAppMilesConverter) > 1) {
            LocationConfig.fitnessAppMilesConverter += 1;
            Toast.makeText(userActionActivityContext, "You have managed" + LocationConfig.fitnessAppMilesConverter * 1000 + " feet", Toast.LENGTH_LONG).show();
        }
    }

    public void updateDistanceInDB() {
        //reduce frequency of updating database
        if ((LocationConfig.fitnessAppDistance - LocationConfig.fitnessAppDistanceStored) > 150) {
            boolean isUpdated = LocationConfig.distanceDatabaseHelper.updateDistance(LocationConfig.fitnessAppDateFormatted, LocationConfig.fitnessAppDistance);
            if (isUpdated) {
                LocationConfig.fitnessAppDistanceStored = LocationConfig.fitnessAppDistance;
            } else {
                Toast.makeText(userActionActivityContext, "Distance updated failed", Toast.LENGTH_LONG).show();
            }

        }
    }

    public void initializeTimerTask() {
        LocationConfig.timeTask = new TimerTask() {
            @Override
            public void run() {
                LocationConfig.fitnessAppHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        textViewCurrentLocationDisplay.setText("You've stayed in the current position for: " + LocationConfig.counter + " seconds");
                        LocationConfig.counter++;
                        if (LocationConfig.counter / 3600 > LocationConfig.timeConverter) {
                            int hour = LocationConfig.counter / 3600;
                            LocationConfig.timeConverter = hour;
                            Toast.makeText(userActionActivityContext, "You've stayed in the current position for: " + hour + " hours.Stand up and walk!", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        };
    }

    public void currentPositionAlert(int geofenceTransition) {
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            LocationConfig.currentTime.scheduleAtFixedRate(LocationConfig.timeTask, 0, 1000);//timer task runs every 1s.
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            LocationConfig.counter = 0;
            LocationConfig.timeConverter = 0;
            LocationConfig.timeTask.cancel();
            textViewCurrentLocationDisplay.setText("");
            initializeTimerTask();
        } else {
            //for debugging purposes
            Log.e("INVALID TRANSITION TYPE", getString(geofenceTransition));
        }
    }

    //restore user action activity when user returns
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                authenticatedName = data.getStringExtra(AUTHENTICATED_USER_BACK);
            }
        }
    }


}