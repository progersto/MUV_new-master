package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConfirmRideItem{

	@SerializedName("tripId")
	@Expose
	private int tripId;

	public void setTripId(int tripId){
		this.tripId = tripId;
	}

	public int getTripId(){
		return tripId;
	}
}