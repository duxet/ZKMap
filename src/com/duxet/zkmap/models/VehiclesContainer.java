package com.duxet.zkmap.models;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("UseSparseArrays")
public class VehiclesContainer {
	
	private static Map<Integer, Vehicle> mVehicles = new HashMap<Integer, Vehicle>();

	public VehiclesContainer() {
	}
	
	public static void addVehicle(Integer id, Vehicle vehicle) {
		mVehicles.put(id, vehicle);
	}
	
	public static Vehicle getVehicle(Integer id) {
		return mVehicles.get(id);
	}

	public static Map<Integer, Vehicle> getVehicles() {
		return mVehicles;
	}
	
	public static void clear() {
		mVehicles.clear();
	}
}
