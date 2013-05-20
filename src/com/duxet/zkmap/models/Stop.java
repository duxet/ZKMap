package com.duxet.zkmap.models;

import com.google.android.gms.maps.model.Marker;

public class Stop {
	
	private Integer id;
	private String name;
	private String type;
	private Float lat;
	private Float lon;
	private Marker marker;

	public Stop(Integer id, String name, String type, Float lat, Float lon) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.lat = lat;
		this.lon = lon;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Float getLat() {
		return lat;
	}
	
	public void setLat(Float lat) {
		this.lat = lat;
	}
	
	public Float getLon() {
		return lon;
	}
	
	public void setLon(Float lon) {
		this.lon = lon;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}
	
}
