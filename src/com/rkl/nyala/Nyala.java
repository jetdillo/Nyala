/* Nyala. Keyboard-less Wifi authentication for your Android phone */
/* Written in Trieste, Italy under the influence of sun, prosciutto, prosecco and espresso. */


package com.rkl.nyala;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;


import java.util.ArrayList;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;

import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.provider.Settings;

import com.google.zxing.client.android.encode.*;
import com.google.zxing.common.*;

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
	private String qrcontents = new String("None");
	private String qrformat;
	public WifiStatusReceiver wsr;
	
	private int connectAction=0;
	private int scanAction=0;
	private int scantry=0;
	private SharedPreferences connectActionPrefs;
	private final String prefStr = new String("NyalaSettings");
	
	private static final int MENU_SHOWQR = Menu.FIRST; 
	private static final int MENU_SETTINGS = Menu.FIRST +1;
	private static final int MENU_ABOUT = Menu.FIRST+2;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) { 
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
      //  Button scanBtn = (Button) findViewById(R.id.scanBtn);
        
        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.nyala);
        iv.setAdjustViewBounds(true); 
        iv.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
       final WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
       
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
       
 	  } else {
 		 ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		 NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		 if (ni.isConnected()) {
 			String apname = new String(wm.getConnectionInfo().getSSID());
 			 Toast.makeText( Nyala.this, "Already connected to"+apname+", scanning another network will disconnect you", Toast.LENGTH_LONG ).show();
 		 }
 	  }	  	  
 	  
   }
   
    /* Creates the menu items  */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,MENU_SHOWQR, 0, "Show Barcode");
        menu.add(0,MENU_SETTINGS, 0, "Settings");
        menu.add(0,MENU_ABOUT,0,"About");
        
        return true;
    }
   
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent mIntent;
        String qrstring = new String(qrcontents);
        switch (item.getItemId()) {
        case MENU_SHOWQR:
        	mIntent = new Intent(Nyala.this,NyalaShare.class);
        	Log.i("INFO","Launching NyalaShare from Menu");
        	mIntent.putExtra("qrstr", qrstring);
        	startActivity(mIntent);
        	return true;
       
        case MENU_SETTINGS:
        	mIntent = new Intent(Nyala.this,NyalaSettings.class);
        	Log.i("INFO","Launching NyalaSettings from Menu");
        	startActivity(mIntent);
        	return true;
        	
        case MENU_ABOUT:
        	mIntent = new Intent(Nyala.this,NyalAbout.class);
        	startActivity(mIntent);
        	return true;
        }
        
        return false;
    } 
        
      
    @Override
    public void onStart() {
    	super.onStart();
    	
    	connectActionPrefs = getSharedPreferences(prefStr, MODE_PRIVATE);
    	connectAction=connectActionPrefs.getInt("stayRunning", 0);
    	scanAction=connectActionPrefs.getInt("autoConnect", 0);
    }

    @Override
    public void onResume() {
    	   super.onResume();
    	  
    	   connectActionPrefs = getSharedPreferences(prefStr, MODE_PRIVATE);
       	   connectAction=connectActionPrefs.getInt("stayRunning", connectAction);
       	   scanAction=connectActionPrefs.getInt("autoConnect", scanAction);
    }

    @Override
    public void onPause() {
    	super.onPause();
    }

    @Override
    public void onStop() {
    	super.onStop();
    	//unregisterReceiver(wsr);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver(wsr);
    }
    
    
   public void ScanBtnClickHandler(View v) {
	   
	         scantry=1;   
	   
	         Intent zxScanIntent = new Intent("com.google.zxing.client.android.SCAN");
	         zxScanIntent.setPackage("com.google.zxing.client.android");
	         zxScanIntent.putExtra("SCAN_MODE", "QR_CODE_MODE");
	         startActivityForResult(zxScanIntent, 0);            
   }
     
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	   
	    int erAP = 0;
	    SharedPreferences ConnectActionPrefs;
	    WifiConfiguration wc;
	    	                                  
	        if ( (requestCode == 0) && ((resultCode == RESULT_OK) ) ) {
	            qrcontents = new String(intent.getStringExtra("SCAN_RESULT"));
	            qrformat = new String(intent.getStringExtra("SCAN_RESULT_FORMAT"));
	            // Handle successful scan
	           Log.i("INFO","SCAN RESULTS: contents="+qrcontents+" format="+qrformat);
	           
	           //Toast.makeText( Nyala.this, "Scanned!", Toast.LENGTH_SHORT ).show();
	           
	           //WifiManager instance to control WiFi interface
	           final WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	           
	           wc = new WifiConfiguration();
	  
	          //Get the results of the most recent network scan. 
	          //This seems to happen automagically and regularly once Wifi is turned on, so we don't really need to initiate a scan ourselves. 
	          //We'll just feed off the most recent results. 	        
	          List<ScanResult> sl = wm.getScanResults();
	          Iterator isl = sl.iterator();
	          ScanResult sr = null ;
	          
	          while (isl.hasNext()) {
	        	    sr = (ScanResult) isl.next();
	        	    
	        	    //Prefer an SSID that identifies itself as part of EduRoam. 
	        	    //We'll get around to this eventually. 
	        	    
	        	    if (sr.SSID=="eduroam") {
	                    erAP=1;     	        
	 		            break;
	        	    }  
	          }
	           if (erAP == 1 ) {
	        	   
	        	   wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
	        	   wc.BSSID = new String(sr.BSSID);    
	        	   //somethingsomethingsomething
	           } else {
	        	   
	        	  if (qrcontents.contains("WIFI")) {
	        		 wc = ParseForZXingEncoding(qrcontents);
	        		 Log.i("INFO","GOT ZXing Encoded SSID:"+wc.SSID);
	        	  }
	        	  if (qrcontents.endsWith("::")) {
	        		  wc = ParseForNyalaEncoding(qrcontents);
	        		  Log.i("INFO","GOT Nyala Encoded SSID:"+wc.SSID);
	        	  } else {
	        		  AlertDialog.Builder ad = new AlertDialog.Builder(this);
	  		  	    ad.setMessage("Wifi Config Encoding not supported. Nyala will exit")
	  		  	         .setCancelable(false)
	  		  	         .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	  		  	             public void onClick(DialogInterface dialog, int id) {
	  		  	            	dialog.cancel();
	  		  	            	finish();
	  		  	             }
	  		  	         });
	        	  }

	        	   String ssid_str = wc.SSID;
	        	   String bssid_str = wc.BSSID;
	        	   String psk_str = wc.preSharedKey;
	        
	        	   Log.i("INFO","ssid_str="+ssid_str+" bssid_str="+bssid_str+" psk_str="+psk_str);
	        	
	        	   //We should give the user a chance to see what SSID they've scanned in before connecting
	        	   //This (will be) settable for those that just want to get on with things. 
	        	   Toast.makeText( Nyala.this, "Connecting...", Toast.LENGTH_SHORT ).show();
	        	   
	        	   //Management and ciphers were set during parsing  
	        	   //Call to do the actual connect
	        	   if (ssid_str.equals("invalid")) {
	        		   AlertDialog.Builder ad = new AlertDialog.Builder(this);
		  		  	    ad.setMessage("AccessPoint is invalid or unavailable ")
		  		  	         .setCancelable(false)
		  		  	         .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		  		  	             public void onClick(DialogInterface dialog, int id) {
		  		  	            	dialog.cancel();
		  		  	             }
		  		  	         });
	        	   } else {
	        	   ConnectFromScan(wc,wm);    
	        	   }
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

   private void ConnectFromScan(WifiConfiguration wc,WifiManager wm) {
	   
	   wc.hiddenSSID=true;
 	  
	   wc.priority = 1;
       wc.status = WifiConfiguration.Status.ENABLED;       
       int netId = wm.addNetwork(wc);
       //int netId = wm.getConnectionInfo().getNetworkId();
       
       //So we've got everything packed up this point. Let's lob it over the wall and see if it sticks.
       wm.enableNetwork(netId, true); 
   }
   
   
   private WifiConfiguration ParseForNyalaEncoding(String qrcontents) {
	   
	   //Take the results of the scanned text and parse it here, returning a WifiConfiguration object
	   // Nyala formatted QRCodes look like: 
	   // SSID::MAC::PSK:: <---note the terminating ::
	   //Like "MyWLAN::de:ad:be:ef:31:33:70::c0ffeeace1::"
	   // For an open network, leave the PSK off the end of the string or make it match [OoPpEeNn]"
	   
	   String theSSID =null;
	   String theType = null ;
	   String thePSK = new String("None");
	   String theBSSID = null; 
	   String theTrailer = null;
	   WifiConfiguration wc = null;
	   String nyala_delim = new String("::");
		  
		  int passpos=0;
		  int ssidpos=0;
		  int typepos=0;
		  int endpos=0;
		  int bssidpos=0;
		  int contentslen=0;
		 
		  int parse_score = 0;
		  int length=0;
	      boolean isvalid=false;
		  int delim=0;
		  int type=0;
		  int ssid_len=0;
		  int sepcount=0;
		  int sepcheck=0;
		   
		 contentslen = qrcontents.length(); 
		  //validate length
		 
		 Log.i("INFO","inside ParseForNyalaEncoding");
		
	     if ( contentslen >0) {
			 Log.i("INFO","contentslen="+contentslen);
			 //check for overly long string: SSID Max length + BSSID(if present) + 64 bytes PSK(max)+formatting=115 chars
			  if (contentslen <= 115) {
				 length=1;
				 
			  }
				  
		 } 
		
		 if (qrcontents.endsWith(nyala_delim)) {
			         delim=1;
				 if (!qrcontents.matches(";")) {
					 type=1;
				 }
				 Log.i("INFO","delim=1 type=1");
		 }
				 
				String [] qr_array =qrcontents.split("::");
				
				for (int i =0; i <=qr_array.length;i++) {
					Log.i("INFO","qr_array="+qr_array[i]);
				}
				 
				 if (qr_array[0].length() <=32 ) {
			           	ssid_len = 1;	 
			           	Log.i("INFO","SSID len OK");
				 }
				 for (char c : qrcontents.toCharArray()) {
		               if (c == ':')
		            	   sepcount++;
		           }
				 if (sepcount == 3) {
					   sepcheck=1;
				 }
				 
				 if (length+delim+type+ssid_len+sepcheck == 5) {
				
			       Log.i("INFO","Found a Nyala-format QRcode");
		       	   ssidpos=qrcontents.indexOf("::");
		           bssidpos = qrcontents.indexOf("::",ssidpos+1);
			       endpos=qrcontents.indexOf("::",bssidpos+1);
			   
			       theSSID= new String (qrcontents.substring(0,ssidpos));
			       theBSSID= new String(qrcontents.substring(ssidpos+2,bssidpos));
			       thePSK= new String (qrcontents.substring(bssidpos+2,endpos));
			       
			       wc = new WifiConfiguration();
				   wc.SSID = "\""+theSSID+"\"";
				   wc.BSSID = theBSSID;  
				   
				   if (thePSK.matches("[OoPpEeNn]") || thePSK.equals("None") || thePSK.length() == 0) {
					   wc.preSharedKey="\"None\"";
					   wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				   } else {
					   // Nyala encoding only allows for WPA/WPA2 PSKs
					   // 
					   wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	        		   wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
				       wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
				       wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
				       wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
				       wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
				        wc.preSharedKey="\""+thePSK+"\"";
				   }

			} else {
				    wc = new WifiConfiguration();
				    wc.SSID= new String("invalid");
				    Log.i("INFO","length="+length+" delim="+delim+" type="+type+" ssid_len="+ssid_len+" sepcount="+sepcount);
			}
				 
		return wc;
   }
   private WifiConfiguration ParseForZXingEncoding(String qrcontents) {
	   
	   String theSSID =null;
	   String theType = null ;
	   String thePSK = null;
	   String theBSSID = null; 
	   String theTrailer = null;
	   WifiConfiguration wc = null;
	   String zxing_delim = new String(";");
		  
	   int passpos=0;
	   int ssidpos=0;
	   int typepos=0;
	   int endpos=0;
	   int bssidpos=0;
	   int contentslen=0;
		 
	   int parse_score = 0;
	   int length=0;
	   int delim=0;
       int type=0;
	   int ssid_len=0;
	   int sepcheck=0;
	   
	   contentslen = qrcontents.length(); 
		  //validate length
	     if ( ( contentslen >0) && (contentslen <= 115) ) {
			 
			 //check for overly long string: SSID Max length + BSSID(if present) + 64 bytes PSK(max)+formatting=115 chars
	 		 length=1;			  
		 } 
		 
		 if ( (qrcontents.startsWith("WIFI")) && (qrcontents.contains(":T:")) && (qrcontents.contains(";S:")) && (qrcontents.contains(";P:")) && (qrcontents.endsWith(";;")) ){
	     //Valid ZXing-formatted WLAN string
			Log.i("INFO","Found a ZXing-format QRcode");
			delim=1;
		    ssidpos=(qrcontents.indexOf(";S:"));
		    passpos=(qrcontents.indexOf(";P:"));
		    endpos=(qrcontents.indexOf(";;"));
		 }                                                                                                                                                                                                            
		    String WlanAuthType=qrcontents.substring(7,ssidpos);
		    Log.i("INFO","WlanAuthType="+WlanAuthType);
		    //Sorry, not supporting your WEP-using ass. 
		    //You must place your WEP-based AP in the Emergency Artificial Intelligence Incinerator labeled "2002" before moving into the next decade
		    if (WlanAuthType.equals("WEP") ) {
		    	type=0;
		    	AlertDialog.Builder ad = new AlertDialog.Builder(this);
		  	    ad.setMessage("WEP access not supported")
		  	         .setCancelable(false)
		  	         .setPositiveButton("I have been appropriately chastised", new DialogInterface.OnClickListener() {
		  	             public void onClick(DialogInterface dialog, int id) {
		  	            	dialog.cancel();
		  	             }
		  	         });
		  	         
		  	    AlertDialog WiFi_AD = ad.create();
		                    WiFi_AD.show();
		    }
		    if (WlanAuthType.equals("WPA")) {
	           thePSK = new String (qrcontents.substring(passpos+3,endpos));	  
	           theSSID = new String(qrcontents.substring(ssidpos+3, passpos));
	           Log.i("INFO","Authtype=WPA thePSK="+thePSK+" theSSID="+theSSID);
	           type=1;
	           if (theSSID.length() <=32) {
	        	   ssid_len=1;
	        	   Log.i("INFO","theSSID length="+theSSID.length());
		       }
	           //Some rudimentary anti-fuzzing validation 
	           //Checks to make sure that somebody isn't trying to slip us an empty field or an SSID made up of all ;s or all :s
	          
	           int sepcount1=0;
	           int sepcount2=0;
	           for (char c : qrcontents.toCharArray()) {
	               if (c == ':')
	                   sepcount1++;
	               if (c == ';')
	            	   sepcount2++;
	           }
                	           
	           Log.i("INFO","sepcount1="+sepcount1+" sepcount2="+sepcount2);
	           
	           if ( (sepcount1 == 4) && ( sepcount2 == 4)) {
	        	   sepcheck=1;
	           }	   
	        }
	        // if all the parsing checks have passed, create a new WifiConfiguration object to pass back to the ActivityResult handler 
		  
		    if (length+type+ssid_len+delim+sepcheck == 5) {
		    	wc = new WifiConfiguration();
		    	if ( (thePSK.length() == 0) || (thePSK.matches("[OoPpEeNn]")) ) {
			    	   wc.preSharedKey= new String("None");
			    	   wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			       } else {
			               wc.preSharedKey="\""+thePSK+"\"";
			               wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			     		   wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
						   wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
						   wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
						   wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
						   wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
						   wc.SSID = "\""+theSSID+"\"";
			       }
		    } else {
			    wc = new WifiConfiguration();
			    wc.SSID= new String("invalid");
			    Log.i("INFO","length="+length+" delim="+delim+" type="+type+" ssid_len="+ssid_len+" sepcheck="+sepcheck);

		}
		    return wc;
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
				   if (connectAction == 1)  {
					   if (scantry > 0) {
				           finish();
					   } 
				   }  
			   }
			   
			   if (wsr_ni.getState() == State.CONNECTING) {
				   Toast.makeText(Nyala.this, "Connecting...", Toast.LENGTH_SHORT).show();
			   } 
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