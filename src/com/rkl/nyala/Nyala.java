/* Nyala. Keyboard-less Wifi authentication for your Android phone */
/* Written in Trieste, Italy under the influence sun, prosciutto, prosecco and espresso. */


package com.rkl.nyala;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;

import android.app.Activity;
import android.os.Bundle;
import java.util.ArrayList;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;

import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.provider.Settings;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Gallery.LayoutParams;

public class Nyala extends Activity {
	private Context wnContext;	
	private WifiConfiguration wc;
	private String qrcontents;
	private String qrformat;
	public WifiStatusReceiver wsr;
	private String NySSID;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) { 
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button scanBtn = (Button) findViewById(R.id.scanBtn);
        
        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.nyala);
        iv.setAdjustViewBounds(true); 
        iv.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
        
        
       final WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
       wsr = new WifiStatusReceiver();
       
       IntentFilter wifilter = new IntentFilter();
       wifilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

       wsr = new WifiStatusReceiver();
       registerReceiver(wsr, wifilter);
     
 	  if (!(wm.isWifiEnabled())) {
 	  AlertDialog.Builder ad = new AlertDialog.Builder(this);
	  ad.setMessage("Wireless is currently disabled. Click 'Enable' to turn on WiFi")
	         .setCancelable(false)
	         .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
	             public void onClick(DialogInterface dialog, int id) {
	            	 Toast.makeText( Nyala.this, "Turning on WiFi", Toast.LENGTH_SHORT ).show();
                	 wm.setWifiEnabled(true);
	             }
	         })                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
	         .setNegativeButton("Exit Nyala", new DialogInterface.OnClickListener() {
	             public void onClick(DialogInterface dialog, int id) {
	                  dialog.cancel();
	                  finish();
	             }
	         });
	  AlertDialog WiFi_AD = ad.create();
                  WiFi_AD.show();
       
 	  }
 	  
   }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    }

    @Override
    public void onResume() {
    	   super.onResume();
    	  
    }

    @Override
    public void onPause() {
    	super.onPause();
    }

    @Override
    public void onStop() {
    	super.onStop();
    	unregisterReceiver(wsr);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver(wsr);
    }
    
    
   public void ScanBtnClickHandler(View v) {
	         Intent zxScanIntent = new Intent("com.google.zxing.client.android.SCAN");
	         zxScanIntent.setPackage("com.google.zxing.client.android");
	         zxScanIntent.putExtra("SCAN_MODE", "QR_CODE_MODE");
	         startActivityForResult(zxScanIntent, 0);
   }
     
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	   
	    int erAP = 0;
	                                  
	        if ( (requestCode == 0) && ((resultCode == RESULT_OK) ) ) {
	            qrcontents = new String(intent.getStringExtra("SCAN_RESULT"));
	            qrformat = new String(intent.getStringExtra("SCAN_RESULT_FORMAT"));
	            // Handle successful scan
	           Log.i("INFO","SCAN RESULTS: contents="+qrcontents+" format="+qrformat);
	           
	           Toast.makeText( Nyala.this, "Scanned!", Toast.LENGTH_SHORT ).show();
	           
	           //WifiManager instance to control WiFi interface
	           WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	           
	           wc = new WifiConfiguration();
	  
	          //Get the results of the most recent network scan. 
	          //This seems to happen automagically, so we don't really need to ask for one ourselves
	          //We'll just feed off the most recent results. 	        
	          List<ScanResult> sl = wm.getScanResults();
	          Iterator isl = sl.iterator();
	          ScanResult sr = null ;
	          
	          while (isl.hasNext()) {
	        	    sr = (ScanResult) isl.next();
	        	    
	        	    //Prefer an SSID that identifies itself as part of EduRoam. 
	        	    //This will eventually be settable 
	        	    
	        	    if (sr.SSID=="eduroam") {
	                    erAP=1;     	        
	 		            break;
	        	    }  
	          }
	           if (erAP == 1 ) {
	        	   
	        	   wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
	        	   wc.BSSID = new String(sr.BSSID);                                                                
	           } else {
	        	   // PSK format is [SSID:BSSID:Keytype:PSK]
	        	   int ssid_delim = qrcontents.indexOf("::");
	        	   int bssid_delim = qrcontents.indexOf("::",ssid_delim+1);
	        	   int psk_delim = qrcontents.indexOf("::",bssid_delim+1);
	        	   
	        	   Log.i("INFO","ssid_delim="+ssid_delim+" bssid_delim="+bssid_delim+" psk_delim="+psk_delim);
	        	   
	        	   String ssid_str = new String(qrcontents.substring(0,ssid_delim));
	        	   String bssid_str = new String(qrcontents.substring(ssid_delim+2,bssid_delim));
	        	   String psk_str = new String(qrcontents.substring(bssid_delim+2));
	        	   
	        	   Log.i("INFO","ssid_str="+ssid_str+" bssid_str="+bssid_str+" psk_str="+psk_str);
	        	   
	        	   wc.SSID = "\""+ssid_str+"\"";
	        	   wc.BSSID = bssid_str;
	        	   
	        	   NySSID = new String(ssid_str);
	        	   
	        	   if (psk_str.equals("None")) {
	        		   wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	        	   }
	        	   else {
	        		   wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	        		   wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
				       wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
				       wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
				       wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
				       wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	        		   wc.preSharedKey="\""+psk_str+"\"";
	        	   }
	        	   wc.hiddenSSID=true;
	        	   
	        	   wc.priority = 1;
			       wc.status = WifiConfiguration.Status.ENABLED;       
			       int netId = wm.addNetwork(wc);
			    
			       wm.enableNetwork(netId, true); 	
			       
	           }
	           	
	           
	        } else if (resultCode == RESULT_CANCELED) {
	        	AlertDialog.Builder ad = new AlertDialog.Builder(this);
	      	  ad.setMessage("Cancelling scan")
	      	         .setCancelable(false)
	      	         .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	      	             public void onClick(DialogInterface dialog, int id) {
	      	            	 Toast.makeText( Nyala.this, "You will need to scan the code to gain access to the network", Toast.LENGTH_SHORT ).show();
	      	             }
	      	         });
	      	  AlertDialog AD = ad.create();
	                      AD.show();   
	        }
	    
	    
	}
   
   public class WifiStatusReceiver extends BroadcastReceiver {
	   
	   @Override
	   public void onReceive(Context context, Intent intent) {
		   
		   Log.i("INFO","******Reached WifiStatusReceiver with "+intent.getAction().toString());
		 
		   
		   if (intent.getAction().toString().equals("android.net.wifi.STATE_CHANGE")) {
			   
			   Log.i("INFO","*****INSIDE wifi.STATE_CHANGE*****");
			   
			   NetworkInfo wsr_ni = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			    
			   if (wsr_ni.getState() == State.CONNECTED) {
				   Toast.makeText( Nyala.this, "Wifi now connected...", Toast.LENGTH_LONG ).show();
				   finish();
			   }
			   if (wsr_ni.getState() == State.CONNECTING) {
				   Toast.makeText(Nyala.this, "Connecting...", Toast.LENGTH_SHORT).show();
			   } else {
				       if (wsr_ni.getDetailedState() == DetailedState.FAILED) {
				    	   if (! (wsr_ni.getReason() == null) ) {
				    	       String rsn_str = new String (wsr_ni.getReason());
				    	       Toast.makeText(Nyala.this,"Wifi Connection failed because "+rsn_str+". Try again in a moment", Toast.LENGTH_LONG).show();
				    	   } else {
				    		    Toast.makeText(Nyala.this, "Wifi Connection failed for an unknown reason", Toast.LENGTH_LONG).show();
				    	   }
				       }
			   }              
		  }
		   
	   }

   }
   
   
    
}  