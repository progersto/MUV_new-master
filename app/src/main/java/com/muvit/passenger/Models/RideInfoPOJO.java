package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RideInfoPOJO{

	@SerializedName("dataAns")
	@Expose
	private RideInfo rideInfo;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setRideInfo(RideInfo rideInfo){
		this.rideInfo = rideInfo;
	}

	public RideInfo getRideInfo(){
		return rideInfo;
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