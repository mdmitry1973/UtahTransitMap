package com.mdmitry1973.utahtransitmap;

import java.util.ArrayList;

import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class TripOverlayItem implements OverlayItem {
	
	public String tripID;
	public String routeID;
	public ArrayList<GeoPoint> geoPoints;
	public int color;
	private	Paint paintLine;
	
	public TripOverlayItem(String tripID, String routeID, ArrayList<GeoPoint> geoPoints, int color, MainActivity activity) 
	{
		this.tripID = tripID;
		this.routeID = routeID;
		this.geoPoints = geoPoints;
		this.color = color;
		
		paintLine = new Paint();
		paintLine.setColor(color);
		paintLine.setStrokeWidth(2);
		paintLine.setStyle(Paint.Style.STROKE);
		paintLine.setStrokeJoin(Paint.Join.ROUND);
	}

	@Override
	public boolean draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas,
			Point canvasPosition) {
		
		float pixelOldX = 0;
		float pixelOldY = 0;
		
		for(int i = 0; i < geoPoints.size() ; i++)
		{
			GeoPoint geoPoint = geoPoints.get(i);
			double latitude = geoPoint.latitude;
			double longitude = geoPoint.longitude;
			float pixelX = (float) (MercatorProjection.longitudeToPixelX(longitude, zoomLevel) - canvasPosition.x);
			float pixelY = (float) (MercatorProjection.latitudeToPixelY(latitude, zoomLevel) - canvasPosition.y);
			
			if (i != 0)
			{
				canvas.drawLine(pixelOldX, pixelOldY, pixelX, pixelY, paintLine);
			}
			
			canvas.drawCircle(pixelX, pixelY, 5, paintLine);
			
			pixelOldX = pixelX;
			pixelOldY = pixelY;
		}
		
		return false;
	}
}
