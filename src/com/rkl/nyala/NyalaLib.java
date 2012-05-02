package com.rkl.nyala;
import android.app.Activity;
import android.os.Environment;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.graphics.Bitmap;

import android.provider.Settings;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;

import java.io.*;

public class NyalaLib {
   
   public void saveScanToStorage(Bitmap bm,String ssidstr) {
	  	 String storagePath = Environment.getExternalStorageDirectory().toString();
	  	 Log.i("INFO","External Storage Path is "+storagePath);
	  	 OutputStream os = null;
	  	    File file = new File(storagePath, "nyalascan.png");
	  	    try {
	  	     os = new FileOutputStream(file);
	  	     bm.compress(Bitmap.CompressFormat.PNG, 100, os);
	  	     //This is kind of lazy and needs to be handled better
	  	     os.flush();
	  	     os.close();
	  	    }
	  	    catch(IOException ioe) {
	  	    	
	  	    }
	   } 
}