package com.duxet.zkmap.models;

import com.google.android.gms.maps.model.Marker;

public class Vehicle {
	private Integer id;
	private String line;
	private String lineType;
	private Integer lat;
	private Integer lon;
	private Long lastUpdate;
	private Marker marker;
	
	public Vehicle(Integer id, String line, String lineType, Integer lat, Integer lon) {
		this.id = id;
		this.line = line;
		this.lineType = lineType;
		this.lat = lat;
		this.lon = lon;
		this.lastUpdate = System.currentTimeMillis()/1000;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLine() {
		return line;
	}
	
	public String getLineType() {
		return lineType;
	}
	
	public void setLine(String line, String lineType) {
		this.line = line;
		this.lineType = lineType;
	}
	
	public Integer getLat() {
		return lat;
	}
	
	public Integer getLon() {
		return lon;
	}
	
	public void updatePosition(Integer newLat, Integer newLon) {
		this.lat = newLat;
		this.lon = newLon;
		this.lastUpdate = System.currentTimeMillis()/1000;
	}

	public Long getLastUpdate() {
		return lastUpdate;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}
}
