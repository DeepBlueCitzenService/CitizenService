package io.github.deepbluecitizenservice.citizenservice.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import io.github.deepbluecitizenservice.citizenservice.MainActivity;
import io.github.deepbluecitizenservice.citizenservice.R;

public class SettingsFragment extends Fragment {
    private final String TAG = "SettingsFragment";

    public static String SP_NOTIFICATION = "settingsNotification";
    public static String SP_LANGUAGE = "SettingsLanguage";
    public static String SP_THEME = "SettingsTheme";

    private int notificationStatus = -1;
    private int languageStatus = -1;
    private int themeStatus = -1;

    private String[] notificationGroup = {"on","off"};
    private String[] languageGroup = {"English", "हिंदी"};
    private String[] themeGroup = {"Indigo-Pink", "MightNightBlue-yello",
            "WetAsphalt-Turquoise", "Grey-Emerald", "Teal-Orange", "Brown-Blue"};

    private OnSettingsFragmentInteraction mListener;

    private SharedPreferences sharedPreferences;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        notificationStatus = sharedPreferences.getInt(SP_NOTIFICATION, -1);
        languageStatus = sharedPreferences.getInt(SP_LANGUAGE, -1);
        themeStatus = sharedPreferences.getInt(SP_THEME, -1);
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
                showDialog("Notification", notificationGroup,
                        notificationStatus, getNotificationDialogListener());
            }
        });


        languageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog("Select Language",languageGroup,
                        languageStatus, getLanguageDialogListener());
            }
        });

        themeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog("Select Theme", themeGroup,
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
                sharedPreferences.edit().putInt(SP_LANGUAGE, selection).apply();
                Toast.makeText(getContext(), "WIP Feature; Will be added soon", Toast.LENGTH_LONG).show();
                //TODO : Handle selection
                dialog.dismiss();
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
}
