package com.duxet.zkmap.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.duxet.zkmap.models.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.content.Context;

public class Storage {
	public Context mContext;
	
	public Storage(Context context) {
		mContext = context;
	}
	
	public void saveRoutes() {
		new Thread(new Runnable() {
	        public void run() {
	        	saveToFile("routes.json", Routes.getRoutes().toString());
	        }
	    }).start();
	}
	
	public void loadRoutes() {
		new Thread(new Runnable() {
	        public void run() {
	        	String content = loadFromFile("routes.json");
	        	
	        	try {
					Routes.setRoutes(new JSONObject(content));
				} catch (JSONException e) {}
	        }
	    }).start();
	}
	
	public void saveStops() {
		new Thread(new Runnable() {
	        public void run() {
	        	JSONObject stopsObject = new JSONObject();
	        	
	        	Map<Integer, Stop> stops = StopsContainer.getStops();
	    		
	        	if (stops == null || stops.isEmpty())
	        		return;
	        	
	            for (Stop stop : stops.values()) {
	            	JSONObject stopObject = new JSONObject();
	            	
	            	try {
						stopObject.put("id", stop.getId());
						stopObject.put("name", stop.getName());
						stopObject.put("type", stop.getType());
						stopObject.put("lat", stop.getLat());
						stopObject.put("lon", stop.getLon());
						
						stopsObject.put("stops", stopObject);
					} catch (JSONException e) {}
	            }
	            
	            saveToFile("stops.json", stopsObject.toString());
	        }
	    }).start();
	}
	
	public void loadStops() {
		new Thread(new Runnable() {
	        public void run() {
	        	String content = loadFromFile("stops.json");
	        	
	        	JSONObject stopsObject = new JSONObject(content);
	            Iterator<?> keys = stopsObject.keys();

	            while( keys.hasNext() ){
	                String key = (String)keys.next();
	                if( jObject.get(key) instanceof JSONObject ){

	                }
	            }
	        }
	    }).start();
	}
	
	private void saveToFile(String file, String content) {
		try {
			FileOutputStream fOut;
			fOut = mContext.openFileOutput(file,
			        Context.MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(fOut); 
			osw.write(content);
			osw.flush();
			osw.close();
		} catch (Exception e) { }
	}
	
	private String loadFromFile(String file) {
		StringBuilder sb = new StringBuilder();
		
		try {
			FileInputStream fIn;
			fIn = mContext.openFileInput(file);
			InputStreamReader isr = new InputStreamReader(fIn);
			BufferedReader bufferedReader = new BufferedReader(isr);
			String line;
		
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
			
		} catch (Exception e) { }
		
		return sb.toString();
	}
}
