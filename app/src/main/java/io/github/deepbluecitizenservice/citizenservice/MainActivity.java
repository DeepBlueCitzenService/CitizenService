package io.github.deepbluecitizenservice.citizenservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = "Main Activity:";
    private boolean isLoggedIn = false;
    private GoogleApiClient mGAP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        mGAP = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        SharedPreferences prefs = getSharedPreferences(getString(R.string.user_preferences_id), Context.MODE_PRIVATE);

        //Check if logged in through shared preferences
        isLoggedIn = prefs.getBoolean(getString(R.string.logged_in_state), false);

        //Check if user is logged in to firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null){
            Log.d(TAG, "Current user is: "+ user.getDisplayName());
        }

        Log.d(TAG, "Logged in state in shared preferences" + (isLoggedIn? "True": "False"));
        Log.d(TAG, prefs.getBoolean(getString(R.string.logged_in_state), false)? "True" : "False");

        //While we redirect to the login activity if not connected to firebase OR google, it might be
        //wise to handle these values separately. This is because shared preferences are available offline
        //So the non login screens should be visible if user was logged in before the connection was lost
        if(!isLoggedIn || user==null){
            //If not logged in, start the login activity
            Intent startLoginActivity = new Intent(this, LoginActivity.class);
            startActivity(startLoginActivity);
        }
    }

    //Handle clicking logout button
    //Might replace this to another screen later
    //This is here for testing logging out and logging in
    public void handleLogoutButtonClick(View v){
        if(mGAP.isConnected()) {
            Auth.GoogleSignInApi.signOut(mGAP).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            Log.d(TAG, "Logged out");
                            SharedPreferences prefs = getSharedPreferences(getString(R.string.user_preferences_id), MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();

                            //Set logged in state to false
                            editor.putBoolean(getString(R.string.logged_in_state), false);
                            editor.apply();

                            //Log out of firebase
                            if(FirebaseAuth.getInstance().getCurrentUser()!=null)
                                FirebaseAuth.getInstance().signOut();

                            Intent login = new Intent(getBaseContext(), LoginActivity.class);
                            startActivity(login);
                        }
                    });
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
