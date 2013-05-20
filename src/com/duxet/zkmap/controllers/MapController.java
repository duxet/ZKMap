package com.duxet.zkmap.controllers;

import java.util.Map;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.duxet.zkmap.R;
import com.duxet.zkmap.models.*;
import com.duxet.zkmap.remote.*;
import com.duxet.zkmap.utils.TextDrawable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapController {
	private static GoogleMap mMap;
	private static Activity activity;
	private static SharedPreferences sharedPref;
	
	public static void setup(GoogleMap googleMap, Activity act, SharedPreferences sharedPreferences) {
		mMap = googleMap;
		activity = act;
		sharedPref = sharedPreferences;
	}
	
	public static void updateVehicles() {
    	ZKM.updateVehicles();

    	Map<Integer, Vehicle> vehicles = VehiclesContainer.getVehicles();
		
    	if (vehicles == null || vehicles.isEmpty())
    		return;
    	
        for (Vehicle vehicle : vehicles.values()) {
            Double lat =  ((double) vehicle.getLat()) / 100000;
			Double lon = ((double) vehicle.getLon()) / 100000;
			
			// Check for invalid position and line
			if (lat < 53 || lat > 55 || lon < 17 || lon > 19)
				continue;

			if (vehicle.getLine().isEmpty())
				continue;
			
			// Get destination for that bus
			String dest = Routes.getRoute(vehicle.getLine(), vehicle.getLineType());
			
			// Add marker on map or update it if already exists
			LatLng point = new LatLng(lat, lon);
			Marker marker = vehicle.getMarker();
			
			if (marker != null) {
				Long currentTime = System.currentTimeMillis()/1000;
				
				if(vehicle.getLastUpdate() < currentTime - 30)
					removeMarker(marker);
				else
					updateMarkerPosition(marker, point);
			} else {
				Bitmap label = createVehicleLabel(vehicle.getLine(), dest);
				createVehicleMarker(point, vehicle, dest, label);
			}
        }
	}
	
	public static void updateStops() {
		Map<Integer, Stop> stops = StopsContainer.getStops();
		
    	if (stops == null || stops.isEmpty())
    		return;
    	
        for (Stop stop : stops.values()) {
			LatLng point = new LatLng(stop.getLat(), stop.getLon());
			Marker marker = stop.getMarker();
			
			if (marker != null) {
				updateMarkerPosition(marker, point);
			} else {
				createStopMarker(point, stop);
			}
        }
	}
	
	public static void toggleVehiclesVisibility(Boolean visible) {
    	Map<Integer, Vehicle> vehicles = VehiclesContainer.getVehicles();
		
    	if (vehicles == null || vehicles.isEmpty())
    		return;
    	
        for (Vehicle vehicle : vehicles.values()) {
			Marker marker = vehicle.getMarker();
			
			if (marker != null)
				toggleMarkerVisibility(marker, visible);
        }
	}
	
	public static void toggleStopsVisibility(Boolean visible) {
		Map<Integer, Stop> stops = StopsContainer.getStops();
		
    	if (stops == null || stops.isEmpty())
    		return;
    	
        for (Stop stop : stops.values()) {
			Marker marker = stop.getMarker();
			
			if (marker != null)
				toggleMarkerVisibility(marker, visible);
        }
	}
	
	private static Bitmap createVehicleLabel(String line, String dest) {
		Drawable drawable = new TextDrawable(line, dest);
		final Bitmap bitmap = Bitmap.createBitmap(200, 60, Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, 200, 60);
		drawable.draw(canvas);
		
		return bitmap;
	}
	
	private static void updateMarkerPosition(final Marker marker, final LatLng point) {
		activity.runOnUiThread(new Runnable() {
    	    public void run() {
    	    	marker.setPosition(point);
    	    }
    	});
	}
	
	private static void toggleMarkerVisibility(final Marker marker, final Boolean visible) {
		activity.runOnUiThread(new Runnable() {
    	    public void run() {
    	    	marker.setVisible(visible);
    	    }
    	});
	}
	
	private static void removeMarker(final Marker marker) {
		activity.runOnUiThread(new Runnable() {
    	    public void run() {
    	    	marker.remove();
    	    }
    	});
	}
	
	private static void createVehicleMarker(final LatLng point, final Vehicle vehicle, final String dest, final Bitmap label) {
		activity.runOnUiThread(new Runnable() {
    	    public void run() {	
    	    	Marker marker = mMap.addMarker(new MarkerOptions()
            	.position(point)
            	.anchor((float) 0.0, (float) 0.0)
            	.title(vehicle.getLine() + " [" + vehicle.getId() + "]")
                .snippet(dest)
                .icon(BitmapDescriptorFactory.fromBitmap(label))
                .visible(sharedPref.getBoolean("show_vehicles", true)));
    	    	
    	    	VehiclesContainer.getVehicle(vehicle.getId()).setMarker(marker);
    	    }
    	});
	}
	
	private static void createStopMarker(final LatLng point, final Stop stop) {
		activity.runOnUiThread(new Runnable() {
    	    public void run() {
    	    	int icon = R.drawable.bus;
    	    	
    	    	if (stop.getType().equals("T")) {
    	    		icon = R.drawable.tram;
    	    	}
    	    	
    	    	Marker marker = mMap.addMarker(new MarkerOptions()
            	.position(point)
            	.anchor((float) 0.0, (float) 0.0)
            	.title(stop.getName())
                .icon(BitmapDescriptorFactory.fromResource(icon))
                .visible(sharedPref.getBoolean("show_stops", true)));
    	    	
    	    	StopsContainer.getStop(stop.getId()).setMarker(marker);
    	    }
    	});
	}
}
