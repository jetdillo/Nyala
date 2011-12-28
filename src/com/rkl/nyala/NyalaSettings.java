package com.rkl.nyala;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class NyalaSettings extends PreferenceActivity {
	
	private SharedPreferences NyalaPrefs;
	
    CheckBoxPreference exitPrefState;
    OnPreferenceClickListener PrefStateClickHandler;

    private static final String NyalaPrefStr = new String("NyalaPrefs");
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.nyala_settings);
        //setContentView(R.layout.nyala_settings);
        
        PrefStateClickHandler onPrefStateClickHandler = new PrefStateClickHandler();
        
       exitPrefState= (CheckBoxPreference) findPreference("exitPrefState");
    }
    
 class PrefStateClickHandler implements OnPreferenceClickListener {

        public boolean onPreferenceClick(Preference pref) {
        	    boolean result;
  	    
        	    Editor NyalaPrefsEditor;
				if (pref instanceof CheckBoxPreference ) {
        	    
                CheckBoxPreference CBP = (CheckBoxPreference)pref;
                NyalaPrefs = getSharedPreferences(NyalaPrefStr,MODE_PRIVATE);
    	    	  NyalaPrefsEditor = NyalaPrefs.edit();
    	    	  
                try {
                	    
                        if( CBP.getKey().equals("connectAction")) {
                        	result = CBP.isChecked();
                            if( CBP.isChecked() ) {
                  	    	  NyalaPrefsEditor.putInt("connectAction", 1);
                  	    	  CBP.setChecked(true);
                            } else {
                        	NyalaPrefsEditor.putInt("connectAction",0);
                        	CBP.setChecked(false);
                            }
                        
                        }
                        
                          NyalaPrefsEditor.commit();
                          return CBP.isChecked();
                          
                } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
                
        	  }
        	  
        	  return false;
      }
 }
 private boolean isNull (String str) {
	    return str.equals(null);
	}
    
}





