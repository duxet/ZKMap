package com.duxet.zkmap.models;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("UseSparseArrays")
public class StopsContainer {
	
	private static Map<Integer, Stop> mStops = new HashMap<Integer, Stop>();

	public StopsContainer() {
	}
	
	public static void addStop(Integer id, Stop stop) {
		mStops.put(id, stop);
	}
	
	public static Stop getStop(Integer id) {
		return mStops.get(id);
	}

	public static Map<Integer, Stop> getStops() {
		return mStops;
	}
	
	public static void clear() {
		mStops.clear();
	}
}
