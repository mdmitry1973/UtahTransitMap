package com.mdmitry1973.utahtransitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class UpdateTimeTablleTask extends AsyncTask<String, Integer, Boolean> {
    
 private	MainActivity 	activity;
 
 public UpdateTimeTablleTask(MainActivity activity) 
	{
	 this.activity = activity;
	}
 
 public void publishProgressData(int progress) 
	{
	  publishProgress(progress);
	}

 @Override
    protected Boolean doInBackground(String... urls) {
          
  			InputStream input = null;
  	        OutputStream output = null;
  	        HttpURLConnection connection = null;
  	        Boolean res = false;
  	        File outputDir = activity.getCacheDir(); // context being the Activity pointer
          	File outputFile = null;
  	        
  			try {
  				outputFile = File.createTempFile("prefix", "extension", outputDir);
  				
  				URL url = new URL(urls[0]);//"http://www.gtfs-data-exchange.com/agency/utah-transit-authority/latest.zip");
  	            connection = (HttpURLConnection) url.openConnection();
  	            connection.connect();

  	            // expect HTTP 200 OK, so we don't mistakenly save error report
  	            // instead of the file
  	            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
  	                //return "Server returned HTTP " + connection.getResponseCode()
  	                //        + " " + connection.getResponseMessage();
  	            	Log.v("MainActivity", "Error" + connection.getResponseCode());
  	            }
  	            
  	            int fileLength = connection.getContentLength();
  	          	
  	          
  	          	// download the file
  	            input = connection.getInputStream();
  	            
      	        output = new FileOutputStream(outputFile);
      	  	
  	            byte data[] = new byte[4096];
  	            long total = 0;
  	            int count;
  	            while ((count = input.read(data)) != -1) {
  	                // allow canceling with back button
  	                if (isCancelled()) {
  	                    input.close();
  	                    return null;
  	                }
  	                total += count;
  	                // publishing the progress....
  	                if (fileLength > 0) // only if total length is known
  	                    publishProgress((int) (total * 100 / fileLength));
  	                output.write(data, 0, count);
  	            }
  	            
  	        } catch (Exception e) {
  	            //return e.toString();
  	        	Log.v("MainActivity", "Error" + e);
  	        } finally {
  	            try {
  	                if (output != null)
  	                    output.close();
  	                if (input != null)
  	                    input.close();
  	            } catch (IOException ignored) {
  	            }

  	            if (connection != null)
  	                connection.disconnect();
  	        }
  			
  			File externalCacheDir = activity.getExternalCacheDir();
  			
  			if (outputFile != null)
  			{
  				activity.handler.sendEmptyMessage(activity.k_event_unzip2);
  				activity.unZipData(outputFile, externalCacheDir);
  				
  				outputFile.delete();
  				
  				activity.handler.sendEmptyMessage(activity.k_event_prepare_data);
  				activity.progressPrepareData = 0;
  				PrepareData  prepareData = new PrepareData();
  						
  				prepareData.mainStartPrepareData(externalCacheDir,  this);
  				
  				res = true;
  			}
  			
        	return res;
    }
 
 	public void changeMessageDialog(int id)
 	{
 		activity.handler.sendEmptyMessage(id);
 	}
    
    protected void onProgressUpdate(Integer... progress) {
      
    	activity.progressDialog.setProgress(progress[0]);
    	//Log.v("MainActivity", "progress=" + progress[0]);
    }

    
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(Boolean result) {
    	activity.progressDialog.dismiss();
    	
    	if (result == true)
    	{
    		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
				SharedPreferences.Editor editor = sharedPrefs.edit();
		    	editor.putString("data_date", activity.data_date);
		    	editor.commit();
    	}
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    	builder.setMessage(R.string.finished_update_data).setTitle(R.string.app_name).setPositiveButton("Ok", null);
    	AlertDialog dialog = builder.create();
    	dialog.show();
    	
    	activity.setRequestedOrientation(activity.nRequestedOrientation);
   }
}