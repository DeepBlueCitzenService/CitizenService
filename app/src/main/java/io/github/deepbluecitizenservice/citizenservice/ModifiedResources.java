package io.github.deepbluecitizenservice.citizenservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import io.github.deepbluecitizenservice.citizenservice.fragments.SettingsFragment;

public class ModifiedResources extends Resources {

    SharedPreferences preferences;

    public ModifiedResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        super(assets, metrics, config);
    }

    public ModifiedResources(Context context, Resources original) {
        super(original.getAssets(), original.getDisplayMetrics(), original.getConfiguration());
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int getColor(int id) throws NotFoundException {
        return getColor(id, null);
    }

    @Override
    public int getColor(int id, Theme theme) throws NotFoundException {
        int themeId = -1;
        switch (getResourceEntryName(id)) {
            case "colorPrimary":
                themeId = preferences.getInt(SettingsFragment.SP_THEME, -1);
                switch (themeId){
                    case 0:
                        return 0xFF3F51B5;
                    case 1:
                        return 0xFF2C3E50;
                    case 2:
                        return 0xFF34495E;
                    case 3:
                        return 0XFF757575;
                    case 4:
                        return 0xFF009688;
                    case 5:
                        return 0xFF795548;
                    default:
                        return 0xFF3F51B5;
                }
            case "colorPrimaryDark":
                themeId = preferences.getInt(SettingsFragment.SP_THEME, -1);
                switch (themeId){
                    case 0:
                        return 0xFF303F9F;
                    case 1:
                        return 0xFF1B2732;
                    case 2:
                        return 0xFF212e3B;
                    case 3:
                        return 0XFF616161;
                    case 4:
                        return 0xFF00796B;
                    case 5:
                        return 0xFF5D4037;
                    default:
                        return 0xFF3F51B5;
                }
            case "colorAccent":
                themeId = preferences.getInt(SettingsFragment.SP_THEME, -1);
                switch (themeId){
                    case 0:
                        return 0xFFFF4081;
                    case 1:
                        return 0xFFFFEB3B;
                    case 2:
                        return 0xFF1ABC9C;
                    case 3:
                        return 0xFF2ECC71;
                    case 4:
                        return 0xFFFF5722;
                    case 5:
                        return 0xFF2196F3;
                    default:
                        return 0xFFFF4081;
                }
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return super.getColor(id, theme);
                }
                return super.getColor(id);
        }
    }
}
