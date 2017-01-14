package io.github.deepbluecitizenservice.citizenservice;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import io.github.deepbluecitizenservice.citizenservice.adapter.ViewPagerAdapter;
import io.github.deepbluecitizenservice.citizenservice.fragments.PhotoFragment;
import io.github.deepbluecitizenservice.citizenservice.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        PhotoFragment.OnPhotoListener, SettingsFragment.OnSettingsFragmentInteraction {

    private final static String TAG = "Main Activity:";
    private GoogleApiClient mGAP;
    private AHBottomNavigation bottomNavigation;

    public final static String HOME_TAG="HOME", ALL_TAG="ALL";
    private boolean backPressed = false;
    private ArrayList<Integer> backStack;

    private ViewPager viewPager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeFromPreferences();
        SettingsFragment.setLocaleFromSharedPreferences(this);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.progress_dialog_logging_in));


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                handleLogCheck();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressDialog.dismiss();

                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

                viewPager.setCurrentItem(0, true);
                backStack = new ArrayList<>();
                backStack.add(0);

                if(bottomNavigation==null)
                    createBottomBar(savedInstanceState==null);

                adjustFragmentWithBottomBar();
            }
        }.execute();
    }

    @Override
    public void onResume(){
        adjustFragmentWithBottomBar();
        super.onResume();
    }

    private void setThemeFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        switch (preferences.getInt(SettingsFragment.SP_THEME, 0)){
            default:
            case SettingsFragment.INDIGO_PINK:
                setTheme(R.style.AppTheme_IndigoPink);
                break;
            case SettingsFragment.MIDNIGHT_BLUE_YELLOW:
                setTheme(R.style.AppTheme_MidNightBlueYellow);
                break;
            case SettingsFragment.WET_ASPHALT_TURQUOISE:
                setTheme(R.style.AppTheme_WetAsphaltTurquoise);
                break;
            case SettingsFragment.GREY_EMERALD:
                setTheme(R.style.AppTheme_GreyEmerald);
                break;
            case SettingsFragment.TEAL_ORANGE:
                setTheme(R.style.AppTheme_TealOrange);
                break;
            case SettingsFragment.BROWN_BLUE:
                setTheme(R.style.AppTheme_BlueBrown);
        }
    }

    //Login check and handler
    private void handleLogCheck(){
        Log.d(TAG, "Logging in.....");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        mGAP = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        SharedPreferences prefs = getSharedPreferences(LoginActivity.SP_ID, Context.MODE_PRIVATE);

        //Check if logged in through shared preferences
        boolean isLoggedIn = prefs.getBoolean(LoginActivity.SP_LOGGED_IN_STATE, false);

        //Check if user is logged in to firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null){
            Log.d(TAG, "Current user is: "+ user.getDisplayName());
        }

        Log.d(TAG, "Logged in state in shared preferences" + (isLoggedIn? "True": "False"));
        Log.d(TAG, prefs.getBoolean(LoginActivity.SP_LOGGED_IN_STATE, false)? "True" : "False");

        //While we redirect to the login activity if not connected to firebase OR google, it might be
        //wise to handle these values separately. This is because shared preferences are available offline
        //So the non login screens should be visible if user was logged in before the connection was lost
        if(!isLoggedIn || user==null){
            //If not logged in, start the login activity
            Intent startLoginActivity = new Intent(this, LoginActivity.class);
            startActivity(startLoginActivity);
            finish();
        }
    }

    //Handle clicking logout button
    //Might replace this to another screen later
    //This is here for testing logging out and logging in
    //Need to move this to settings fragment

    @Override
    public void onLogoutClick(){
        if(mGAP.isConnected()) {
            Auth.GoogleSignInApi.signOut(mGAP).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            Log.d(TAG, "Logged out");
                            SharedPreferences prefs = getSharedPreferences(LoginActivity.SP_ID, MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();

                            //Set logged in state to false
                            editor.putBoolean(LoginActivity.SP_LOGGED_IN_STATE, false);
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

    private void createBottomBar(boolean isNotSaved){
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);

        TypedValue primaryColor = new TypedValue();
        TypedValue accentColor = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, primaryColor, true);
        getTheme().resolveAttribute(R.attr.colorAccent, accentColor, true);

        // Create items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.bottom_bar_home_tab, R.drawable.ic_home, primaryColor.resourceId);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.bottom_bar_all_tab, R.drawable.ic_world, primaryColor.resourceId);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.bottom_bar_camera_tab, R.drawable.ic_camera, primaryColor.resourceId);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.bottom_bar_settings_tab, R.drawable.ic_settings, primaryColor.resourceId);

        // Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);
        bottomNavigation.addItem(item4);

        // Set background color
        bottomNavigation.setDefaultBackgroundColor(primaryColor.data);

        // Disable the translation inside the CoordinatorLayout
        bottomNavigation.setBehaviorTranslationEnabled(true);

        // Change colors of icons, when active and inactive
        bottomNavigation.setAccentColor(accentColor.data);
        bottomNavigation.setInactiveColor(ContextCompat.getColor(this, R.color.white));

        // Force to tint the drawable (useful for font with icon for example)
        bottomNavigation.setForceTint(true);

        // Manage titles
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        // Set current item programmatically
        if(isNotSaved)
            bottomNavigation.setCurrentItem(0);

        // Set listeners
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                viewPager.setCurrentItem(position, true);

                if(!wasSelected  && !backPressed){
                    if(backStack ==null)
                        backStack = new ArrayList<>();
                    if(backStack.isEmpty() || backStack.get(0) != position)
                        backStack.add(0, position);
                    if(backStack.size()==5)
                        backStack.remove(4);
                }

                backPressed = false;
                return true;
            }
        });

        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), bottomNavigation));
        viewPager.setOffscreenPageLimit(4);
    }

    public void changeBottomBarSelection(int idx){
        bottomNavigation.setCurrentItem(idx);
    }

    public void adjustFragmentWithBottomBar(){
        if(bottomNavigation != null){
            int height = bottomNavigation.getHeight();
            ScrollView settingsBaseView = (ScrollView) findViewById(R.id.setting_fragment_base_view);
            ScrollView photoFragmentBaseView = (ScrollView) findViewById(R.id.photo_fragment_base_view);
            settingsBaseView.setPadding(0, 0, 0, height);
            photoFragmentBaseView.setPadding(0, 0, 0, height);
        }
    }

    public void setupNoInternetCard(CardView noInternetCard){
        noInternetCard = setCardColor(noInternetCard, R.attr.colorControlActivated);
        TextView openSettings = (TextView) noInternetCard.findViewById(R.id.no_internet_connection);
        openSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
    }

    public CardView setCardColor(CardView card, @AttrRes int attr){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        card.setBackgroundColor(typedValue.data);
        return card;
    }

    public boolean checkInternetConnectivity(CardView card){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected();

        if(card != null)
            if(!isNetworkAvailable)
                card.setVisibility(View.VISIBLE);
            else
                card.setVisibility(View.GONE);

        return isNetworkAvailable;
    }

    @Override
    public void changeView(int toWhere) {
        bottomNavigation.setCurrentItem(toWhere);
    }

    //Handle the back stack navigation
    @Override
    public void onBackPressed() {
        backPressed = true;

        if (backStack.size() > 1) {
            int position = backStack.get(1);
            Log.d(TAG, "Back stack woo " + backStack.size() + " " + backStack);

            bottomNavigation.restoreBottomNavigation();
            viewPager.setCurrentItem(position, true);

            backStack.remove(0);
        } else {
            if (backStack.size() == 1) {
                backStack.remove(0);
            }
            super.onBackPressed();
        }
    }
}
