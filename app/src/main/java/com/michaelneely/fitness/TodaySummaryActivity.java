package com.michaelneely.fitness;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TodaySummaryActivity extends AppCompatActivity {

    TextView textViewDisplayTodaySummary;
    Button backToUserActionActivityButton;
    AuthenticationDatabaseHelper authenticationDatabaseHelper;
    DistanceDatabaseHelper distanceDatabaseHelper;
    String formattedDate;
    UserActionActivity userActionActivityInstance;
    private final String AUTHENTICATED_USER = "AUTH_USER_NAME";
    private final String AUTHENTICATED_USER_BACK = "AUTH_USER_NAME_BACK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_summary);
        textViewDisplayTodaySummary = findViewById(R.id.board);
        backToUserActionActivityButton = findViewById(R.id.go_back_to_user_action_button);
        initializePageOperation();
    }

    public class PairsHandler {
        DistanceHandler startNode;

        PairsHandler() {
            startNode = null;
        }

        public void insert(DistanceHandler distanceHandlerHead) {
            if (startNode == null) {
                startNode = distanceHandlerHead;
            } else {
                DistanceHandler temporaryNode = startNode;
                while ((temporaryNode.distanceCovered > distanceHandlerHead.distanceCovered) && (temporaryNode.nextEntity != null)) {
                    temporaryNode = temporaryNode.nextEntity;
                }
                while ((temporaryNode.distanceCovered == distanceHandlerHead.distanceCovered) && temporaryNode.memberName.compareTo(distanceHandlerHead.memberName) < 0 && (temporaryNode.nextEntity != null)) {
                    temporaryNode = temporaryNode.nextEntity;
                }
                if (temporaryNode.distanceCovered > distanceHandlerHead.distanceCovered) {
                    temporaryNode.nextEntity = distanceHandlerHead;
                    distanceHandlerHead.previousEntity = temporaryNode;
                } else if ((temporaryNode.distanceCovered == distanceHandlerHead.distanceCovered) && temporaryNode.memberName.compareTo(distanceHandlerHead.memberName) < 0) {
                    temporaryNode.nextEntity = distanceHandlerHead;
                    distanceHandlerHead.previousEntity = temporaryNode;
                } else {
                    if (temporaryNode.previousEntity != null) {
                        temporaryNode.previousEntity.nextEntity = distanceHandlerHead;
                        distanceHandlerHead.previousEntity = temporaryNode.previousEntity;
                        distanceHandlerHead.nextEntity = temporaryNode;
                        temporaryNode.previousEntity = distanceHandlerHead;
                    } else {
                        distanceHandlerHead.nextEntity = temporaryNode;
                        temporaryNode.previousEntity = distanceHandlerHead;
                        startNode = distanceHandlerHead;
                    }
                }
            }
        }
    }

    public class DistanceHandler {
        private double distanceCovered;
        private DistanceHandler previousEntity;
        private String memberName;
        private DistanceHandler nextEntity;

        DistanceHandler(String sportsManName, double sportsManDistance) {
            distanceCovered = sportsManDistance;
            nextEntity = null;
            memberName = sportsManName;
            previousEntity = null;
        }
    }

    private void initializePageOperation() {
        updateTodayStatistics();
        finishAndGoBackToPreviousActivity();
        Intent intentFromCallerActivity = getIntent();
        String username = intentFromCallerActivity.getStringExtra(AUTHENTICATED_USER);
        Intent intentBackToCallerActivity = new Intent();
        intentBackToCallerActivity.putExtra(AUTHENTICATED_USER_BACK, username);
        setResult(RESULT_OK, intentBackToCallerActivity);
    }

    public void finishAndGoBackToPreviousActivity() {
        backToUserActionActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void updateTodayStatistics() {
        authenticationDatabaseHelper = MainActivity.authenticationDatabase;
        userActionActivityInstance = UserActionActivity.instance;
        formattedDate = LocationConfig.fitnessAppDateFormatted;
        Cursor authenticationDatabaseCursor = authenticationDatabaseHelper.getAllData();
        PairsHandler allUsersName = new PairsHandler();
        while (authenticationDatabaseCursor.moveToNext()) {
            String sportsName = authenticationDatabaseCursor.getString(0);
            distanceDatabaseHelper = new DistanceDatabaseHelper(userActionActivityInstance, sportsName);
            double distance = distanceDatabaseHelper.getDistance(formattedDate);
            DistanceHandler pair = new DistanceHandler(sportsName, distance);
            allUsersName.insert(pair);
        }
        DistanceHandler temporaryDistanceHandler = allUsersName.startNode;
        while (temporaryDistanceHandler != null) {
            textViewDisplayTodaySummary.append(temporaryDistanceHandler.memberName + ":  " + temporaryDistanceHandler.distanceCovered + " feet\n");
            temporaryDistanceHandler = temporaryDistanceHandler.nextEntity;
        }
    }


}