package com.mdmitry1973.utahtransitmap;

import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.util.Log;

public class CurrentPositionItem   implements OverlayItem {
	
	private GeoPoint 		geoPoint;
	private	MainActivity 	activity;
	private	Paint 			paintLineRuler;
	private	Paint 			paintTextRuler;
	
	private static double metersToPixels(double latitude, float meters, byte zoom) 
	{
		double groundResolution = MercatorProjection.calculateGroundResolution(latitude, zoom);
		return meters / groundResolution;
	}
	
	public CurrentPositionItem(GeoPoint geoPoint, MainActivity activity) 
	{
		this.geoPoint = geoPoint;
		this.activity = activity;
		
		paintLineRuler = new Paint();
		paintLineRuler.setColor(Color.BLACK);
		paintLineRuler.setStrokeWidth(5);
		paintLineRuler.setStyle(Paint.Style.STROKE);
		paintLineRuler.setStrokeJoin(Paint.Join.ROUND);
		
		paintTextRuler = new Paint();
		paintTextRuler.setColor(Color.BLACK);
		paintTextRuler.setTextSize(20);
		paintTextRuler.setFakeBoldText(true);
	}
	
	public void setGeoPoint(GeoPoint geoPoint)
	{
		this.geoPoint = geoPoint;
	}
	
	@Override
	public synchronized boolean draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point canvasPosition) 
	{
		float pixelLeft = (float) (MercatorProjection.longitudeToPixelX(boundingBox.minLongitude, zoomLevel) - canvasPosition.x);
		float pixelTop = (float) (MercatorProjection.latitudeToPixelY(boundingBox.minLatitude, zoomLevel) - canvasPosition.y);
		float pixelRight = (float) (MercatorProjection.longitudeToPixelX(boundingBox.maxLongitude, zoomLevel) - canvasPosition.x);
		float pixelBottom = (float) (MercatorProjection.latitudeToPixelY(boundingBox.maxLatitude, zoomLevel) - canvasPosition.y);
		
		try
		{
			float[] resultPt=new float[5]; 
			
			double LatitudeY1 = MercatorProjection.pixelYToLatitude(pixelBottom + canvasPosition.y, zoomLevel);
			double LatitudeY2 = MercatorProjection.pixelYToLatitude(pixelBottom - 1 + canvasPosition.y, zoomLevel);
				
			Location.distanceBetween (LatitudeY1, boundingBox.maxLongitude, LatitudeY2, boundingBox.maxLongitude, resultPt);
			
			{
				float disM = pixelTop*resultPt[0];//result[0];
				double r =Math.log10(disM);
				double p =Math.pow(10, (double)(int)r);
				double closeP = p;
				int nR = 1;
				double pixMin = 0;
				
				for(; closeP + p < disM;)
				{
					closeP = closeP + p;
					nR++;
				}
				
				double diff = disM - closeP;
				
				if (diff < closeP/6)
				{
					closeP = closeP - p;
					diff = disM - closeP;
					nR--;
				}
				
				pixMin = diff/resultPt[0];
				String strDis = new String("" + closeP + " m");
				Rect bounds = new Rect();
				
				double pinPex = p/resultPt[0];
				
				if (nR == 1)
				{
					pinPex = ((p/10)/resultPt[0]);
					nR = 11;
				}
				
				paintTextRuler.getTextBounds(strDis, 0, strDis.length(), bounds);
				
				canvas.drawLine((float)pixelRight - 10, (float)pixelTop, (float)pixelRight - 10, (float)pixelBottom + (float)pixMin, paintLineRuler);
				
				for(int n = 0; n < nR; n++)
				{
					canvas.drawLine((float)pixelRight, (float)(pixelTop - pinPex*n), (float)pixelRight - 20, (float)(pixelTop - pinPex*n), paintLineRuler);
				}
				
				canvas.drawText(strDis, (float)pixelRight - bounds.width() - 10, (float)pixelBottom + (float)pixMin - (bounds.height() - 5), paintTextRuler);
			}
			
			double LatitudeX1 = MercatorProjection.pixelYToLatitude(pixelBottom + canvasPosition.y, zoomLevel);
			double LatitudeX2 = MercatorProjection.pixelYToLatitude(pixelBottom - 1 + canvasPosition.y, zoomLevel);
				
			Location.distanceBetween (boundingBox.maxLatitude, LatitudeX1, boundingBox.maxLatitude, LatitudeX2, resultPt);
			
			{
				float disM = pixelRight*resultPt[0];//result[0];
				double r =Math.log10(disM);
				double p =Math.pow(10, (double)(int)r);
				double closeP = p;
				int nR = 1;
				double pixMin = 0;
				
				for(; closeP + p < disM;)
				{
					closeP = closeP + p;
					nR++;
				}
				
				double diff = disM - closeP;
				
				if (diff < closeP/6)
				{
					closeP = closeP - p;
					diff = disM - closeP;
					nR--;
				}
				
				pixMin = diff/resultPt[0];
				String strDis = new String("" + closeP + " m");
				Rect bounds = new Rect();
				
				double pinPex = p/resultPt[0];
				
				if (nR == 1)
				{
					pinPex = ((p/10)/resultPt[0]);
					nR = 11;
				}
				
				paintTextRuler.getTextBounds(strDis, 0, strDis.length(), bounds);
				
				canvas.drawLine((float)pixelLeft + (float)pixMin, (float)pixelTop - 10, (float)pixelRight, (float)pixelTop - 10, paintLineRuler);
				
				for(int n = 0; n < nR; n++)
				{
					float xxPos = (float) (pixelRight - pinPex*n);
					canvas.drawLine(xxPos, (float)(pixelTop), xxPos, (float)(pixelTop - 20), paintLineRuler);
				}
				
				canvas.drawText(strDis,(float)(pixelLeft + pixMin) - (bounds.width() + 10), (float)(pixelTop - 10), paintTextRuler);
			}
		}
		catch(Exception ex)
		{
			Log.v("MainActivity", "trouble ", ex);
		}
		
		if (this.geoPoint == null) 
		{
			return false;
		}
		
		Bitmap bitmap = activity.getCurrentPositionBitmap();
		double latitude = this.geoPoint.latitude;
		double longitude = this.geoPoint.longitude;
		float pixelX = (float) (MercatorProjection.longitudeToPixelX(longitude, zoomLevel) - canvasPosition.x);
		float pixelY = (float) (MercatorProjection.latitudeToPixelY(latitude, zoomLevel) - canvasPosition.y);
		float widthInPixel = (float)bitmap.getHeight();// metersToPixels(latitude, 10, zoomLevel);
		
		Rect dst = new Rect((int)(pixelX - widthInPixel), (int)(pixelY - widthInPixel), 
							(int)(pixelX + widthInPixel), (int)(pixelY + widthInPixel));
			
		canvas.drawBitmap(bitmap, null, dst, null);
			
		return true;
	}
}
