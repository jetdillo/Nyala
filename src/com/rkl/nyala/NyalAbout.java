package com.rkl.nyala;

import android.app.Activity;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


public class NyalAbout extends Activity{

	    protected void onCreate(Bundle savedInstanceState) {
	       super.onCreate(savedInstanceState);
           /*
	        ImageView iv = new ImageView(this);
	        iv.setImageResource(R.drawable.nyala_side);
	        iv.setAdjustViewBounds(true); 
	        */
	        setContentView(R.layout.nyalabout);
	    }   
}
