package com.rkl.nyala;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class NyalaSettings extends PreferenceActivity {
	
	private SharedPreferences NyalaPrefs;
    

    private static final String NyalaPrefStr = new String("NyalaSettings");
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.nyala_settings);
    }
 private boolean isNull (String str) {
	    return str.equals(null);
	}
    
}