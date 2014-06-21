package com.mdmitry1973.utahtransitmap;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.mapsforge.android.AndroidUtils;
import org.mapsforge.android.maps.DebugSettings;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapScaleBar;
import org.mapsforge.android.maps.MapScaleBar.TextField;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewPosition;
import org.mapsforge.android.maps.mapgenerator.TileCache;
import org.mapsforge.android.maps.overlay.Circle;
import org.mapsforge.android.maps.overlay.ListOverlay;
import org.mapsforge.android.maps.overlay.Marker;
import org.mapsforge.android.maps.overlay.MyLocationOverlay;
import org.mapsforge.android.maps.overlay.Overlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.IOUtils;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.reader.header.FileOpenResult;
import org.mapsforge.map.reader.header.MapFileInfo;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MainActivity extends MapActivity implements ColorPickerDialog.OnColorChangedListener  {
	
	public final String kMapFileName			= "utah.map";
	public final String kStopsFileName 			= "stops.txt";
	public final String kAgencyFileName 		= "agency.txt";
	public final String kCalendarFileName 		= "calendar.txt";
	public final String kCalendarDatesFileName	= "calendar_dates.txt";
	public final String kShapesFileName 		= "shapes.txt";
	public final String kStopTimesFileName 		= "stop_times.txt";
	public final String kTripsFileName 			= "trips.txt";
	
	public final int k_stop_param_stop_id 	= 0;
	public final int k_stop_param_stop_code = 1;
	public final int k_stop_param_stop_name = 2;
	public final int k_stop_param_stop_desc = 3;
	public final int k_stop_param_stop_lat 	= 4;
	public final int k_stop_param_stop_lon 	= 5;
	public final int k_stop_param_zone_id 	= 6;
	public final int k_stop_param_stop_url 	= 7;
	public final int k_stop_param_location_type 	= 8;
	public final int k_stop_param_parent_station 	= 9;
	
	public final String DROIDS_COLOR_KEY = "DROIDS_COLOR_KEY";
	public final int DROIDS_COLOR_DEFAULT = Color.BLUE;
	
	MapView mapView;
	ListOverlay listOverlay;
	Map<String, ArrayList<String>> mapServiceData = new HashMap<String, ArrayList<String>>();
	
	Bitmap bitmapBus;
	Bitmap bitmapFlex;
	Bitmap bitmapMax;
	Bitmap bitmapTrax;
	Bitmap bitmapFront;
	
	Bitmap bitmapCurrentPosition;
	
	Map<String,File> filesMaps;
	MainActivity 	activity;
	MyLocationListener mlocListener = null;
	ListOverlay m_listOverlayCurrentPosition = null;
	
	ListOverlay m_listOverlayTrips = new ListOverlay();
	
	public static final String PREFS_NAME = "UtahTransitMapPref";
	
	public static int currentDay = 0;
	
	ProgressDialog  progress = null;
	
	public final int k_event_unzip 			= 111;
	public final int k_event_loading_data 	= 222;
	
	public static String sel_tripId = ""; 
	public static String sel_roudeId = ""; 
	
	private Handler handler = new Handler() {
        @Override
            public void handleMessage(Message msg) {
        	
        	int y = msg.what;
        	
        	if (y == k_event_unzip)
        	{
        		progress.dismiss();
        		progress = new ProgressDialog(activity);
        		progress.setTitle(getResources().getString(R.string.Unzipping_data));
        		progress.setMessage(getResources().getString(R.string.please_wait_20));
        		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        		progress.setMax(7000);
        		progress.show();
        	}
        	else
        	if (y == k_event_loading_data)
        	{
        		progress.dismiss();
        		progress = new ProgressDialog(activity);
        		progress.setTitle(getResources().getString(R.string.loading_data));
        		progress.setMessage(getResources().getString(R.string.please_wait));
        		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        		progress.show();
        	}
        	else
        	{
        		Log.v("MainActivity", "unknown event = " + y);
        	}
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		activity = this;
		
		progress = new ProgressDialog(this);
		progress.setTitle(getResources().getString(R.string.loading_data));
  		progress.setMessage(getResources().getString(R.string.please_wait));
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.show();
		
	    Calendar rightNow = Calendar.getInstance();
	    
	    currentDay = rightNow.get(Calendar.DAY_OF_WEEK);

		bitmapBus = BitmapFactory.decodeResource(getResources(), R.drawable.bus);
		bitmapFlex = BitmapFactory.decodeResource(getResources(), R.drawable.flex_icon);
		bitmapMax = BitmapFactory.decodeResource(getResources(), R.drawable.max);
		bitmapTrax = BitmapFactory.decodeResource(getResources(), R.drawable.trax);
		bitmapFront = BitmapFactory.decodeResource(getResources(), R.drawable.front);
		
		bitmapCurrentPosition = BitmapFactory.decodeResource(getResources(), R.drawable.current_position);
		
	 	filesMaps = new HashMap<String,File> ();
		
		final File externalCacheDir = getExternalCacheDir();
		
		filesMaps.put(kMapFileName, 		new File(externalCacheDir, kMapFileName));
		filesMaps.put(kStopsFileName, 		new File(externalCacheDir, kStopsFileName));
		filesMaps.put(kAgencyFileName, 		new File(externalCacheDir, kAgencyFileName));
		filesMaps.put(kCalendarFileName, 	new File(externalCacheDir, kCalendarFileName));
		filesMaps.put(kCalendarDatesFileName, new File(externalCacheDir, kCalendarDatesFileName));
		filesMaps.put(kShapesFileName, 		new File(externalCacheDir, kShapesFileName));
		filesMaps.put(kStopTimesFileName, 	new File(externalCacheDir, kStopTimesFileName));
		filesMaps.put(kTripsFileName, 		new File(externalCacheDir, kTripsFileName));
		
		mapView = (MapView) findViewById(R.id.mapView);
  		
  		configureMapView();
		
		new Thread(new Runnable() {
			  @Override
			  public void run()
			  {
				  	//if (!filesMaps.get(kMapFileName).exists())
			  		{
			  			File externalStorageDirectory = Environment.getExternalStorageDirectory();
			  			String packageName = getApplicationContext().getPackageName();
			  			File storageLocation = new File(externalStorageDirectory, "/Android/obb/" + packageName);
			  			File dataZipFile = new File(storageLocation, "main." + getVersion() + ".com.mdmitry1973.UtahTransitMap.obb");
			  			File versionFile = new File(externalCacheDir.getAbsolutePath(), "version.xml");
			  			
			  			Log.v("MainActivity", "start unzip");
			  			try{
			  				boolean needUpdate = true;
			  				
		  					if (versionFile.exists())
		  					{
			  					try 
			  					{
				  					DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
									DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
									
									Document documentCurrent = xmlBuilder.parse(versionFile);
									Element elRootCurrent = documentCurrent.getDocumentElement();
						    			
									String counterCurrent = elRootCurrent.getAttribute("data_counter");
									
									if (counterCurrent.equalsIgnoreCase(getResources().getString(R.string.data_counter)) == true)
									{
										needUpdate = false;
									}
			  					} 
			  					catch (Exception e) {
									Log.v("MainActivity", "e=" + e);
								} 
		  					}
		  					
			  				if (needUpdate)
			  				{
			  					int nProgress = 0;
			  					
			  					handler.sendEmptyMessage(k_event_unzip);
			  					
			  					ZipInputStream zis = new ZipInputStream(new FileInputStream(dataZipFile));
			  					
			  					try {
			  					     
			  						ZipEntry ze;
			  					     while ((ze = zis.getNextEntry()) != null) {
			  					    	 
			  					    	File currentDir = new File(externalCacheDir.getAbsolutePath());
			  					    	 
			  					    	 String zipName =  ze.getName();
			  					    	 
			  					    	 if (zipName.indexOf('/') != -1)
			  					    	 {
			  					    		 if (zipName.indexOf('/') == zipName.length() - 1)
			  					    		 {
			  					    			String dirName = zipName.substring(0, zipName.length() - 1);
			  					    			File file = new File(currentDir, dirName);
			  					    			file.mkdirs();
			  					    			 continue;
			  					    		 }
			  					    		 else
			  					    		 {
				  					    		 String []dirs = zipName.split("/");
				  					    		 
				  					    		 for(int h = 0; h < dirs.length - 1; h++)
				  					    		 {
				  					    			File file = new File(currentDir, dirs[h]);
				  					    			if (!file.exists())
				  					    			{
				  					    				file.mkdir();
				  					    			}
						  					    	currentDir = file;
				  					    		 }
				  					    		 
				  					    		zipName =  dirs[dirs.length - 1];
			  					    		 }
			  					    	}
			  					    	
										File file = new File(currentDir, zipName);
										FileOutputStream stream = new FileOutputStream(file);
										byte[] buffer = new byte[1024];
										int count;
										while ((count = zis.read(buffer, 0, 1024)) != -1) 
										{
											stream.write(buffer, 0, count);
										}	
										
										stream.close();
											
										nProgress++;
										progress.setProgress(nProgress);	
			  					     }
			  					 } catch (IOException e) {
			  						// TODO Auto-generated catch block
			  						e.printStackTrace();
			  					} finally {
			  					     try {
			  							zis.close();
			  						} catch (IOException e) {
			  							// TODO Auto-generated catch block
			  							e.printStackTrace();
			  						}
			  					 }
			  					
			  					try {
			  						DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
			  						DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
			  						Document document = xmlBuilder.newDocument();
			  						Element elRoot = document.createElement("version");
			  						elRoot.setAttribute("data_counter", getResources().getString(R.string.data_counter));
			  						document.appendChild(elRoot);
			  						
			  					// Use a Transformer for output
									TransformerFactory tFactory = TransformerFactory.newInstance();
									Transformer transformer = tFactory.newTransformer();
									
									DOMSource source = new DOMSource(document);
									StreamResult result = new StreamResult(versionFile);
									transformer.transform(source, result); 
			  					}
								catch (Exception e) {
									Log.v("MainActivity", "e=" + e);
								} 
			  					
			  					handler.sendEmptyMessage(k_event_loading_data);
			  				}
			  			}
			  			catch(FileNotFoundException e)
			  			{
			  				e.printStackTrace();
			  			}
			  			Log.v("MainActivity", "stop unzip");
			  		}
			  		
			  		try{
				  		BufferedReader serviceFileBuffer = new BufferedReader(new FileReader(filesMaps.get(kCalendarFileName)));
						
				  		serviceFileBuffer.readLine();
						
						while(serviceFileBuffer.ready())
						{
							//service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date
							String serviceLine = serviceFileBuffer.readLine();
							String[] serviceData = serviceLine.split(",");
							
							ArrayList<String> arrayList = new ArrayList<String>();
							
							arrayList.add(serviceData[1]);
							arrayList.add(serviceData[2]);
							arrayList.add(serviceData[3]);
							arrayList.add(serviceData[4]);
							arrayList.add(serviceData[5]);
							arrayList.add(serviceData[6]);
							arrayList.add(serviceData[7]);
							arrayList.add(serviceData[8]);
							arrayList.add(serviceData[9]);
							
							mapServiceData.put(serviceData[0], arrayList);
						}
						
						serviceFileBuffer.close();
			  		}
					catch(Exception ex)
					{
						Log.v("MainActivity", "read CalendarFileName");
					}
			  		
			  		FileOpenResult fileOpenResult = mapView.setMapFile(filesMaps.get(kMapFileName));
			  		
			  		if (!fileOpenResult.isSuccess()) {
			  			Log.v("MainActivity", "cannot read map");
			  			finish();
			  		}
			  		
			  		//add stops
			  		Log.v("MainActivity", "start add stops");
			  		try 
			  		{
			  			List<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
			  			String line = "";
			  			int index = 0;
			  			  
			  			BufferedReader br = new BufferedReader(new FileReader(filesMaps.get(kStopsFileName)));
			  			while ((line = br.readLine()) != null) {

			  				if (index > 0)
			  				{
			  					String[] params =  line.split(",");
			  			    
			  				    if (params.length >= k_stop_param_parent_station)
			  				    {
			  				    	String  stop_lat = params[k_stop_param_stop_lat];
			  				    	String  stop_lon = params[k_stop_param_stop_lon];
			  				    	
			  				    	try 
			  						{
			  					    	GeoPoint geoPoint = new GeoPoint(Float.parseFloat(stop_lat), Float.parseFloat(stop_lon));
			  					    	
			  					    	//overlayItems.add(new Circle(geoPoint, 5, paintFill, paintStroke));
			  					    	
			  					    	OverlayStopItem item = new OverlayStopItem(geoPoint, activity);
			  					    	
			  					    	item.setStopType(OverlayStopItem.k_stopBus);
			  					    	item.setStopId(params[k_stop_param_stop_id]);
			  					    	item.setStopCode(params[k_stop_param_stop_code]);
			  					    	item.setStopName(params[k_stop_param_stop_name]);
			  					    	item.setStopDesc(params[k_stop_param_stop_desc]);
			  					    	
			  					    	overlayItems.add(item);
			  					    	
			  						} catch (Exception e) {
			  							// TODO Auto-generated catch block
			  							e.printStackTrace();
			  						}
			  				    }
			  				    else
			  				    {
			  				    	assert(false);
			  				    }
			  				}
			  				
			  				index++;
			  			}
			  			
			  			br.close();
			  			
			  			listOverlay = new ListOverlay();
			  			listOverlay.getOverlayItems().addAll(overlayItems);
			  			mapView.getOverlays().add(listOverlay);
			  			
			  			mapView.getOverlays().add(m_listOverlayTrips);
			  			
			  		} catch (FileNotFoundException e) {
			  			// TODO Auto-generated catch block
			  			e.printStackTrace();
			  		} catch (IOException e) {
			  			// TODO Auto-generated catch block
			  			e.printStackTrace();
			  		}
			  		
			  		Log.v("MainActivity", "end add stops");
			  		
			  		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			  		
			  		if (settings.contains("tripsId"))
			  		{
				  		String tripsId = settings.getString("tripsId", "");
				  		String roudesId = settings.getString("roudesId", "");
				  		String colorsId = settings.getString("colorsId", "");
				  		
				  		String[] arrTripsId = tripsId.split(",");
				  		String[] arrRoutesId = roudesId.split(",");
				  		String[] arrColorsId = colorsId.split(",");
				  		
				  		for(int t = 0; t < arrTripsId.length; t++)
				  		{
				  			sel_tripId = arrTripsId[t]; 
				  			sel_roudeId = arrRoutesId[t]; 
				  			
				  			colorChanged("", Integer.parseInt(arrColorsId[t]));
				  		}
			  		}
			  		
			  		mapView.redraw();
			  		
			  		

			    runOnUiThread(new Runnable() {
			      @Override
			      public void run()
			      {
			    	  progress.dismiss();
			    	  
			    	  AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			    	  builder.setMessage(R.string.tap_on_station).setTitle(R.string.app_name);
			    	  AlertDialog dialog = builder.create();
			    	
			    	  dialog.show();
			      }
			    });
			  }
			}).start();
		
			mapView.setOnTouchListener(new OnTouchListener() {
            
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				Log.v("MainActivity", "OnTouchListener=" + event.toString());
				
				if (event.getAction() == MotionEvent.ACTION_UP && 
					event.getEventTime() - event.getDownTime() > 1000)
				{
					try{
						
						Log.v("MainActivity", "check for stop");
						
						float x = event.getX();
						float y = event.getY();
						GeoPoint geoPointUser = mapView.getProjection().fromPixels((int)x, (int)y);
						
						Log.v("MainActivity", "Latitudee,Longitude=" + geoPointUser.latitude + "," + geoPointUser.longitude);
						
						List<OverlayItem> OverlayItems = listOverlay.getOverlayItems();
						
						for(int i = 0; i < OverlayItems.size(); i++)
						{
							OverlayStopItem item = (OverlayStopItem) OverlayItems.get(i);
							
							if (item.getVisibled())
							{
								GeoPoint geoPoint = item.getGeoPoint();
								float [] dist = new float[1];
								
								Location.distanceBetween (geoPoint.latitude, geoPoint.longitude, geoPointUser.latitude, geoPointUser.longitude, dist);
								
								if (dist[0] < 10)
								{
									String stop_id = item.getStopId();
									RoutesDialog dialog = new RoutesDialog(activity, stop_id);
									
									dialog.show();
									
									break;
								}
							}
						}
					}
					catch(Exception ex)
					{
						Log.v("MainActivity", "trouble ", ex);
					}
				}
				
				return false;
			}
        });
	}
	
	
	@Override
    protected void onStop(){
       super.onStop();
       
	   MapViewPosition currentPosition = mapView.getMapViewPosition();
	
	   SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	   SharedPreferences.Editor editor = settings.edit();
	   editor.putInt("current_ZoomLevel", currentPosition.getZoomLevel());
	   editor.putString("current_latitude", String.valueOf(currentPosition.getCenter().latitude));
	   editor.putString("current_longitude", String.valueOf(currentPosition.getCenter().longitude));
	   
	   String tripsId = "";
	   String roudesId = "";
	   String colorsId = "";
	   
	   List<OverlayItem> list = m_listOverlayTrips.getOverlayItems();
		
	   for(int ii = 0; ii < list.size(); ii++)
	   {
		   tripsId = tripsId + ((TripOverlayItem)list.get(ii)).tripID + ",";
		   roudesId = roudesId + ((TripOverlayItem)list.get(ii)).routeID + ",";
		   colorsId = colorsId + ((TripOverlayItem)list.get(ii)).color + ",";
	   }
		
	   editor.putString("tripsId", tripsId);
	   editor.putString("roudesId", roudesId);
	   editor.putString("colorsId", colorsId);
	   
	   editor.commit();
    }

	
	protected void onDestroy(){
		super.onDestroy();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_exit:
	        {
	        	 finish();
	        }
	        return true;
	        
	        case R.id.action_cur_location:
	        {
	        	/* Use the LocationManager class to obtain GPS locations */
	            LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

	            mlocListener = new MyLocationListener();
	            mlocListener.setMainActivity(this);
	            mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
	        	
	        }
	        return true;
	        
	        case R.id.action_settings:
	            //showHelp();
	           // return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public int getVersion() {
	    int v = 0;
	    try {
	        v = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
	    } catch (NameNotFoundException e) {
	    	e.printStackTrace();
	    }
	    return v;
	}
	
	private void configureMapView() {
		this.mapView.setBuiltInZoomControls(true);
		this.mapView.setClickable(true);
		this.mapView.setFocusable(true);
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		
	    
		if (settings.contains("current_ZoomLevel"))
		{
			int currentZoom = settings.getInt("current_ZoomLevel", 1);
			String currentLatitude = settings.getString("current_latitude", "");
			String currentLongitude = settings.getString("current_longitude", "");
			MapViewPosition currentPosition = mapView.getMapViewPosition();
			GeoPoint geoPoint = null;
			
			if (!currentLatitude.isEmpty() &&
				!currentLongitude.isEmpty())
			{
				geoPoint = new GeoPoint(Float.parseFloat(currentLatitude), Float.parseFloat(currentLongitude));
			}
			
			currentPosition.setZoomLevel((byte) currentZoom);
			
			if (geoPoint != null)
			{
				currentPosition.setCenter(geoPoint);
			}
		}
		
		m_listOverlayCurrentPosition = new ListOverlay();
		
		//if (settings.contains("current_position_latitude") &&
		//	settings.contains("current_position_longitude"))
		//{
			GeoPoint geoPoint = null;
			String currentLatitude = settings.getString("current_position_latitude", "");
			String currentLongitude = settings.getString("current_position_longitude", "");
			
			if (!currentLatitude.isEmpty() &&
				!currentLongitude.isEmpty())
			{
				geoPoint = new GeoPoint(Float.parseFloat(currentLatitude), Float.parseFloat(currentLongitude));
			}
			
			CurrentPositionItem item = new CurrentPositionItem(geoPoint, activity);
			m_listOverlayCurrentPosition.getOverlayItems().add(item);
		//}
		
		mapView.getOverlays().add(m_listOverlayCurrentPosition);

		//MapScaleBar mapScaleBar = this.mapView.getMapScaleBar();
		//mapScaleBar.setText(TextField.KILOMETER, getString(R.string.unit_symbol_kilometer));
		//mapScaleBar.setText(TextField.METER, getString(R.string.unit_symbol_meter));
	}
	
	public Bitmap getStopBitmap(int id)
	{
		if (id == OverlayStopItem.k_stopBus)
			return bitmapBus;
		if (id == OverlayStopItem.k_stopFlex)
			return bitmapFlex;
		if (id == OverlayStopItem.k_stopMax)
			return bitmapMax;
		if (id == OverlayStopItem.k_stopTrax)
			return bitmapTrax;
		if (id == OverlayStopItem.k_stopFront)
			return bitmapFront;
		return bitmapBus;
	}
	
	public Bitmap getCurrentPositionBitmap()
	{
		return bitmapCurrentPosition;
	}
	
	public void colorChanged(String key, int color)
	{
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putInt(key, color);
		
		try{
			ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
			BufferedReader serviceFileBuffer = new BufferedReader(new FileReader(filesMaps.get(kStopTimesFileName)));
			
	  		serviceFileBuffer.readLine();
	  		
	  		Boolean foundTrip = false;
			
			while(serviceFileBuffer.ready())
			{
				//trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled
				String serviceLine = serviceFileBuffer.readLine();
				String[] serviceData = serviceLine.split(",");
				
				if (serviceData[0].compareTo(sel_tripId) == 0)
				{
					foundTrip = true;
					
					String stop_id = serviceData[3];
					
					/*
					<?xml version="1.0" encoding="UTF-8" standalone="no"?>
					<Stop stop_code="174068" stop_desc="E 9400 S" stop_id="13697" stop_lat="40.580351" stop_lon="-111.829475"
					 */
					
					File externalCacheDir = getExternalCacheDir();
					File stopFile = new File(new File(externalCacheDir, "stops_data"), String.format("%s", stop_id));
					
					try {
						DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder xmlBuilder;
						xmlBuilder = xmlFactory.newDocumentBuilder();
						Document document;
						document = xmlBuilder.parse(stopFile);
						Element elRoot = document.getDocumentElement();
						
						String stop_lat = elRoot.getAttribute("stop_lat");
						String stop_lon = elRoot.getAttribute("stop_lon");
						
						GeoPoint point = new GeoPoint(Double.parseDouble(stop_lat), Double.parseDouble(stop_lon));
						
						geoPoints.add(point);
						
					}
					catch(Exception ex)
					{
						Log.v("MainActivity", "read CalendarFileName");
					}
				}
				else
				{
					if (foundTrip == true)
					{
						break;
					}
				}
			}
			
			serviceFileBuffer.close();
			
			TripOverlayItem item = new TripOverlayItem(sel_tripId,  sel_roudeId, geoPoints, color, this);
			
			List<OverlayItem> list = m_listOverlayTrips.getOverlayItems();
			
			for(int ii = 0; ii < list.size(); ii++)
			{
				if (((TripOverlayItem)list.get(ii)).routeID.compareTo(sel_roudeId) == 0)
				{
					list.remove(ii);
					break;
				}
			}
			
			m_listOverlayTrips.getOverlayItems().add(item);
  		}
		catch(Exception ex)
		{
			Log.v("MainActivity", "read CalendarFileName");
		}
		
		mapView.redraw();
	}
	
	public void setTripOverlay(String tripId, String roudeId)
	{
		sel_tripId = tripId; 
		sel_roudeId = roudeId; 
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	   
		ColorPickerDialog colorDiaolg = new ColorPickerDialog(this, this, DROIDS_COLOR_KEY, settings.getInt(DROIDS_COLOR_KEY, DROIDS_COLOR_DEFAULT), DROIDS_COLOR_DEFAULT);
		
		colorDiaolg.show();
	}
	
	public void getTripOverlay(ArrayList<String> tripsId, ArrayList<String> roudesId)
	{
		List<OverlayItem> list = m_listOverlayTrips.getOverlayItems();
		
		for(int ii = 0; ii < list.size(); ii++)
		{
			tripsId.add(((TripOverlayItem)list.get(ii)).tripID);
			roudesId.add(((TripOverlayItem)list.get(ii)).routeID);
		}
	}
	
	public void onLocationChanged(Location loc)
    {
		MapViewPosition currentPosition = mapView.getMapViewPosition();
		GeoPoint geoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
		
		CurrentPositionItem item = (CurrentPositionItem)m_listOverlayCurrentPosition.getOverlayItems().get(0);
		
		item.setGeoPoint(geoPoint);
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString("current_position_latitude", String.valueOf(geoPoint.latitude));
	    editor.putString("current_position_longitude", String.valueOf(geoPoint.longitude));
	       
	    editor.commit();
		
		currentPosition.setCenter(geoPoint);
		
		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        mlocManager.removeUpdates(mlocListener);
    }
	
	public Map<String, ArrayList<String>> getMpServiceData()
	{
		return mapServiceData;
	}

	public int getCurrentDay()
	{
		return currentDay;
	}
}
