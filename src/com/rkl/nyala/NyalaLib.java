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
import android.content.ContentResolver;

import android.graphics.Bitmap;

import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;

import java.io.*;

public class NyalaLib {
	
	Context libcontext;

public NyalaLib(Context c) {
	libcontext = c;
}
	
   public boolean saveScanToStorage(Bitmap bm,String ssidstr,Context c) {
	  SharedPreferences nyalaPrefs = PreferenceManager.getDefaultSharedPreferences(c);
       String scanSavePath = new String();
	   boolean result=false;
	   File imgdir;
	   File imgfile;
	   scanSavePath=nyalaPrefs.getString("ScanPath", scanSavePath);
	  String sdPath = Environment.getExternalStorageDirectory().toString();
      String storagePath = sdPath+"/"+scanSavePath; 
      
	  Log.i("INFO","External Storage Path is "+storagePath);
	  	 
	  	 OutputStream ostream = null;
	  	 imgdir = new File (storagePath);
	  	 if ( !(imgdir.exists()) ){
	  		 imgdir.mkdirs();
	  	 }
	  		imgfile = new File(storagePath, "nyalascan.png");
	  	
	  	    try {
	  	    	   
	  	       ostream = new FileOutputStream(imgfile);
	  	       bm.compress(Bitmap.CompressFormat.PNG, 100, ostream);
	  	        //This is kind of lazy and needs to be handled better
	  	       ostream.flush();
	  	       ostream.close();
	  	       result=true;
	  	   	  	     
	  	     } catch(IOException ioe) {
	  	    	Log.e("ERROR","Could not save "+imgfile+" to "+storagePath);
	  	    	result=false;
	  	     }
	  	    try {
		      	MediaStore.Images.Media.insertImage(c.getContentResolver(),imgfile.getAbsolutePath(),imgfile.getName(),imgfile.getName());
	  	    } catch (FileNotFoundException fnf) {
	  	    	Log.e("ERROR","MediaStore insertImage error");
	  	    	result=false;
	  	    }
	  	    return result;
} 
   
   
    public boolean checkForMedia() {
    	boolean result=false;
    	
        String mediastate = Environment.getExternalStorageState();
        
        if (Environment.MEDIA_MOUNTED.equals(mediastate)) {
        	result=true;
        }	
        return result;
    }
    public String getScanPath() {
    	if (checkForMedia()) {
    		   SharedPreferences nyalaPrefs = PreferenceManager.getDefaultSharedPreferences(libcontext);
    	       String scanSavePath = new String();
    		   scanSavePath=nyalaPrefs.getString("ScanPath", scanSavePath);
    		   return scanSavePath;
    	} else {
    		   return new String("failed");
    	}
    }
    public boolean createScanDir() {
    	
    	boolean success=false;
    	String saneSavePath= new String("");
    	if (checkForMedia()) {
    	   SharedPreferences nyalaPrefs = PreferenceManager.getDefaultSharedPreferences(libcontext);
    	   String sdPath = Environment.getExternalStorageDirectory().toString();
 	       String scanSavePath = new String();
 		   //guard against crappily-formed pathnames

 		   scanSavePath=nyalaPrefs.getString("ScanPath", scanSavePath);
 		   if (scanSavePath.contains("/mnt/sdcard")) {
 			   saneSavePath = new String(scanSavePath);
 		   } else {
 			       saneSavePath=new String(sdPath+scanSavePath);
 		   }
 		   
 		   Log.i("INFO","Attempting to create "+saneSavePath);
  		   File scanPath = new File(saneSavePath);
 		   if (!scanPath.exists()) {
 			   if (scanPath.mkdirs()) {
 				   success=true;
 				  Log.i("INFO","...SUCCESS!!");
 			   } else {
 				   success=false;
 				   Log.i("INFO","FAILED TO CREATE "+scanPath);
 			   
 			   }
 		   }
    	} else {
    		success=false;
    	}
    	return success;
    }
}