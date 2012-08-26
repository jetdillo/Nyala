/* Nyala. Keyboard-less Wifi authentication for your Android phone */
/* Written in Trieste, Italy under the influence of sun, prosciutto, prosecco and espresso. */

package com.rkl.nyala;

import java.io.FileOutputStream;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;

import android.app.AlertDialog;

import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;

import android.preference.PreferenceManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.R.id;

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

public class Nyala extends Activity  {
	private String qrcontents = new String("None");
	private String qrformat;
	public WifiStatusReceiver wsr;
	
	private boolean connectAction=false;
	private boolean scanAction=false;
	private boolean saveScanAction=false;
	private int scantry=0;
	private boolean confirmConnect=false;
	private SharedPreferences nyalaPrefs;
	private final String prefStr = new String("NyalaSettings");
	private String scanSavePath = new String("nyala/scans");
	private NyalaLib nl = new NyalaLib(Nyala.this);
	
	
	private static final int MENU_SHOWQR = Menu.FIRST; 
	private static final int MENU_SHOWSCANS = Menu.FIRST +1;
	private static final int MENU_SETTINGS = Menu.FIRST +2;
	private static final int MENU_ABOUT = Menu.FIRST+3;
	private static final int MENU_HELP = Menu.FIRST+4;
	
    @Override
    public void onCreate(Bundle savedInstanceState) { 
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.nyala);
        iv.setAdjustViewBounds(true); 
        iv.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
        /*Hide SSID, channel, signal strength, etc. from users until we're actually connected to something */
        
