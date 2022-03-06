package com.shapun.screentranslater.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import com.mannan.translateapi.Language;
import com.shapun.screentranslater.R;
import com.shapun.screentranslater.theme.Theme;
import java.io.File;
import java.util.Locale;

public final class Preferences {
	
    private SharedPreferences mPrefernces;
    private static final String PREFERENCES = "preferences";
    private static final String LANGUAGE = "language";
    private static final String SERVICE_ENABLED = "service_enabled";
	private static final String SELECTED_THEME = "selected_theme";

  public static Language getLanguage(Context context) {
        SharedPreferences preferences = getPreferences(context);
        if (preferences.contains(LANGUAGE)) {
            return Language.getLanguage(preferences.getString(LANGUAGE, "en"));
        } else {
            Language language = Language.getLanguage(Locale.getDefault().getLanguage());
            return (language == null) ? Language.getLanguage("en") : language;
        }
    }

    public static void setLanguage(Context context, Language language) {
        getPreferences(context).edit().putString(LANGUAGE, language.code).apply();
    }	

    public static boolean isServiceEnabled(Context context) {
        return getPreferences(context).getBoolean(SERVICE_ENABLED, false);
    }

    public static void setServiceEnabled(Context context, boolean val) {
        getPreferences(context).edit().putBoolean(SERVICE_ENABLED, val).apply();
    }
	
	public static void setTheme(Context context,Theme theme) {
        getPreferences(context).edit().putString(SELECTED_THEME,context.getResources().getResourceEntryName(theme.resId)).apply();
    }
		
	private static String getThemeString(Context context) {
        return getPreferences(context).getString(SELECTED_THEME,"CustomThemePurple");
    }
	
	public static int getTheme(Context context) {
			return context.getResources().getIdentifier(getThemeString(context), "style", context.getPackageName());
    }
	
    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
