package com.rkl.nyala;

import java.io.File;
import java.io.FilenameFilter;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.AlertDialog;


import com.rkl.nyala.NyalaLib;

public class NyalaHistory extends ListActivity {
	
	public void onCreate(Bundle savedInstanceState) {
		
		String[] histFilesStr =new String[1];
		int histlength=0;
	       super.onCreate(savedInstanceState);
	       setContentView(R.layout.nyalahistory);
		
		String scanpath= new String("");
		String listpath=new String("");
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String basepath = new String (Environment.getExternalStorageDirectory().toString());
		scanpath = sp.getString("SaveLoc", scanpath);
		
		ListViewLoader lvl = new ListViewLoader(NyalaHistory.this);
		
		NyalaLib nl = new NyalaLib(NyalaHistory.this);
		
		// If scanpath is "Internal" load in images from Internal App Storage
		if (scanpath.contains("Internal")) {
			histlength = lvl.internalLoader();
		}
		
		// If scanpath is "External" load in images from SDCard
		
		if (scanpath.contains("External")) {
			histlength = lvl.externalLoader();
		}
		
		if (histlength >0 ) {
		    histFilesStr = new String[histlength];
		    histFilesStr = lvl.getHistoryFilenames();
	     	ArrayAdapter<String> histArray = new ArrayAdapter<String>(this,R.layout.history_item,histFilesStr);
	    	setListAdapter(histArray);
		} else {
		  AlertDialog ad= nl.SimpleDialogFactory("No Barcodes in history", "OK", NyalaHistory.this);	
		  ad.show();
		  
		}
	   // listpath = new String(basepath+"/"+scanpath);
		    
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
	
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
	}
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		
	}
	
    private class ListViewLoader {
		String[] historyFileNames;
	    Context gc;
	    NyalaLib nl;
	   
		public ListViewLoader (Context c) {
			gc = c;
		    nl = new NyalaLib(NyalaHistory.this);
		}
		
		public String[] getHistoryFilenames() {
			return historyFileNames;
			
		}
		
		
		private String[] convertArrays(File[] farray) {
			
			int numfiles = farray.length;
			
			String[] names_str= new String[numfiles];
			for (int i=0;i < numfiles;i++) {
				names_str[i]=farray[i].toString();
			}
			return names_str;
		}
		
		
		public int internalLoader() {
			File hf;
			
			String[] histFilesStr = gc.fileList();

		    historyFileNames = gc.fileList();
		
			return histFilesStr.length;
		}
		
		public int externalLoader() {
		
			String[] histFilesStr = gc.fileList();
			
			File[] histFiles= new File[histFilesStr.length];
			File hf;
			
			String externpath = new String(Environment.getExternalStorageDirectory().toString());
			hf = new File(externpath);
			
			FilenameFilter histFileFilter = new FilenameFilter() {
				      public boolean accept(File f, String name) {
				    	  return name.startsWith("nyala_");
				      }
			};
			
			if (nl.checkForMedia()) {
			    histFiles = hf.listFiles(histFileFilter);
	        } 
			
			historyFileNames = convertArrays(histFiles);
			String[] historyFilesStr = historyFileNames;
			return histFiles.length;
	  
	    }
    }		
	
}