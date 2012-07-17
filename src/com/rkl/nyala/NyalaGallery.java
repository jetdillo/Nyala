package com.rkl.nyala;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;

import android.app.Activity;
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
import android.widget.ImageView;
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
		scanpath = sp.getString("ScanPath", scanpath);
	    gallerypath = new String(basepath+"/"+scanpath);
		
		Gallery scanGallery = (Gallery) findViewById(id.nygallery);
		 scanGallery.setAdapter(new ImageAdapter(NyalaGallery.this,gallerypath));
		
        scanGallery.setSpacing(2);
        
		scanGallery.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView parent, View v, int position, long id) {
	            Toast.makeText(NyalaGallery.this, "" + position, Toast.LENGTH_SHORT).show();
	        }
	    });		
	}
	
	public class ImageAdapter extends BaseAdapter {
	    int mGalleryItemBackground;
	    private Context iaContext;
        File gallerydir;
        File[] scanFile;
        FilenameFilter gfilter;
        Bitmap[] scanBitmap = null;
        private Integer[] mImageIds = {
	          //fill in from count of loaded files
	    };

	    public ImageAdapter(Context c, String gallerypath) {
	           iaContext = c;
	           gallerydir = new File(gallerypath);
	   		   gfilter = new FilenameFilter() {
	   			      public boolean accept(File f, String name) {
	   			    	  return !name.startsWith(".");
	   			      }
	   		   };
	   		
	   		   scanFile = gallerydir.listFiles(gfilter);
	   		   scanBitmap = new Bitmap[scanFile.length];
	   		   for (int i=0;i < scanFile.length;i++) {
	   			   Log.i("INFO","Bitmap name="+scanFile[i].toString());
	   		       scanBitmap[i] = BitmapFactory.decodeFile(scanFile[i].toString());
	   		   }
	    }

	    public int getCount() {
	        return mImageIds.length;
	    }

	    public Object getItem(int position) {
	        return position;
	    }

	    public long getItemId(int position) {
	        return position;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView imageView = new ImageView(iaContext);

	        imageView.setImageBitmap(scanBitmap[position]);
	        imageView.setLayoutParams(new Gallery.LayoutParams(320, 240));
	        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
	        
	        return imageView;
	    }
	}
}


