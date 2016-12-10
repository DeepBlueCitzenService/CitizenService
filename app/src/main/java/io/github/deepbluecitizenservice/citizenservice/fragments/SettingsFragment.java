package io.github.deepbluecitizenservice.citizenservice.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import io.github.deepbluecitizenservice.citizenservice.MainActivity;
import io.github.deepbluecitizenservice.citizenservice.R;

public class SettingsFragment extends Fragment {

    public static String SP_NOTIFICATION = "settingsNotification";
    public static String SP_LANGUAGE = "SettingsLanguage";
    public static String SP_THEME = "SettingsTheme";

    private int notificationStatus = 0;
    private int languageStatus = 0;
    private int themeStatus = 0;

    private String[] notificationGroup;
    private String[] themeGroup;
    private String[] languageGroup = {
            "English",
            "हिंदी"
    };


    public static final int INDIGO_PINK = 0;
    public static final int MIDNIGHT_BLUE_YELLOW = 1;
    public static final int WET_ASPHALT_TURQUOISE = 2;
    public static final int GREY_EMERALD = 3;
    public static final int TEAL_ORANGE = 4;
    public static final int BROWN_BLUE = 5;

    private OnSettingsFragmentInteraction mListener;

    private SharedPreferences sharedPreferences;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        notificationGroup = new String[]{
                getString(R.string.on),
                getString(R.string.off)
        };

        themeGroup = new String[]{
                getString(R.string.theme_indigo_pink),
                getString(R.string.theme_midNightBlue_yellow),
                getString(R.string.theme_wetAsphalt_turquoise),
                getString(R.string.theme_grey_emerald),
                getString(R.string.theme_teal_orange),
                getString(R.string.theme_brown_blue)
        };
        
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        notificationStatus = sharedPreferences.getInt(SP_NOTIFICATION, 0);
        languageStatus = getLocaleInt(sharedPreferences.getString(SP_LANGUAGE, "en"));
        themeStatus = sharedPreferences.getInt(SP_THEME, 0);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        View notificationButton = view.findViewById(R.id.settings_notifications);
        View languageButton = view.findViewById(R.id.settings_language);
        View themeButton = view.findViewById(R.id.settings_theme);
        View librariesButton = view.findViewById(R.id.settings_libraries);
        View licencesButton = view.findViewById(R.id.settings_licences);
        View aboutButton = view.findViewById(R.id.settings_about);
        View logoutButton = view.findViewById(R.id.settings_log_out);

        TextView userNameTV = (TextView) view.findViewById(R.id.settings_user_name);
        TextView userEmailTV = (TextView) view.findViewById(R.id.settings_user_email);
        ImageView userImage = (ImageView) view.findViewById(R.id.settings_user_image);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userNameTV.setText(user.getDisplayName());
            userEmailTV.setText(user.getEmail());
            Glide.with(getContext()).load(user.getPhotoUrl()).crossFade().into(userImage);
        }

        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(getString(R.string.notification_dialog_title), notificationGroup,
                        notificationStatus, getNotificationDialogListener());
            }
        });


        languageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(getString(R.string.language_dialog_title),languageGroup,
                        languageStatus, getLanguageDialogListener());
            }
        });

        themeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(getString(R.string.themes_dialog_title), themeGroup,
                        themeStatus, getThemeDialogListener());
            }
        });

        librariesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.dialog_libraries);
                dialog.show();
            }
        });

        licencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.dialog_licences);
                dialog.show();
            }
        });

        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.dialog_about);
                dialog.show();
            }
        });


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onLogoutClick();
            }
        });
        return view;
    }

    private void showDialog(String title, final String[] group,
                            int prevSelection, DialogInterface.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title)
                .setSingleChoiceItems(group, prevSelection, listener)
                .create()
                .show();
    }

    private DialogInterface.OnClickListener getNotificationDialogListener(){
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selection) {
                notificationStatus = selection;
                sharedPreferences.edit().putInt(SP_NOTIFICATION, selection).apply();
                //TODO : Handle selection
                Toast.makeText(getContext(), "WIP Feature; Will be added soon", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        };
    }

    private DialogInterface.OnClickListener getLanguageDialogListener(){
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selection) {
                languageStatus = selection;
                String localeString = getLocaleString(languageStatus);
                sharedPreferences.edit().putString(SP_LANGUAGE, localeString).apply();
                dialog.dismiss();
                getActivity().finish();
                startActivity(new Intent(getContext(), MainActivity.class));
            }
        };
    }

    private DialogInterface.OnClickListener getThemeDialogListener(){
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selection) {
                themeStatus = selection;
                sharedPreferences.edit().putInt(SP_THEME, selection).apply();
                dialog.dismiss();
                getActivity().finish();
                startActivity(new Intent(getContext(), MainActivity.class));

            }
        };
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingsFragmentInteraction) {
            mListener = (OnSettingsFragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnSettingsFragmentInteraction {
        void onLogoutClick();
    }

    private String getLocaleString(int idx){
        switch (idx){
            default:
            case 0:
                return "en";
            case 1:
                return  "hi";
        }
    }

    private int getLocaleInt(String locale){
        switch (locale){
            default:
            case "en":
                return 0;
            case "hi":
                return 1;
        }
    }

    public static void setLocaleFromSharedPreferences(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String localeString = preferences.getString(SettingsFragment.SP_LANGUAGE, "en");

        Locale locale = new Locale(localeString);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}
