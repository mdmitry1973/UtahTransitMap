package com.mdmitry1973.utahtransitmap;

import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class CurrentPositionItem   implements OverlayItem {
	
	private GeoPoint geoPoint;
	//private int stopType;
	//private int stop_id;
	//private int stop_code;
	//private String stop_name;
	//private String stop_desc;
	private	MainActivity activity;
	//private boolean visibled;
	
	//public static final int k_stopBus	= 0;
	//public static final int k_stopFlex	= 1;
	//public static final int k_stopMax	= 2;
	//public static final int k_stopTrax	= 3;
	//public static final int k_stopFront	= 4;
	
	private static double metersToPixels(double latitude, float meters, byte zoom) {
		double groundResolution = MercatorProjection.calculateGroundResolution(latitude, zoom);
		return meters / groundResolution;
	}
	
	public CurrentPositionItem(GeoPoint geoPoint, MainActivity activity) {
		this.geoPoint = geoPoint;
		this.activity = activity;
	}
	
	@Override
	public synchronized boolean draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point canvasPosition) {
		if (this.geoPoint == null) {
			return false;
		}

		Bitmap bitmap = activity.getCurrentPositionBitmap();
		double latitude = this.geoPoint.latitude;
		double longitude = this.geoPoint.longitude;
		float pixelX = (float) (MercatorProjection.longitudeToPixelX(longitude, zoomLevel) - canvasPosition.x);
		float pixelY = (float) (MercatorProjection.latitudeToPixelY(latitude, zoomLevel) - canvasPosition.y);
		float widthInPixel = (float)bitmap.getHeight();// metersToPixels(latitude, 10, zoomLevel);
		
		//if (widthInPixel >= 1 &&
		//	pixelX > 0 && pixelY > 0)
		//{
			
			
			
			Rect dst = new Rect((int)(pixelX - widthInPixel), (int)(pixelY - widthInPixel), 
								(int)(pixelX + widthInPixel), (int)(pixelY + widthInPixel));
			
			
			canvas.drawBitmap(bitmap, null, dst, null);
			
		//	visibled = true;
		//}
		//else
		//{
		//	visibled = false;
		//}

		return true;
	}

	//public boolean getVisibled()
	//{
	//	return visibled;
	//}
	
	//public GeoPoint getGeoPoint()
	//{
	//	return geoPoint;
	//}
}
