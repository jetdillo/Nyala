
package com.rkl.nyala;

import java.io.File;

import android.app.Activity;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.RemoteException;

import android.app.Activity;
import android.app.AlertDialog;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.provider.Settings;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.R;
import com.google.zxing.client.android.encode.*;
import com.google.zxing.common.*;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Gallery.LayoutParams;

/* Activity to share a previously scanned QRCode with a friend */
/*Like your tablet after you've scanned your phone online */
/*Bits and pieces of code swiped from the ZXing projects' Encode Activity */

public class NyalaShare extends Activity {
  
	String nyshareStr = new String("");
	NyalaLib nl = new NyalaLib(this);
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
        
       Intent nyshareIntent = getIntent();
       if (nyshareIntent != null) {
           setContentView(R.layout.share_code);
           nyshareStr = nyshareIntent.getStringExtra("qrstr");
           String nyshareSSID = nyshareIntent.getStringExtra("qrssid");
           
    	if (!(nyshareStr.equals("None")) ){
    	   
    	   Log.i("INFO","nyshareStr="+nyshareStr);
    	   showScanBitmap(nyshareStr); 
    	   
           TextView shareText = (TextView) findViewById(R.id.nyShareTxt);
           shareText.setText(nyshareSSID);
    	} else {
    		   Log.i("INFO", "No Recent Scan...searching for most recent save");
    		   nyshareStr = new String(nl.getLastScanFile());
    		    if (nyshareStr.equals("None")) {
    		       	nl.SimpleDialogFactory("No Scan to Display", "Back", NyalaShare.this);
    		    } else {
    		    	    showScanBitmap(nyshareStr);
    		    }
    		}
    	 
    	}
     }
      
    public void onResume(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	Log.i("INFO","IN NyalaShare onResume");
    	
    }
    
    public void showScanBitmap(String bitMapStr) {
    	DisplayMetrics dm = new DisplayMetrics();
 	   getWindowManager().getDefaultDisplay().getMetrics(dm);
 	   int hip = dm.heightPixels;
 	   int wip = dm.widthPixels;
 	
 	   int minside = 0;
 	
 	   if (hip < wip) {
 		   minside = hip;
 	   } else {
 		   minside = wip;
 	   }
 	
 	   Intent encode_intent = new Intent("com.google.zxing.client.android.ENCODE");
 	   encode_intent.setAction(Intents.Encode.ACTION);
 	   encode_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
 	   encode_intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
 	   encode_intent.putExtra(Intents.Encode.DATA, bitMapStr);
 	   encode_intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
 	   QRCodeEncoder nyshareEncoder = new QRCodeEncoder(this,encode_intent,minside);
 	
 	   setTitle(getString(R.string.app_name) + " - " + nyshareEncoder.getTitle());
        Bitmap nyshareBm = null;
		   try {
			   nyshareBm = nyshareEncoder.encodeAsBitmap();
		   } catch (WriterException e) {
			   e.printStackTrace();
		   }
        ImageView iv = (ImageView) findViewById(R.id.sharecodeIV);
        iv.setImageBitmap(nyshareBm);
    }
    
   
    }