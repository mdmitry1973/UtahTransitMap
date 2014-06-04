package com.mdmitry1973.utahtransitmap;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class MyLocationListener implements  LocationListener {
	
	private	MainActivity m_activity;
	
	void setMainActivity(MainActivity activity)
	{
		m_activity = activity;		
	}

	@Override
    public void onLocationChanged(Location loc)
    {

      loc.getLatitude();
      loc.getLongitude();

      String Text = "My current location is: " +
      "Latitud = " + loc.getLatitude() +
      "Longitud = " + loc.getLongitude();
      
      Log.v("MyLocationListener", Text);

      m_activity.onLocationChanged(loc);
    }

    @Override
    public void onProviderDisabled(String provider)
    {
    	Log.v("MyLocationListener", "onProviderDisabled provider=" + provider);
    }

    @Override
    public void onProviderEnabled(String provider)
    {
    	Log.v("MyLocationListener", "onProviderEnabled provider=" + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    	Log.v("MyLocationListener", "onStatusChanged status=" + status);
    }
}
