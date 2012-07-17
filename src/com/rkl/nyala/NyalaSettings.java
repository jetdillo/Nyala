package com.rkl.nyala;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class NyalaSettings extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	String current_scanpath = null;
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.nyala_settings); 
    
    }
    
}