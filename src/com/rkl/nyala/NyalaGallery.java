package com.rkl.nyala;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.preference.PreferenceManager;
import android.provider.Settings;

import com.rkl.nyala.NyalaLib;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.R;
import com.google.zxing.client.android.R.id;
import com.google.zxing.client.android.encode.*;
import com.google.zxing.common.*;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Gallery.LayoutParams;


public class NyalaGallery extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nyalagallery);
		
		String scanpath= new String("");
		String gallerypath=new String("");
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String basepath = new String (Environment.getExternalStorageDirectory().toString());
		scanpath = sp.getString("SaveLoc", scanpath);
		
		// If scanpath is "Internal" load in images from Internal App Storage
		// If scanpath is "External" load in images from SDCard
		
	    gallerypath = new String(basepath+"/"+scanpath);
		
	    ImageAdapter gImageAdapter = new ImageAdapter(NyalaGallery.this,gallerypath);
	    
		Gallery scanGallery = (Gallery) findViewById(id.nygallery);
		//scanGallery.setAdapter(new ImageAdapter(NyalaGallery.this,gallerypath));
		scanGallery.setAdapter(gImageAdapter);
		
        scanGallery.setSpacing(2);
        
        
		scanGallery.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView parent, View v, int position, long id) {
	            Toast.makeText(NyalaGallery.this, "" + position, Toast.LENGTH_SHORT).show();
	            
	            Intent toShareIntent = new Intent(NyalaGallery.this,NyalaShare.class);
	            
	            startActivity(toShareIntent);
	        }
	    });		
	}

	public class ImageAdapter extends BaseAdapter {
	    int mGalleryItemBackground;
	    private Context iaContext;
        File gallerydir;
        File[] scanFile;
        File noentry=null;
        FilenameFilter galleryfilter;
        Bitmap[] scanBitmap = null;
        String[] scanBitmapName;
        String gallerypath = new String();
        GalleryLoader gl = new GalleryLoader(NyalaGallery.this);
        
	    public ImageAdapter(Context c, String dir) {
	    		
	    	   iaContext = c;
	    	
	           gallerypath = new String(dir);
	           gallerydir = new File(gallerypath);
	           if (gallerypath.contains("sdcard")) {
	        	 scanBitmap = gl.loadFromExternal();
	        	 scanBitmapName = gl.getGalleryFilenames();
	              	 
	           } else {
	        	       scanBitmap = gl.loadFromInternal();
	        	       scanBitmapName = gl.getGalleryFilenames();
	           }
	           
	      	
	    }

	    public int getCount() {
	        return  scanBitmap.length;
	    }

	    public Object getItem(int position) {
	        return position;
	    }

	    public long getItemId(int position) {
	        return position;
	    }
	    
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	
	    	LinearLayout llv = new LinearLayout(NyalaGallery.this);
	    	llv.setOrientation(LinearLayout.VERTICAL);
	    	
	    	ImageView iv = new ImageView(iaContext);
	        iv.setImageBitmap(scanBitmap[position]);
	        iv.setScaleType(ImageView.ScaleType.FIT_XY);
	        

	        String sbnpath = new String(scanBitmapName[position]);
           
	        TextView tv = new TextView(iaContext);
	        
	        tv.setText(sbnpath.substring((sbnpath.lastIndexOf("nyala_"))+6,(sbnpath.lastIndexOf("."))));
	        tv.setTextSize((float) 24.00);
	        llv.addView(iv);
	        llv.addView(tv);
	    
	        //return iv;
	        return llv;
	    }
	    
	}
	
	public class GalleryLoader {
		
		Context gc;
		String[] galleryFileNames;
		public GalleryLoader (Context c) {
			gc = c;
		}
		
		public String[] getGalleryFilenames() {
			return galleryFileNames;
			
		}
		
		private String[] convertArrays(File[] farray) {
			
			int numfiles = farray.length;
			
			String[] names_str= new String[numfiles];
			for (int i=0;i < numfiles;i++) {
				names_str[i]=farray[i].toString();
			}
			return names_str;
		}
		
		
		public Bitmap[] loadFromInternal() {
			File gp;
			Bitmap[] bitmap_arr;
			String[] bitMapNameStr = gc.fileList();
			
			int numbitmaps = gc.fileList().length;
			
			bitmap_arr = new Bitmap[numbitmaps];
		    gp = gc.getFilesDir();
		    
		    for (int i=0;i < numbitmaps;i++) {
		    	 String fn = gp.getAbsolutePath()+bitMapNameStr[i];
		    	 bitmap_arr[i] = BitmapFactory.decodeFile(fn);
		    }
		    
		    galleryFileNames = gc.fileList();
		    
		    return bitmap_arr;
		}
		
		public Bitmap[] loadFromExternal() {
		
			String[] bitMapNameStr = gc.fileList();
			Bitmap[] bitmap_arr = new Bitmap[bitMapNameStr.length];
			File[] imagefile = new File[bitMapNameStr.length];
			File gp;
			
			NyalaLib nl = new NyalaLib(NyalaGallery.this);
			
			String externpath = new String(Environment.getExternalStorageDirectory().toString());
			gp = new File(externpath);
			
			FilenameFilter imageFileFilter = new FilenameFilter() {
				      public boolean accept(File f, String name) {
				    	  return name.startsWith("nyala_");
				      }
			};
			if (nl.checkForMedia()) {
			    imagefile = gp.listFiles(imageFileFilter);
			    bitmap_arr = new Bitmap[imagefile.length];
			    for (int i=0; i< imagefile.length;i++) {
			    	bitmap_arr[i] = BitmapFactory.decodeFile(imagefile[i].toString());
			    }
	        } else {
	        	                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
	        }
			galleryFileNames = convertArrays(imagefile);
	   return bitmap_arr;
	}
		
	
	}

	 private void dialogFactory(String msgstr,String posbtn, String negbtn,Context c) {
	    	
			AlertDialog.Builder ad = new AlertDialog.Builder(c);
			  ad.setMessage(msgstr)
			         .setCancelable(false)
			         .setPositiveButton(posbtn, new DialogInterface.OnClickListener() {
			             public void onClick(DialogInterface dialog, int id) {
			            	dialog.cancel();
			            	finish();
			             }
			         })                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
			         .setNegativeButton(negbtn, new DialogInterface.OnClickListener() {
			             public void onClick(DialogInterface dialog, int id) {
			                  dialog.cancel();
			                  finish();
			             }
			         });
			AlertDialog Share_AD = ad.create();
		                Share_AD.show();
		    }
	 
	private void dialogFactory(String msgstr, String posbtn, Context c) {
		AlertDialog.Builder ad = new AlertDialog.Builder(c);
		  ad.setMessage(msgstr)
		         .setCancelable(false)
		         .setPositiveButton(posbtn, new DialogInterface.OnClickListener() {
		             public void onClick(DialogInterface dialog, int id) {
		            	dialog.cancel();
		            	finish();
		             }
		         });
		AlertDialog Share_AD = ad.create();
	                Share_AD.show();
	}
	
   }
	



