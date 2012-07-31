package com.rkl.nyala;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Environment;

import android.content.Context;
import android.content.DialogInterface;
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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NyalaLib {
	
	Context libcontext;

public NyalaLib(Context c) {
	libcontext = c;
}
	
   public boolean saveScan(Bitmap bm,String ssidstr, Context c) {
	   SharedPreferences nyalaPrefs = PreferenceManager.getDefaultSharedPreferences(libcontext);
	   String saveLoc = new String();
	   String scanImageFile = new String();
	   boolean result=false;
	  
	   saveLoc = new String(nyalaPrefs.getString("ScanLoc", "Internal"));
	   
	   if (saveLoc.contains("Internal")) {
		  result=saveScanToStorage(bm,ssidstr,libcontext);
	   }
	   if (saveLoc.contains("External")) {
		   result=saveScanToSD(bm,ssidstr,libcontext);
	   }
	  return result;
	   
   }
   
   public boolean saveScanToStorage(Bitmap bm,String ssidstr,Context c) {
	  
	   boolean result = false;
	   
	   String imgfile = new String("nyala_"+ssidstr+".png");
	   try {
	         FileOutputStream imgFOS = c.openFileOutput(imgfile,Context.MODE_PRIVATE); 
	         result=true;
	         
	   } catch (IOException ioe) {
		   result=false;
	   }
	   return result;
   }
   
   public boolean saveScanToSD(Bitmap bm,String ssidstr,Context c) {
	   SharedPreferences nyalaPrefs = PreferenceManager.getDefaultSharedPreferences(c);
       String scanSavePath = new String();
       String scanImageFile = new String();
	   boolean result=false;
	   File imgdir;
	   File imgfile;
	   scanSavePath = new String(getScanDir());
	   scanImageFile = new String("nyala_"+ssidstr+".png"); 
	   
	  String sdPath = Environment.getExternalStorageDirectory().toString();
      String storagePath = sdPath+"/"+ssidstr; 
     
	  	 OutputStream ostream = null;
		 imgfile = new File(sdPath, scanImageFile);
	  	
	  	    try {
	  	    	   
	  	       ostream = new FileOutputStream(imgfile);
	  	       bm.compress(Bitmap.CompressFormat.PNG, 100, ostream);
	  	        //This is kind of lazy and needs to be handled better
	  	       ostream.flush();
	  	       ostream.close();
	  	       result=true;
	  	   	  	     
	  	     } catch(IOException ioe) {
	  	    	Log.e("ERROR","Could not save "+imgfile+" to "+scanSavePath);
	  	    	result=false;
	  	     }
	  	    try {
		      	MediaStore.Images.Media.insertImage(c.getContentResolver(),imgfile.getAbsolutePath(),imgfile.getName(),imgfile.getName());
		      	
		      	
	  	    } catch (FileNotFoundException fnf) {
	  	    	Log.e("ERROR","MediaStore insertImage error trying to save "+imgfile.getName()+" to "+imgfile.getAbsolutePath());
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
    public String getScanDir() {
    	if (checkForMedia()) {
    		   SharedPreferences nyalaPrefs = PreferenceManager.getDefaultSharedPreferences(libcontext);
    	       String scanSavePath = new String();
    		   scanSavePath=nyalaPrefs.getString("ScanPath", scanSavePath);
    		   return scanSavePath;
    	} else {
    		   return new String("failed");
    	}
    }
    
    public String getLastScanFile() {
      
      
      String scandir_str= new String(getScanDir());
      File scandir = new File(scandir_str);
	  FilenameFilter nyfilter = FilterFactory("nyala_");  
	  String latest_scan = null; 
	  long[] scan_mtime;
	  long last_mtime = 0;
	    
	  int pos=0;
      if ( (!(scandir_str.equals("failed"))) && (nyfilter!=null) ) {
    	  int numfiles = scandir.listFiles(nyfilter).length;
    	  scan_mtime = new long[numfiles];
    	  for (File scan_f : scandir.listFiles(nyfilter)) {
    		  scan_mtime[pos]=scan_f.lastModified();
    		  pos++;
    	  }
    	  Arrays.sort(scan_mtime);
    	  last_mtime=scan_mtime[(scan_mtime.length)-1];
    	  
    	  for (File scan_f : scandir.listFiles(nyfilter)) {
    		   if ( (scan_f.lastModified()) == last_mtime) {
    			   latest_scan = new String(scan_f.toString());
    			   break;
    		   }
    	  }
    	  
      } else {
    	      latest_scan = new String("None");
      }
      
      Log.i("INFO","getLastScanFile: latest_scan="+latest_scan+"");
      
      return latest_scan;      
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
 			   scanPath.setWritable(true);
 			   if (scanPath.mkdirs()) {
 				   success=true;
 				  Log.i("INFO","...SUCCESS!!");
 			   } else {
 				   success=false;
 				   Log.i("INFO","FAILED TO CREATE "+scanPath);
 			   
 			   }
 		   } else {
 			   Log.i("INFO",scanPath+" already exists");
 			   success=true;
 		   }
    	} else {
    		success=false;
    	}
    	return success;
    }
    
    public boolean purgeScanDir() {
    	
    	String sp = new String();
    	sp = new String(getScanDir());
    	File purgeD = new File(sp);
    	boolean results=false;
    	
		FilenameFilter purgeFilter = new FilenameFilter() {
			      public boolean accept(File f, String name) {
			    	  return name.startsWith("nyala_");
			      }
		}; 
    	
    	for (File pf : purgeD.listFiles(purgeFilter)) {
    		 if (!pf.delete()) {
    			 Log.e("ERROR","Failed to delete "+pf);
    			 results=false;
    		 }
    	}
    	return results;
    }
    
    public AlertDialog SimpleDialogFactory(String msgStr, String posBtn, String negBtn, Context c) {
    	AlertDialog.Builder ad = new AlertDialog.Builder(c);
    	 
		  ad.setMessage(msgStr)
		         .setCancelable(false)
		         .setPositiveButton(posBtn, new DialogInterface.OnClickListener() {
		             public void onClick(DialogInterface dialog, int id) {
		            	dialog.cancel();
		            			             }
		         })                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
		         .setNegativeButton(negBtn, new DialogInterface.OnClickListener() {
		             public void onClick(DialogInterface dialog, int id) {
		                  dialog.cancel();
		                  
		             }
		         });
		return ad.create();
    }
    
    public AlertDialog SimpleDialogFactory(String msgStr, String posBtn, Context c) {
    	AlertDialog.Builder ad = new AlertDialog.Builder(c);
    	 
		  ad.setMessage(msgStr)
		         .setCancelable(false)
		         .setPositiveButton(posBtn, new DialogInterface.OnClickListener() {
		             public void onClick(DialogInterface dialog, int id) {
		            	dialog.cancel();
		            			             }
		       
		         });
		return ad.create();
    }
    
    public FilenameFilter FilterFactory(String filterspec) {
    	
    	final String filter_str = new String(filterspec);
    	
    	FilenameFilter ff = new FilenameFilter() {
		      public boolean accept(File f, String name) {
		    	 
		    	  return name.startsWith(filter_str);
		      }
	    };
	    return ff;
    }
}