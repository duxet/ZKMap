package com.duxet.zkmap.models;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.json.JSONException;
import org.json.JSONObject;

import com.duxet.zkmap.remote.ZKM;

import android.content.Context;

public class Routes {
	private static JSONObject routes = new JSONObject();
	private static Context mContext;
	
	public static void setContext(Context context) {
		mContext = context;
	}
	
	public static void addRoute(String line, String type, String desc) {
		try {
			routes.put(line + "_" + type, desc);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static String getRoute(String line, String type) {
		if (!routes.has(line + "_" + type))		
			ZKM.updateLine(line);

		try { 
			return routes.getString(line + "_" + type);
		} catch (JSONException e) {
			e.printStackTrace();
		}	

		return null;
	}
	
	public static JSONObject getRoutes() {
		return routes;
	}
	
	public static void setRoutes(JSONObject newRoutes) {
		routes = newRoutes;
	}

	public static void saveRoutes() {
		new Thread(new Runnable() {
	        public void run() {
	        	FileOutputStream fOut;
	        	
				try {
					fOut = mContext.openFileOutput("routes.json",
					        Context.MODE_PRIVATE);
					OutputStreamWriter osw = new OutputStreamWriter(fOut); 
					osw.write(routes.toString());
					osw.flush();
					osw.close();
				} catch (Exception e) { }
	        }
	    }).start();
	}
	
	public static void loadRoutes() {
		new Thread(new Runnable() {
	        public void run() {
	        	FileInputStream fIn;
	        	
				try {
					fIn = mContext.openFileInput("routes.json");
					InputStreamReader isr = new InputStreamReader(fIn);
        			BufferedReader bufferedReader = new BufferedReader(isr);
        			StringBuilder sb = new StringBuilder();
        			String line;
				
        			while ((line = bufferedReader.readLine()) != null) {
						sb.append(line);
					}
        			
        			routes = new JSONObject(sb.toString());
				} catch (Exception e) { }
	        }
	    }).start();
	}
	
}
