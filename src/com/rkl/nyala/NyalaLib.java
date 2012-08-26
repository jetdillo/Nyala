package com.rkl.nyala;
import android.app.AlertDialog;
import android.os.Environment;

import android.content.Context;
import android.content.DialogInterface;

import android.content.SharedPreferences;
import android.util.Log;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.preference.PreferenceManager;
import android.provider.MediaStore;

import java.io.*;
import java.util.Arrays;

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
	       if ( bm.compress(Bitmap.CompressFormat.PNG, 80,imgFOS)) { 
               imgFOS.flush();    
               imgFOS.close();
	           result=true;
	       } else {
	    	    imgFOS.close();
	    	    result=false;
	       }
	         
	   } catch (IOException ioe) {
		   result=false;
	   }
	   return result;
   }
   
   public boolean readScanFromStorage(String scanfile, Context c) {
	   boolean result = false;
	   Bitmap bm;
	   try {
		   FileInputStream imgFIS = c.openFileInput(scanfile);
		   bm = BitmapFactory.decodeStream(imgFIS, null,null);
		   result = true;
		    
	   } catch (FileNotFoundException fnf) {
		   Log.e("ERROR", "Failed to read "+scanfile+" because "+fnf);
		   result=false;
	   }
	return result;   
   }
   
   
   public boolean saveScanToSD(Bitmap bm,String ssidstr,Context c) {
       String scanSavePath = new String();
       String scanImageFile = new String();
	   boolean result=false;
	   File imgfile;
	   scanSavePath = new String(getScanDir());
	   scanImageFile = new String("nyala_"+ssidstr+".png"); 
	   
	  String sdPath = Environment.getExternalStorageDirectory().toString();
     
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
    	
    	 String nystorageDir = null;
    	
    	 SharedPreferences nyalaPrefs = PreferenceManager.getDefaultSharedPreferences(libcontext);
    	 String storageLoc = new String(nyalaPrefs.getString("SaveLoc","Internal"));
    	 if (storageLoc.equals("Internal")) {
    		 nystorageDir = new String(Environment.getDataDirectory().toString());
    		 
    	 } else {
    		 
    	   if (checkForMedia()) {
    		  nystorageDir = new String(Environment.getExternalStorageDirectory().toString());
    	   } else {
    		   nystorageDir = new String("Failed");
    	   }
    	 }
    	 Log.i("INFO","NYSTORAGEDIR="+nystorageDir);
    	 return nystorageDir;
}
    
    public String getLastScanFileName() {
      
    
      String scandir_str= new String(getScanDir());
      File scandir = new File(scandir_str);
	  FilenameFilter nyfilter = FilterFactory("nyala_");  
	  String latest_scan = null; 
	  long[] scan_mtime;
	  long last_mtime = 0;
	  File[] scanlist;
	    
      if ( !(scandir_str.equals("failed")) ) {
    	 scanlist = scandir.listFiles(nyfilter);
    	  
         if (scanlist != null) {
    	  
    	  if (scanlist.length >0 ) {
    	  
    	      scan_mtime = new long[scanlist.length];
    	      int pos=0;
    	      for (File scan_f : scandir.listFiles(nyfilter)) {
    		      scan_mtime[pos]=scan_f.lastModified();
    		      pos++;
    	      }
    	      Arrays.sort(scan_mtime);
    	      last_mtime=scan_mtime[(scan_mtime.length)-1];
    	  
    	      for (File scan_f : scandir.listFiles(nyfilter)) {
    	    	  Log.i("INFO","scan_f="+scan_f.toString());
    		       if ( (scan_f.lastModified()) == last_mtime) {
    			       latest_scan = new String(scan_f.toString());
    			       break;
    		       }
    	      }
    	  } else {
    		      latest_scan = new String("None");
    	  }
      } else {
    	      latest_scan = new String("None");
      }
     }
      
     Log.i("INFO","getLastScanFile: latest_scan="+latest_scan);
      
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