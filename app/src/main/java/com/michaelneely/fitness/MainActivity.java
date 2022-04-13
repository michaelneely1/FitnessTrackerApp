package com.michaelneely.fitness;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //field required in the MainActivity
    private EditText editTextEnterUsername;
    private EditText editTextEnterPassword;
    private Button loginButton;
    private TextView goToSignUpActivity_textView;
    static AuthenticationDatabaseHelper authenticationDatabase;
    private String userNameEntered;
    private final String AUTHENTICATED_USER="AUTH_USER_NAME";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        authenticationDatabase = new AuthenticationDatabaseHelper(this);

        //instantiate views in the main activity page
        editTextEnterUsername = findViewById(R.id.edit_text_username_login_page);
        editTextEnterPassword = findViewById(R.id.edit_text_password_login_page);
        loginButton = findViewById(R.id.button_login_login_page);
        goToSignUpActivity_textView = findViewById(R.id.text_go_to_login_activity);

        //attach listener to sign up textView
        goToSignUpActivity_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentSignUp = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intentSignUp);
            }
        });

        //will attach lister to login button which when clicked will initiate login process
        handleUserLogin();
    }


    public void handleUserLogin() {
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //initiate login process by taking username input
                userNameEntered = editTextEnterUsername.getText().toString();

                //before proceeding further it is important to check if user name exists in the database
                //Taking this step will help avoid exception which might occur when wanting to validate a user
                //that does not exist
                boolean isAccountFound = authenticationDatabase.isAccountFoundInTheDatabase(userNameEntered);

                //check if the provided username and password match with what is stored in the database
                boolean isAuthenticated = authenticationDatabase.checkAccount(userNameEntered,
                        editTextEnterPassword.getText().toString());

                //if account is found in the database handle authentication
                handleAuthentication(isAccountFound,isAuthenticated);
            }
        });
    }

    private void handleAuthentication(boolean isAccountFound, boolean isAuthenticated) {
        //only handle authentication if account is found in the database
        if (isAccountFound) {

            //if account is found and users password and username match take the user to user action page
            if (isAuthenticated) {
                Intent intentStartUserAction = new Intent(MainActivity.this, UserActionActivity.class);
                intentStartUserAction.putExtra(AUTHENTICATED_USER, userNameEntered);
                startActivity(intentStartUserAction);
            }
            //if username is found but password is wrong inform the user
            else {
                Toast.makeText(MainActivity.this, "Wrong Password!", Toast.LENGTH_LONG).show();
            }
        }
        //when user enters a username that is not found in the database inform them
        else {
            Toast.makeText(MainActivity.this, "Account not found!", Toast.LENGTH_LONG).show();
        }
    }
}
