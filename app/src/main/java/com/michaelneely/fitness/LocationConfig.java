package com.michaelneely.fitness;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

public class LocationConfig {
    static DistanceDatabaseHelper distanceDatabaseHelper;
    static String fitnessAppDateFormatted;
    static float fitnessAppDistance;
    static float fitnessAppDistanceStored;
    static int fitnessAppMilesConverter;
    static LocationManager fitnessApLocationManager;
    static LocationListener fitnessAppLocationListener;
    static Location fitnessAppLastLocationRecorded;
    static double currentLocationLatitude = -91;
    static double currentLocationLongitude = -181;
    static Location currentLocation = null;
    static boolean isUserInSameLocation = false;
    static Timer currentTime;
    static TimerTask timeTask;
    static final Handler fitnessAppHandler = new Handler();
    static int counter = 0;
    static int timeConverter = 0;
    static final String latitudeSearchStringFromDatabase="officeLatitude";
    static final String longitudeSearchStringFromDatabase="officeLongitude";
}
