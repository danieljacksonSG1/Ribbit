package au.com.wsit.ribbit.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import au.com.wsit.ribbit.R;
import au.com.wsit.ribbit.RibbitApplication;


public class LoginActivity extends ActionBarActivity {

    protected EditText mUsername;
    protected EditText mPassword;
    protected Button LoginButton;

    protected TextView mSignUpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable the progress bar in the action bar
        //supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        getSupportActionBar().hide();

        // Get a handle on the ids
        mSignUpTextView = (TextView) findViewById(R.id.signUpText);
        mUsername = (EditText) findViewById(R.id.userNameField);
        mPassword = (EditText) findViewById(R.id.passwordField);
        LoginButton = (Button) findViewById(R.id.loginButton);

        // Create our listener for the sign up text
        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // Go to the Sign up activity
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        // Listen for button clicks on our login button
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the username and password from the EditText fields
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();


                // Trim white spaces from the end
                username = username.trim();
                password = password.trim();

                // Validate user input
                if (username.isEmpty() || password.isEmpty()) {
                    // Create the alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage(R.string.login_error_message)
                            .setTitle(R.string.login_error_title)
                            .setPositiveButton(android.R.string.ok, null);

                    AlertDialog dialog = builder.create();
                    dialog.show();


                } else {
                    // Login
                    // Show the progress spinner to show we are logging in
                    //setSupportProgressBarIndeterminateVisibility(true);
                    // Pass the username and password to the Parse class
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            // When we get the callback, hide the progress spinner
                            //setSupportProgressBarIndeterminateVisibility(false);
                            // If the exception from the callback in null then the login succeeds
                            if (e == null)
                            {
                                // Success
                                RibbitApplication.updateParseInstallation(user);

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else
                            {
                                // Something went wrong
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setMessage(R.string.login_error_message)
                                        .setTitle(R.string.login_error_title)
                                        .setPositiveButton(android.R.string.ok, null);

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }

                        }
                    });

                }


            }
        });
    }




}