        TextView ssidLabel  = (TextView) findViewById(id.ssidLbl);
        TextView channelLabel = (TextView) findViewById(id.channelLbl);
        TextView signalLabel = (TextView) findViewById(id.signalLbl);
 
   }
   
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,MENU_SHOWQR, 0, "Share Last");
        menu.add(0,MENU_SHOWSCANS,0, "Show Scan History");
        menu.add(0,MENU_SETTINGS, 0, "Settings");
        menu.add(0,MENU_ABOUT,0,"About");
        menu.add(0,MENU_HELP,0,"Help");
        
        return true;
    }
   
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent mIntent;
        String qrstring = new String(qrcontents);
        String qrssid =  getSSIDFromQRStr(qrcontents);
    	mIntent = new Intent(Nyala.this,NyalaShare.class);
    	
    	String nyshareFile = new String(nl.getLastScanFileName()); 
    	
        switch (item.getItemId()) {
        case MENU_SHOWQR:
        	Log.i("INFO","Nyala: qrstring="+qrstring);
              
        	if ( (qrstring.equals("None")) && (nyshareFile.equals("None")) ) {
        	 	AlertDialog ad = nl.SimpleDialogFactory("No Recent Scan", "OK", Nyala.this);
        		ad.show();
        	} else {
        	
        	     if (!(qrstring.equals("None")) ) {
                  	mIntent.putExtra("qrstr", qrstring);
        	        mIntent.putExtra("qrssid", qrssid);
        	     } else {
      		         	nyshareFile = new String("file:"+nyshareFile);
      		    	    mIntent = new Intent(Nyala.this,NyalaShare.class);
      		         	mIntent.putExtra("qrstr", nyshareFile);
      		        	mIntent.putExtra("qrssid",qrssid);
      		     }
        	    startActivity(mIntent);	
        	}
        	return true;
        	
        case MENU_SHOWSCANS:
        //	mIntent = new Intent(Nyala.this,NyalaGallery.class);
        	mIntent = new Intent(Nyala.this,NyalaHistory.class);
        	startActivity(mIntent);
        	return true;
       
        case MENU_SETTINGS:
        	mIntent = new Intent(Nyala.this,NyalaSettings.class);
        	startActivity(mIntent);
        	return true;
        	
        case MENU_ABOUT:
        	mIntent = new Intent(Nyala.this,NyalAbout.class);
        	startActivity(mIntent);
        	return true;
        	
        case MENU_HELP: 
        	mIntent = new Intent(Nyala.this,NyalaHelp.class);
        	startActivity(mIntent);
        	return true;
        }
        
        return false;
    } 
       
    @Override
    public void onStart() {
    	super.onStart();
    	nyalaPrefs = PreferenceManager.getDefaultSharedPreferences(Nyala.this);
    	connectAction=nyalaPrefs.getBoolean("autoExit", connectAction);
    	scanAction=nyalaPrefs.getBoolean("autoConnect", scanAction);
    	saveScanAction = nyalaPrefs.getBoolean("autoSave", saveScanAction);
    	scanSavePath = nyalaPrefs.getString("ScanPath", scanSavePath);
    	
    }

    @Override
    public void onResume() {
    	   super.onResume();
    	   
    	   nyalaPrefs = PreferenceManager.getDefaultSharedPreferences(Nyala.this);
    	   connectAction=nyalaPrefs.getBoolean("stayRunning", connectAction);
       	   scanAction=nyalaPrefs.getBoolean("autoConnect", scanAction);
       	   saveScanAction = nyalaPrefs.getBoolean("autoSave", saveScanAction);
       	
       	   IntentFilter wifilter = new IntentFilter();
           wifilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
           wsr = new WifiStatusReceiver();
           registerReceiver(wsr, wifilter);
       	   checkWiFiStatus();
    }

    @Override
    public void onPause() {
    	super.onPause();  
    	unregisterReceiver(wsr);
    	
    }

    @Override
    public void onStop() {
    	super.onStop();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    }
 
      
   private void checkWiFiStatus() {
	  final WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	   
	 	  if (!(wm.isWifiEnabled())) {
	 	  AlertDialog.Builder ad = new AlertDialog.Builder(this);
		  ad.setMessage("Wireless is currently disabled. Tap 'Enable' to turn on WiFi")
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
	 			ScanResult sr = getDetailsForAP(apname);
	 			 setAPInfoView(sr.SSID,Integer.toString(sr.frequency),Integer.toString(sr.level));

	 			 Toast.makeText( Nyala.this, "Already connected to"+apname+", scanning another network will disconnect you", Toast.LENGTH_SHORT ).show();
	 		     
	 		 }
	 	  }	  	  
   }
    
   public void ScanBtnClickHandler(View v) {
	   
	    scantry=1;   
	    String btntext=null;
	    List<WifiConfiguration> wcl;
	    WifiConfiguration wc_active = new WifiConfiguration();
	    WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	    Button Btn = (Button)findViewById(id.scanBtn);
	         
	    btntext = new String (Btn.getText().toString());
	    if (btntext.contains("Disconnect")) {
	   
	       String curssid = new String(wm.getConnectionInfo().getSSID());
	 	   wcl = wm.getConfiguredNetworks();
	 	    
	 	   for (WifiConfiguration wc : wcl) {
	 		  Log.i("INFO","wc.SSID="+wc.SSID+" curssid="+curssid);
	 	        if (wc.SSID.contains(curssid)) {
	 	    		wc_active = wc;
	 	    	}
	 	    }
	 	    //If the user wants to disconnect,
	        //Adjust the priority of the current connection downward to make sure they don't immediately reconnect;
	        wm.disconnect();
	        wm.disableNetwork(wc_active.networkId);
	        wc_active.priority = 2;
	        wm.updateNetwork(wc_active);
	        Btn.setText("Scan and Connect");
	        ImageView iv = (ImageView)findViewById(id.nyala);
	        iv.setImageResource(R.drawable.nyala);
	        
	        
	    } else {    
	         Intent zxScanIntent = new Intent("com.google.zxing.client.android.SCAN");
	         
             PackageManager pm = getPackageManager();
             try {
				pm.getPackageInfo("com.google.zxing.client.android", PackageManager.GET_ACTIVITIES);
				zxScanIntent.setPackage("com.google.zxing.client.android");
		        zxScanIntent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		        startActivityForResult(zxScanIntent, 0);      
			 } catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				 AlertDialog.Builder ad = new AlertDialog.Builder(this);
				  ad.setMessage("Nyala needs the BarcodeScanner App installed")
				         .setCancelable(false)
				         .setPositiveButton("Install BarcodeScanner App", new DialogInterface.OnClickListener() {
				             public void onClick(DialogInterface dialog, int id) {
				            	 Toast.makeText( Nyala.this, "Launching Google Play...", Toast.LENGTH_SHORT ).show();
				            	 //Launch Google Play Store
				            	 Uri storeUri = Uri.parse("https://play.google.com/search?q=com.google.zxing.client.android");
				            	 Intent installIntent = new Intent(Intent.ACTION_VIEW,storeUri);
				            	 startActivity(installIntent);
				             }
				         })                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
				         .setNegativeButton("Exit Nyala", new DialogInterface.OnClickListener() {
				             public void onClick(DialogInterface dialog, int id) {
				                  dialog.cancel();
				                  finish();
				             }
				         });
				  AlertDialog InstallZXing_AD = ad.create();
			                  InstallZXing_AD.show();
			}
	    }

   }
     
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	   
	    WifiConfiguration wc;
	        if ( (requestCode == 0) && ((resultCode == RESULT_OK) ) ) {
	            qrcontents = new String(intent.getStringExtra("SCAN_RESULT"));
	            qrformat = new String(intent.getStringExtra("SCAN_RESULT_FORMAT"));
	            // Handle successful scan
	      
	            final WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	           
	           wc = new WifiConfiguration();
	        	   
	        	  if (qrcontents.contains("WIFI")) {
	        		 wc = ParseForZXingEncoding(qrcontents);
	        	  }
	        	  if (qrcontents.endsWith("::")) {
	        		  wc = ParseForNyalaEncoding(qrcontents);
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

	        	   String ssid_str = new String(wc.SSID);
	        		        	
	        	   //We should give the user a chance to see what SSID they've scanned in before connecting
	        	   //This (will be) settable for those that just want to get on with things. 
	  
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
	        	     if (scanAction) {

	        	    //	 Toast.makeText( Nyala.this, "Connecting...", Toast.LENGTH_SHORT ).show();
	        	        
	        	        ConnectFromScan(wc,wm,qrcontents); 
	        	     } else {
	        	    	final  WifiConfiguration wcon=wc;
	        	    	 AlertDialog.Builder ad = new AlertDialog.Builder(this);
	       	      	     ad.setMessage("Ready to connect to "+ssid_str+". Continue?")
	       	      	         .setTitle("Confirm Connection")
	       	      	         .setCancelable(false)
	       	      	         .setPositiveButton("OK", new DialogInterface.OnClickListener() {  
	       	      	             public void onClick(DialogInterface dialog, int id) {

	     	 	        	    	// Toast.makeText( Nyala.this, "Connecting...", Toast.LENGTH_SHORT ).show();
	     	 	        	    	ConnectFromScan(wcon,wm,qrcontents); 

	       	      	             }
	       	      	         })
	       	      	    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	   		             public void onClick(DialogInterface dialog, int id) {
	   		                  dialog.cancel();
	 	        	    	 Toast.makeText( Nyala.this, "Connection Cancelled", Toast.LENGTH_SHORT ).show();

	   		             }
	   		         });
	       	      	     AlertDialog AD = ad.create();
                         AD.show();   
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

   private void ConnectFromScan(WifiConfiguration wc,WifiManager wm,String qrstr) {
	   
       ProgressDialog pd=ProgressDialog.show(Nyala.this, "Connection Progress", "Connecting...", true);
       String apfreq=null;
       String apsignal=null;
       String currSSID = wc.SSID.replace("\"","");
       int netId =0;
       List<ScanResult> sr = wm.getScanResults();
       List<WifiConfiguration>wc_remembered = wm.getConfiguredNetworks();
       
       for (ScanResult ap : sr) {
    	    
    	    if (ap.SSID.equals(currSSID)) {
    	    	apfreq = new String(Integer.toString(ap.frequency));
    	    	apsignal = new String(Integer.toString(ap.level));
    	    	Log.i("INFO","ConnectFromScan: ScanResults - apfreq="+apfreq+" apsignal="+apsignal);
    	    	break;
    	    }
       }
       
       for (WifiConfiguration wcr: wc_remembered) {
    	   String listedSSID = new String(wcr.SSID.replace("\"",""));
    	   if (listedSSID.equals(currSSID)) {
    		   netId=wcr.networkId;
    		   Log.i("INFO","FOUND NETWORK ID "+netId+" FOR "+wcr.SSID);
    		   break;
    	   }
       }
       
	   wc.hiddenSSID=true;
 	  
	   wc.priority = 1;
       wc.status = WifiConfiguration.Status.ENABLED;  
       
       //If the network isn't in the current list of configured networks, add it in
       if (netId ==0 ){
    	   netId = wm.addNetwork(wc);
       }
           
       //So we've got everything packed up this point. Let's lob it over the wall and see if it sticks
          
       if (wm.enableNetwork(netId, true)) {
    	   pd.dismiss();
    	   if (saveScanAction) {
    		 //  saveScan(qrstr,wc.SSID.toString());
    		   saveScan(qrstr, wc.SSID);
    	   }
    	   Bitmap bm = scanToBitmap(qrstr);
    	   ImageView iv = (ImageView) findViewById(id.nyala);
    	   iv.setImageBitmap(bm);
    	   setAPInfoView(wc.SSID,apfreq,apsignal);
    	   
    	   
       } else {
    	   pd.setMessage("...Failed");
       }
       pd.dismiss();
   }
   
   public String getSSIDFromQRStr(String qrcontents) {
	   WifiConfiguration wconfig = new WifiConfiguration();
	   
	   if (qrcontents.contains("WIFI")) {
  		 wconfig = ParseForZXingEncoding(qrcontents);
  		// Log.i("INFO","GOT ZXing Encoded SSID:"+wconfig.SSID);
  	  }
  	  if (qrcontents.endsWith("::")) {
  		  wconfig = ParseForNyalaEncoding(qrcontents);
  		//  Log.i("INFO","GOT Nyala Encoded SSID:"+wconfig.SSID);
  	  }
  	  return wconfig.SSID;
   }
   
   private WifiConfiguration ParseForNyalaEncoding(String qrcontents) {
	   
	   //Take the results of the scanned text and parse it here, returning a WifiConfiguration object
	   // Nyala formatted QRCodes look like: 
	   // SSID::MAC::PSK:: <---note the terminating ::
	   //Like "MyWLAN::de:ad:be:ef:31:33:70::c0ffeeace1::"
	   // For an open network, leave the PSK off the end of the string or make it match [OoPpEeNn]"
	   
	   String theSSID =null;
	   String thePSK = new String("None");
	   String theBSSID = null; 
	   WifiConfiguration wc = null;
	   String nyala_delim = new String("::");
		  
		  int ssidpos=0;
		  int endpos=0;
		  int bssidpos=0;
		  int contentslen=0;
		 
		  int len_check=0;
		  int delim=0;
		  int type=0;
		  int ssid_len=0;
		  int sepcount=0;
		  int sepcheck=0;
		   
		 contentslen = qrcontents.length(); 
		  //validate length
		 		
	     if ( contentslen >0) {
			 //check for overly long string: SSID Max length + BSSID(if present) + 64 bytes PSK(max)+formatting=115 chars
			  if (contentslen <= 115) {
				 len_check=1;
				 
			  }
				  
		 } 
		
		 if (qrcontents.endsWith(nyala_delim)) {
			         delim=1;
				 if (!qrcontents.matches(";")) {
					 type=1;
				 }
		 }
				 
				String [] qr_array =qrcontents.split("::");
				
					
				 if (qr_array[0].length() <=32 ) {
			           	ssid_len = 1;	 
				 }
				 for (char c : qrcontents.toCharArray()) {
		               if (c == ':')
		            	   sepcount++;
		           }
				 if  (sepcount == 11)  {
					   sepcheck=1;
				 }
				 
				 if (len_check+delim+type+ssid_len+sepcheck == 5) {
				
		       	   ssidpos=qrcontents.indexOf("::");
		           bssidpos = qrcontents.indexOf("::",ssidpos+1);
			       endpos=qrcontents.indexOf("::",bssidpos+1);
			   
			       theSSID= new String (qrcontents.substring(0,ssidpos));
			       theBSSID= new String(qrcontents.substring(ssidpos+2,bssidpos));
			       thePSK= new String (qrcontents.substring(bssidpos+2,endpos));
			       
			       wc = wifiBasicConfig(theSSID,thePSK);
			       wc.BSSID = theBSSID;  
			       
			} else {
				    wc = new WifiConfiguration();
				    wc.SSID= new String("invalid");
			}
				 
		return wc;
   }
   
   private WifiConfiguration ParseForZXingEncoding(String qrcontents) {
	   
	   //Parse out and process a ZXing-encoded string
	   
	   String WlanAuthType=null;
	   String theSSID =null;
	   String thePSK = null;
	   String wifiStr = null;
	   String[] wifiStrArr=null;
	   
	   WifiConfiguration wc = null;
	
	   int endpos=0;
	   int contentslen=0;	 
	   int length=0;
	   int delim=0;
       int type=0;
	   int ssid_len=0;
	   int sepcheck=0;
	   int components=0;
	   
	   Log.i("INFO","qrcontents="+qrcontents);
	   
	   contentslen = qrcontents.length(); 
		  //validate length
	     if ( ( contentslen >0) && (contentslen <= 115) ) {
			 
			 //check for overly long string: SSID Max length + BSSID(if present) + 64 bytes PSK(max)+formatting=115 chars
	 		 length=1;			  
		 } 
	     
	     if ( (endpos=qrcontents.indexOf(";;")) >0) {
			  delim=1;
	     }
	     
		 if ( (qrcontents.startsWith("WIFI")) && (qrcontents.contains("T:")) 
			   && (qrcontents.contains("S:")) && (qrcontents.contains("P:")) ){
         
		    wifiStr = new String (qrcontents.substring(5,endpos));
		    wifiStrArr = new String[3];
		    wifiStrArr = wifiStr.split(";");
		    
		    for (int i=0;i < 3;i++) {
		    			    	
		    	if (wifiStrArr[i].startsWith("S:")) {
		    		theSSID = new String(wifiStrArr[i].substring(2));
		    		components +=1;
		    	}
		    	if (wifiStrArr[i].startsWith("T:")) {
		    		WlanAuthType= new String(wifiStrArr[i].substring(2));
		    		components +=1;
		    	}
		    	if (wifiStrArr[i].startsWith("P:")) {
		    		thePSK= new String(wifiStrArr[i].substring(2));
		    		components+=1;
		    	}
		    }
		    
		 }   
		    
		 if (components !=3) {
			 
			    wc = new WifiConfiguration();
			    wc.SSID= new String("invalid");
		    	AlertDialog.Builder ad = new AlertDialog.Builder(this);
				  ad.setTitle("Invalid format in scanned data")
				         .setMessage("QRCode did not contain a valid WIFI string" )
				         .setCancelable(false)
				         .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				             public void onClick(DialogInterface dialog, int id) {
				             dialog.cancel();
				             }
				         });                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
				        
				  AlertDialog WiFi_AD = ad.create();
			                  WiFi_AD.show();
		 } else {

			 //Sorry, not supporting your WEP-using ass. 
		    //You must place your WEP-based AP in the Emergency Artificial Intelligence Incinerator labeled "2002" before moving into the next decade
		    if (WlanAuthType.equals("WEP") ) {
		    	type=0;
		    	wc = new WifiConfiguration();
				wc.SSID= new String("invalid");
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
		   
	           type=1;
	           if (theSSID.length() <=32) {
	        	   ssid_len=1;
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
                	           	           
	           if ( (sepcount1 == 4) && ( sepcount2 == 4)) {
	        	   sepcheck=1;
	           }	   
	        }
	        // if all the parsing checks have passed, create a new WifiConfiguration object to pass back to the ActivityResult handler 
		  
		    if (length+type+ssid_len+delim+sepcheck == 5) {
		    	wc = wifiBasicConfig(theSSID,thePSK);
		    } else {
			    wc = new WifiConfiguration();
			    wc.SSID= new String("invalid");
		}
		 }
		  
		    return wc;
   }
   
   private WifiConfiguration wifiBasicConfig(String theSSID, String thePSK) {
	
	  WifiConfiguration wc = new WifiConfiguration();
	  wc.SSID = "\""+theSSID+"\"";
	  
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
	       }
				  
	return wc;			   
   }
   
   public class WifiStatusReceiver extends BroadcastReceiver {
	   
	   @Override
	   public void onReceive(Context context, Intent intent) {
		  
		   if (intent.getAction().toString().equals("android.net.wifi.STATE_CHANGE")) {
			   
			   NetworkInfo wsr_ni = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			    
			   if (wsr_ni.getState() == State.CONNECTED) {
                 
				   Button connectBtn = (Button) findViewById(id.scanBtn);
				   connectBtn.setText("Disconnect");
			
				   toggleAPInfoVisibility(View.VISIBLE);
				   Toast.makeText( Nyala.this, "Wifi now connected...", Toast.LENGTH_SHORT ).show();
				   
				   if (connectAction)  {
					   if (scantry > 0) {
				           finish();
					   } 
				   }  
			   }
			   
			   if (wsr_ni.getState() == State.CONNECTING) {
				 //  Toast.makeText(Nyala.this, "Connecting...", Toast.LENGTH_SHORT).show();
		
			   } 
			   if (wsr_ni.getDetailedState() == DetailedState.FAILED) {
				   if (! (wsr_ni.getReason() == null) ) {
				    	  String rsn_str = new String (wsr_ni.getReason());
				    	 
				    	  Button connectBtn = (Button) findViewById(id.scanBtn);
						  connectBtn.setText("Scan and Connect");
				    	  
				    	  Toast.makeText(Nyala.this,"Wifi Connection failed because "+rsn_str+". Try again in a moment", Toast.LENGTH_LONG).show();
				    	   } else {
				    		toggleAPInfoVisibility(View.INVISIBLE);  
				    		Toast.makeText(Nyala.this, "Wifi disconnected", Toast.LENGTH_LONG).show();
				    	   }
				       }             
		  }
		   
	   }

   }
   
   private void saveScan(String qrstr,String ssidstr) {
      
	  String clean_ssidstr= new String(ssidstr.substring(1,(ssidstr.length())-1)); 
		  
	      Bitmap scanBm= scanToBitmap(qrstr);
       //   NyalaLib nl = new NyalaLib(Nyala.this);
          if (!(nl.saveScan(scanBm,clean_ssidstr,Nyala.this))) {
    	  
    	      AlertDialog.Builder ad = new AlertDialog.Builder(this);
		      ad.setTitle("File IO Error")
		             .setMessage("Could not save scanned barcode, check filename and permissions" )
		             .setCancelable(false)
		             .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		                 public void onClick(DialogInterface dialog, int id) {
		                 dialog.cancel();
		                 }
		         });                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
		        
		       AlertDialog saveScanAD = ad.create();
	                       saveScanAD.show();
         } else {
        	 String sf = new String("nyala_"+clean_ssidstr+".png");
        	 if( nl.readScanFromStorage(sf,Nyala.this))  {
        		 Log.i("INFO","SUCCESSFULLY READ BACK "+sf);
        	 } else {
        		 Log.e("ERROR", "Failed to read back "+sf);
        	 }
        	 
         }
   }
   
   private Bitmap scanToBitmap(String qrstr) {	

	   int minside = 0;
	   DisplayMetrics dm = new DisplayMetrics();
 	   getWindowManager().getDefaultDisplay().getMetrics(dm);
 	   int hip = dm.heightPixels;
 	   int wip = dm.widthPixels;
	   if (hip < wip) {
		   minside = hip;
	   } else {
		   minside = wip;
	   }
           Intent encode_intent = new Intent("com.google.zxing.client.android.ENCODE");
     	   encode_intent.setAction(Intents.Encode.ACTION);
     	   encode_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
     	   encode_intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
     	   encode_intent.putExtra(Intents.Encode.DATA, qrstr);
     	   encode_intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
     	   QRCodeEncoder nyshareEncoder = new QRCodeEncoder(this,encode_intent,minside);
     	   
            Bitmap nyshareBm = null;
 		   try {
 			   nyshareBm = nyshareEncoder.encodeAsBitmap();
 			
 		   } catch (WriterException e) {
 			   e.printStackTrace();
 		   }
 		   return nyshareBm;
 		
  } 
   
  private void toggleAPInfoVisibility(int visibility) {

	  TextView ssidLbl =  (TextView) findViewById(R.id.ssidLbl);
	   TextView ssidTxt = (TextView) findViewById(R.id.ssidTV);
	   TextView channelLbl = (TextView) findViewById(R.id.channelLbl);
	   TextView channelTxt = (TextView) findViewById(R.id.channelTV);
	   TextView signalLbl = (TextView) findViewById(R.id.signalLbl);
	   TextView signalTxt = (TextView) findViewById(R.id.signalTV);
	   
	   ssidLbl.setVisibility(visibility);
	   ssidTxt.setVisibility(visibility);
	   channelLbl.setVisibility(visibility);
	   channelTxt.setVisibility(visibility);
	   signalLbl.setVisibility(visibility);
	   signalTxt.setVisibility(visibility);  
  }
  
  private void setAPInfoView(String ssid, String channel, String signal) {
	  
	   TextView ssidTxt = (TextView) findViewById(R.id.ssidTV);
	   TextView channelTxt = (TextView) findViewById(R.id.channelTV);
	   TextView signalTxt = (TextView) findViewById(R.id.signalTV);
	   
	   ssidTxt.setText(ssid);
	   channelTxt.setText(channel);
	   signalTxt.setText(signal);
	   
  }
  
  private ScanResult getDetailsForAP(String theSSID) {
	  
	 WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	 List<ScanResult> curr_scanlist = wm.getScanResults();
	 ScanResult theSSID_Result=null;
	       
	       for (ScanResult sr : curr_scanlist) {
	    	    
	    	    if (sr.SSID.equals(theSSID)) {
	    	    	theSSID_Result = sr;
	    	    	break;
	    	    }
	       }
	  return theSSID_Result;
  }


}  