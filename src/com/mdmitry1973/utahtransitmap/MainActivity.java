package com.mdmitry1973.utahtransitmap;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.reader.header.FileOpenResult;
import org.mapsforge.map.reader.header.MapFileInfo;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MainActivity extends MapActivity  {
	
	public final String kMapFileName			= "utah.map";
	public final String kStopsFileName 			= "stops.txt";
	public final String kAgencyFileName 		= "agency.txt";
	public final String kCalendarFileName 		= "calendar.txt";
	public final String kCalendarDatesFileName	= "calendar_dates.txt";
	public final String kShapesFileName 		= "shapes.txt";
	public final String kStopTimesFileName 		= "stop_times.txt";
	public final String kTripsFileName 			= "trips.txt";
	
	//public final String kStopTripsFileName	= "stopTrips";
	
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
	
	MapView mapView;
	ListOverlay listOverlay;
	
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
	
	public static final String PREFS_NAME = "UtahTransitMapPref";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		activity = this;
		
		final ProgressDialog  progress = ProgressDialog.show(this, "Loading data", "Please wait....", true);
		
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

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
				  	if (!filesMaps.get(kMapFileName).exists())
			  		{
			  			File externalStorageDirectory = Environment.getExternalStorageDirectory();
			  			String packageName = getApplicationContext().getPackageName();
			  			File storageLocation = new File(externalStorageDirectory, "/Android/obb/" + packageName);
			  			File dataZipFile = new File(storageLocation, "main." + getVersion() + ".com.mdmitry1973.UtahTransitMap.obb");
			  			
			  			Log.v("MainActivity", "start unzip");
			  			try{
			  					ZipInputStream zis = new ZipInputStream(new FileInputStream(dataZipFile));
			  					
			  					
			  					try {
			  					     ZipEntry ze;
			  					     while ((ze = zis.getNextEntry()) != null) {
			  					    	 
			  					    	File currentDir = new File(externalCacheDir.getAbsolutePath());
			  					    	 
			  					    	 //Log.v("MainActivity", "getName=" + ze.getName());
			  					    	 
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
										 
										if (!file.exists())
										{
											FileOutputStream stream = new FileOutputStream(file);
											byte[] buffer = new byte[1024];
											int count;
											while ((count = zis.read(buffer, 0, 1024)) != -1) 
											{
												stream.write(buffer, 0, count);
											}	
											
											stream.close();
										}
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
			  	
			  			}
			  			catch(FileNotFoundException e)
			  			{
			  				e.printStackTrace();
			  			}
			  			Log.v("MainActivity", "stop unzip");
			  			
			  			//parse stop times
				  		//Log.v("MainActivity", "start parse stop times");
				  		/*
				  		try 
				  		{
				  			//List<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
				  			String line = "";
				  			int index = 0;
				  			File externalCacheDir = getExternalCacheDir();
				  			  
				  			BufferedReader br = new BufferedReader(new FileReader(filesMaps.get(kStopTimesFileName)));
				  			while ((line = br.readLine()) != null) {

				  				if (index > 0)
				  				{
				  					String[] params =  line.split(",");
				  			    
				  					//trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled
				  					int stop_id = Integer.parseInt(params[3]);
				  					
				  					File file = new File(externalCacheDir, stop_id + ".txt");
				  					
				  					//file.delete();
				  					
				  					BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				  					//params.
				  					//String param = "";//params.toString();
				  					//Log.v("MainActivity", "param=" + param);
				  					//for(int i = 0; i < params.length; i++)
				  					//{
				  					//	if (i == 3)
				  					//	{
				  					//		
				  					//	}
				  					//	else
				  					//	{
				  					//		param = param + params[1] + ",";
				  					//	}
				  					//}
				  					
				  					bw.write(params.toString());
				  					bw.close();
				  				}
				  				
				  				index++;
				  			}
				  			
				  			br.close();
				  			
				  		} catch (FileNotFoundException e) {
				  			// TODO Auto-generated catch block
				  			e.printStackTrace();
				  		} catch (IOException e) {
				  			// TODO Auto-generated catch block
				  			e.printStackTrace();
				  		}
				  		*/
				  		Log.v("MainActivity", "end parse stop times");
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
			  			
			  		} catch (FileNotFoundException e) {
			  			// TODO Auto-generated catch block
			  			e.printStackTrace();
			  		} catch (IOException e) {
			  			// TODO Auto-generated catch block
			  			e.printStackTrace();
			  		}
			  		
			  		Log.v("MainActivity", "end add stops");
			  		
			  		mapView.redraw();

			    runOnUiThread(new Runnable() {
			      @Override
			      public void run()
			      {
			    	  progress.dismiss();
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
									File externalCacheDir = getExternalCacheDir();
									File stopFile = new File(new File(externalCacheDir, "stops_data"), String.format("%s", stop_id));
									
									Log.v("MainActivity", "dis2=" + dist[0] +" geoPoint.latitude=" + geoPoint.latitude + " geoPoint.longitude=" + geoPoint.longitude + " StopName=" + item.getStopName());
									Log.v("MainActivity", "fount stop name=" + item.getStopName());
									
									final Dialog dialog = new Dialog(activity);
									
									dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
									dialog.setContentView(R.layout.stop_dialog);
									
									dialog.setCanceledOnTouchOutside(true);
									
									TextView nameItem = (TextView) dialog.findViewById(R.id.textViewStopName);
									TextView descItem = (TextView) dialog.findViewById(R.id.textViewStopDesc);
									TextView codeItem = (TextView) dialog.findViewById(R.id.textViewStopCode);
									
									LinearLayout layoutItem = (LinearLayout) dialog.findViewById(R.id.routes);
									
									if (nameItem != null)
									{
										nameItem.setText(item.getStopName());
									}
									
									if (descItem != null)
									{
										descItem.setText(item.getStopDesc());
									}
									
									if (codeItem != null)
									{
										codeItem.setText("" + item.getStopCode());
									}
									
									if (layoutItem != null)
									{
										DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
										DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
										Document document = xmlBuilder.parse(stopFile);
										Element elRoot = document.getDocumentElement();
										
										/*
										  	<?xml version="1.0" encoding="UTF-8" standalone="no"?>
											<Stop stop_code="SOTNMALL" stop_desc="UNNAMED ST" stop_id="100" stop_lat="40.564691" stop_lon="-111.895258" stop_name="SOUTH TOWNE MALL">
											  <routesItems>
											    <Route roudeId="41266" route_long_name="DRAPER FLEX" route_short_name="F546">
											      <Trip arrival_time="8:40:00" block_id="723945" departure_time="8:40:00" direction_id="1" service_id="4" shape_id="92527" stop_headsign="" stop_id="100" stop_sequence="2" trip_headsign="F546 DRAPER FLEX - TO DRAPER" trip_id="1515712"/>
											    </Route>
											  </routesItems>
											</Stop>
										 */
										
										NodeList roudeNodes = elRoot.getElementsByTagName("routesItems");
										
										if (roudeNodes.getLength() > 0)
										{
											Element routeNode = (Element)roudeNodes.item(0);
											
											NodeList roudeList = routeNode.getElementsByTagName("Route");
											
											for(int ii = 0; ii < roudeList.getLength(); ii++)
											{
												routeNode = (Element)roudeList.item(ii);
												
												String str_roudeId = routeNode.getAttribute("roudeId");
												String str_route_long_name = routeNode.getAttribute("route_long_name");
												String str_route_short_name = routeNode.getAttribute("route_short_name");
												
												Button routeButton = new Button(dialog.getContext());
												
												routeButton.setText(str_route_short_name);
												
												routeButton.setOnClickListener(new View.OnClickListener() {
											        @Override
											        public void onClick(View v) {
											            //label.setText("Clicked button for " + name); 
											        	Log.v("MainActivity", "Clicked button");
											        }
											    });
												
												LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
												layoutItem.addView(routeButton, lp);
											}
										}
									}
									
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
			GeoPoint geoPoint = new GeoPoint(Float.parseFloat(currentLatitude), 
												Float.parseFloat(currentLongitude));
		    	
			
			currentPosition.setZoomLevel((byte) currentZoom);
			currentPosition.setCenter(geoPoint);
			
		}

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
	
	public void onLocationChanged(Location loc)
    {
		MapViewPosition currentPosition = mapView.getMapViewPosition();
		GeoPoint geoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
		
		if (m_listOverlayCurrentPosition == null)
		{
			m_listOverlayCurrentPosition = new ListOverlay();
			CurrentPositionItem item = new CurrentPositionItem(geoPoint, activity);
			m_listOverlayCurrentPosition.getOverlayItems().add(item);
			mapView.getOverlays().add(m_listOverlayCurrentPosition);
		}
		else
		{
			m_listOverlayCurrentPosition.getOverlayItems().clear();
			CurrentPositionItem item = new CurrentPositionItem(geoPoint, activity);
			m_listOverlayCurrentPosition.getOverlayItems().add(item);
		}
		
		currentPosition.setCenter(geoPoint);
		
		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        mlocManager.removeUpdates(mlocListener);
    }

}
