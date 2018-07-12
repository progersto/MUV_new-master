package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TripTrack{

	@SerializedName("ridePathString")
	@Expose
	private String ridePathString;

	@SerializedName("pickUpLong")
	@Expose
	private String pickUpLong;

	@SerializedName("dropOffLong")
	@Expose
	private String dropOffLong;

	@SerializedName("pickUpLat")
	@Expose
	private String pickUpLat;

	@SerializedName("dropOffLat")
	@Expose
	private String dropOffLat;

	public void setRidePathString(String ridePathString){
		this.ridePathString = ridePathString;
	}

	public String getRidePathString(){
		return ridePathString;
	}

	public void setPickUpLong(String pickUpLong){
		this.pickUpLong = pickUpLong;
	}

	public String getPickUpLong(){
		return pickUpLong;
	}

	public void setDropOffLong(String dropOffLong){
		this.dropOffLong = dropOffLong;
	}

	public String getDropOffLong(){
		return dropOffLong;
	}

	public void setPickUpLat(String pickUpLat){
		this.pickUpLat = pickUpLat;
	}

	public String getPickUpLat(){
		return pickUpLat;
	}

	public void setDropOffLat(String dropOffLat){
		this.dropOffLat = dropOffLat;
	}

	public String getDropOffLat(){
		return dropOffLat;
	}
}