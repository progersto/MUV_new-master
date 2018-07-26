package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TripTrackPOJO{

	@SerializedName("dataAns")
	@Expose
	private TripTrack tripTrack;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setTripTrack(TripTrack tripTrack){
		this.tripTrack = tripTrack;
	}

	public TripTrack getTripTrack(){
		return tripTrack;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setStatus(boolean status){
		this.status = status;
	}

	public boolean isStatus(){
		return status;
	}
}