package io.github.deepbluecitizenservice.citizenservice;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import io.github.deepbluecitizenservice.citizenservice.database.CustomDatabase;
import io.github.deepbluecitizenservice.citizenservice.permission.ContactsPermission;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String SP_ID = "UserPreferences";
    public static final String SP_LOGGED_IN_STATE = "LoggedInState";
    public static final String SP_USER_NAME = "UserName";
    public static final String SP_USER_EMAIL = "UserEmail";
    public static final String SP_USER_EXISTS_IN_FIREBASE = "UserExistsInFirebase";

    public static final int STARTUP_DELAY = 300;
    public static final int ANIM_ITEM_DURATION = 1000;
    public static final int ITEM_DELAY = 300;

    private boolean animationStarted = false;

    private GoogleApiClient mGAP;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "Login Activity";
    private static int RC_SIGN_IN = 1;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_SplashScreen);
        setContentView(R.layout.activity_login);

        //Attach intent for signing in to a button
        SignInButton signInButton = (SignInButton) findViewById(R.id.login_sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkContactsPermission()){
                    Intent SignInIntent = Auth.GoogleSignInApi.getSignInIntent(mGAP);
                    startActivityForResult(SignInIntent, RC_SIGN_IN);
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage(getString(R.string.progress_dialog_authenticating));
                    progressDialog.show();
                }
            }
        });

        //Enable the google sign in API
        //Request for email as well
        //The ID token is required for FireBase Authorization
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        //Build the Google Api client
        mGAP = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //Get FireBase Authorization instance
        mAuth = FirebaseAuth.getInstance();

        //Listen for changes in authorization (this is just for testing for now)
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "Firebase logged in: " + user.getUid());
                } else {
                    Log.d(TAG, "Firebase logged out");
                }
            }
        };
    }

    private boolean checkContactsPermission(){
        View baseView = findViewById(R.id.login_base_view);
        ContactsPermission permission = new ContactsPermission(this);
        permission.askPermissions(baseView);
        return  permission.isGranted();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus || animationStarted) return;
        animate();
        super.onWindowFocusChanged(hasFocus);
    }

    //Connect to google api and start listening for firebase changes
    @Override
    protected void onStart() {
        super.onStart();
        mGAP.connect();
        mAuth.addAuthStateListener(mAuthListener);

    }

    //Disconnect from google api and stop listening for firebase changes
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
        mGAP.disconnect();
        if(progressDialog!=null) {
            if(progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    private void animate() {
        ImageView logoIV = (ImageView) findViewById(R.id.login_logo);
        TextView appNameTV = (TextView) findViewById(R.id.login_app_name);
        SignInButton signInButton = (SignInButton) findViewById(R.id.login_sign_in);

        ViewCompat.animate(logoIV)
                .translationY(-250)
                .setStartDelay(STARTUP_DELAY)
                .setDuration(ANIM_ITEM_DURATION).setInterpolator(
                new DecelerateInterpolator(1.2f)).start();

        ViewCompat.animate(appNameTV)
                .translationY(50).alpha(1)
                .setStartDelay(STARTUP_DELAY + 200)
                .setDuration(1000).setInterpolator(
                new DecelerateInterpolator()).start();

        ViewCompat.animate(signInButton)
                .scaleY(1f).scaleX(1f).alpha(1)
                .setStartDelay(STARTUP_DELAY + ITEM_DELAY + 200)
                .setDuration(500);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //After signing in, handle the return
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //If sign in is successful, start the process for authorizing next activities
        if(requestCode ==  RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }


    //Get the sign in account if successful login and account exists
    //Then navigate to the main activity
    protected void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            //Authorize firebase with current google account
            firebaseAuthWithGoogle(account);
        }
    }

    //Authenticate firebase with google account
    private void firebaseAuthWithGoogle(GoogleSignInAccount account){
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        //To access the account inside inner anonymous class
        final GoogleSignInAccount act = account;

        //Get credentials to sign into firebase
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        //Sign into firebase
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                        }

                        //Start an intent to start the main activity
                        Intent startMainActivity = new Intent(getBaseContext(), MainActivity.class);
                        startMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        //In shared preferences, store some user data and the log in state
                        //This is to let the app work when offline and faster access
                        SharedPreferences prefs = getSharedPreferences(SP_ID, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        editor.putBoolean(SP_LOGGED_IN_STATE, true);
                        editor.putString(SP_USER_NAME, act.getDisplayName());
                        editor.putString(SP_USER_EMAIL, act.getEmail());

                        //Create a new user if logging in for the first time
                        if(!prefs.getBoolean(SP_USER_EXISTS_IN_FIREBASE, false)) {
                            CustomDatabase db = new CustomDatabase(FirebaseDatabase.getInstance().getReference());
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            db.createUser(user.getDisplayName(), user.getEmail(), user.getUid(), user.getPhotoUrl());
                            editor.putBoolean(SP_USER_EXISTS_IN_FIREBASE, true);
                        }

                        editor.apply();

                        startActivity(startMainActivity);
                        finish();
                    }
                });
    }

    //This prevents the login activity from going back to the main activity (which launches it)
    //So when the user press 'back' it acts as the home button. The advantage is that the back stack
    //is not cleared this way, it just acts like it for this screen
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}