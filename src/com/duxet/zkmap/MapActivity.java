package com.duxet.zkmap;

import java.util.Timer;
import java.util.TimerTask;

import com.duxet.zkmap.controllers.*;
import com.duxet.zkmap.models.*;
import com.duxet.zkmap.remote.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

public class MapActivity  extends android.support.v4.app.FragmentActivity {
	private LocationManager locationManager;
	private SharedPreferences sharedPref;
	private GoogleMap mMap;
	private UiSettings mUiSettings;

	private Boolean isPaused;
	private TimerTask refreshTask;
	private Timer refreshTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        setUpMapIfNeeded();

        this.isPaused = false;
        
        // Pass context and load routes from file
        Routes.setContext(getApplicationContext());
        Routes.loadRoutes();
        
        // Add save task which will be triggered after 30s
        TimerTask saveRoutesTask = new TimerTask() {
            public void run() {
            	Routes.saveRoutes();
            }
        };
        
        // Create timer and schedule that task
        Timer startupTimer = new Timer();
        startupTimer.schedule(saveRoutesTask, 30000);
        
        // Initialize connection with ZKM in new thread
        new initialize().execute();

        // Add timer for refreshing vehicles data
        refreshTimer = new Timer();
        refreshTask = new updateMarkers();
        
        if(sharedPref.getBoolean("pref_refresh", true))
        	refreshTimer.scheduleAtFixedRate(refreshTask, 3000, Integer.parseInt(sharedPref.getString("pref_refreshTime", "10")) * 1000);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	if(!hasFocus)
    		this.isPaused = true;
    	else
    		this.isPaused = false;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        
        menu.findItem(R.id.stops).setChecked(sharedPref.getBoolean("show_stops", true));
        menu.findItem(R.id.vehicles).setChecked(sharedPref.getBoolean("show_vehicles", true));
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
            	refreshTask.cancel();
            	refreshTimer.purge();
            	refreshTask = new updateMarkers();
            	
            	if(sharedPref.getBoolean("pref_refresh", true))
            		refreshTimer.scheduleAtFixedRate(refreshTask, 0, Integer.parseInt(sharedPref.getString("pref_refreshTime", "10")) * 1000);
            	else
            		refreshTimer.schedule(refreshTask, 0);
            	
            	break;
            case R.id.settings:
            	Intent intent = new Intent(this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
                
            case R.id.stops:
            	Boolean stopsVisibility = !sharedPref.getBoolean("show_stops", true);
            	sharedPref.edit().putBoolean("show_stops", stopsVisibility).commit();
            	item.setChecked(stopsVisibility);
            	MapController.toggleStopsVisibility(stopsVisibility);
            	break;
            	
            case R.id.vehicles:
            	Boolean vehiclesVisibility = !sharedPref.getBoolean("show_vehicles", true);
            	sharedPref.edit().putBoolean("show_vehicles", vehiclesVisibility).commit();
            	item.setChecked(vehiclesVisibility);
            	MapController.toggleVehiclesVisibility(vehiclesVisibility);
            	break;
            	
            default:
                return super.onOptionsItemSelected(item);
        }
        
        return true;
    }
    
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            
            if (mMap != null) {
            	MapController.setup(mMap, this, sharedPref);
                setUpMap();
            }
        }
    }
    
    private void setUpMap() {
    	Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    	
    	if (location != null) {
    		LatLng userPos = new LatLng(location.getLatitude(), location.getLongitude());
    		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPos, 14));
    	} else {
    		LatLng gdansk = new LatLng(54.351887, 18.646401);
        	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gdansk, 14));
    	}
    	
        mMap.setMyLocationEnabled(true);
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(true);
    }
    
    private class updateMarkers extends TimerTask {
        @Override
        public void run() {        	
        	if(isPaused)
        		return;

        	MapController.updateVehicles();
        }
    }
    
    private class initialize extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			ZKM.initialize();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			MapController.updateStops();
		}

    }
}
