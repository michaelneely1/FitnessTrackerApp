package com.michaelneely.fitness;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUpActivity extends AppCompatActivity {
    private EditText editTextEnterUsername;
    private EditText editTextEnterPassword;
    private Button buttonSignUp;
    private String userNameEntered;
    //database that handles authentication
    static AuthenticationDatabaseHelper authenticationDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //get reference to views
        authenticationDatabase = new AuthenticationDatabaseHelper(this);
        editTextEnterUsername = findViewById(R.id.edit_text_enter_name_sign_up_page);
        editTextEnterPassword = findViewById(R.id.edit_text_enter_password_sign_up_page);
        buttonSignUp = findViewById(R.id.button_sign_up_sign_up_page);

        //allow user to create a new account
        handleUserSignUp();
    }

    public void handleUserSignUp() {
        buttonSignUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                userNameEntered = editTextEnterUsername.getText().toString();
                if(userNameEntered.length()<4){
                    Toast.makeText(getApplicationContext(), "Username cannot be less than 4 characters long", Toast.LENGTH_SHORT).show();

                }else {
                    boolean isAccountFound = authenticationDatabase.isAccountFoundInTheDatabase(userNameEntered);
                    processBasedProvidedInfo(isAccountFound);
                }

            }
        });
    }

    private void processBasedProvidedInfo(boolean isAccountFound) {
        //inform user of account existence if user tries creating a new account with username found in the database
        if (isAccountFound) {
            Toast.makeText(SignUpActivity.this, "Account in use,try another username!", Toast.LENGTH_LONG).show();
        }
        //process account that is not found
        else {
            boolean isSignUpSuccessful = authenticationDatabase.createAccount(userNameEntered,
                    editTextEnterPassword.getText().toString());
            //if signup completed successful inform user using toast message
            if (isSignUpSuccessful) {
                Toast.makeText(SignUpActivity.this, "Account creation was successful", Toast.LENGTH_LONG).show();
                Intent intentStartLogin = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intentStartLogin);

            }
            //if signup did not succeed due to internal error inform the user
            else {
                Toast.makeText(SignUpActivity.this, "Account creation Failed,An Error occured!", Toast.LENGTH_LONG).show();
            }
        }
    }
}