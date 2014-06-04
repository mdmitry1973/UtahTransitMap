package com.mdmitry1973.utahtransitmap;

import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class OverlayStopItem  implements OverlayItem {
	
	private GeoPoint geoPoint;
	private int stopType;
	private String stop_id;
	private String stop_code;
	private String stop_name;
	private String stop_desc;
	private	MainActivity activity;
	private boolean visibled;
	
	public static final int k_stopBus	= 0;
	public static final int k_stopFlex	= 1;
	public static final int k_stopMax	= 2;
	public static final int k_stopTrax	= 3;
	public static final int k_stopFront	= 4;
	
	private static double metersToPixels(double latitude, float meters, byte zoom) {
		double groundResolution = MercatorProjection.calculateGroundResolution(latitude, zoom);
		return meters / groundResolution;
	}
	
	public OverlayStopItem(GeoPoint geoPoint, MainActivity activity) {
		this.geoPoint = geoPoint;
		this.activity = activity;
		this.stopType = k_stopBus;
		
		stop_id = "";
		stop_code = "";
		stop_name = "";
		stop_desc = "";
	}
	
	@Override
	public synchronized boolean draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point canvasPosition) {
		if (this.geoPoint == null) {
			return false;
		}

		double latitude = this.geoPoint.latitude;
		double longitude = this.geoPoint.longitude;
		float pixelX = (float) (MercatorProjection.longitudeToPixelX(longitude, zoomLevel) - canvasPosition.x);
		float pixelY = (float) (MercatorProjection.latitudeToPixelY(latitude, zoomLevel) - canvasPosition.y);
		float widthInPixel = (float) metersToPixels(latitude, 10, zoomLevel);
		//float hieghtInPixel = (float) metersToPixels(latitude, this.hieght, zoomLevel)/2;
		
		//if (stop_id == 18058)
		//{
		//	Log.v("MainActivity", "Latitudee,Longitude=");
		//}
		
		if (widthInPixel >= 1 &&
			pixelX > 0 && pixelY > 0)
		{
			//Log.v("MainActivity", "canvasPosition.x=" + canvasPosition.x + " canvasPosition.y" + canvasPosition.y);
			
			//Paint paintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
			//paintFill.setStyle(Paint.Style.FILL);
			//paintFill.setColor(Color.BLUE);
			//paintFill.setAlpha(64);
			
			//Paint paintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
			//paintStroke.setStyle(Paint.Style.STROKE);
			//paintStroke.setColor(Color.BLUE);
			//paintStroke.setAlpha(100);
			//paintStroke.setStrokeWidth(2);
			
			//canvas.drawCircle(pixelX, pixelY, widthInPixel + 1, paintStroke);
			//canvas.drawCircle(pixelX, pixelY, widthInPixel + 1, paintFill);
			
			Bitmap bitmap = activity.getStopBitmap(stopType);
			//Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			Rect dst = new Rect((int)(pixelX - widthInPixel), (int)(pixelY - widthInPixel), 
								(int)(pixelX + widthInPixel), (int)(pixelY + widthInPixel));
			
			//canvas.drawText("name=" + stop_name + " lat=" + geoPoint.latitude + " long=" + geoPoint.longitude, pixelX, pixelY, paintStroke);
			
			//Paint paint = new Paint();
			//paint.setAntiAlias(true);
			////paint.setFilterBitmap(true);
			//paint.setDither(true);
		
			canvas.drawBitmap(bitmap, null, dst, null);
			
			visibled = true;
		}
		else
		{
			visibled = false;
		}

		return true;
	}

	public void setStopType(int type)
	{
		stopType = type;
	}
	
	public int getStopType()
	{
		return stopType;
	}
	
	public void setStopId(String id)
	{
		stop_id = id;
	}
	
	public void setStopCode(String code)
	{
		stop_code = code;
	}
	
	public void setStopName(String name)
	{
		stop_name = name;
	}
	
	public void setStopDesc(String desc)
	{
		stop_desc = desc;
	}
	
	public String getStopId()
	{
		return stop_id;
	}
	
	public String getStopCode()
	{
		return stop_code;
	}
	
	public String getStopName()
	{
		return stop_name;
	}
	
	public String getStopDesc()
	{
		return stop_desc;
	}
	
	public boolean getVisibled()
	{
		return visibled;
	}
	
	public GeoPoint getGeoPoint()
	{
		return geoPoint;
	}
}
