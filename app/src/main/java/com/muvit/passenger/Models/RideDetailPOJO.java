package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RideDetailPOJO{

	@SerializedName("dataAns")
	@Expose
	private RideDetail rideDetail;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setRideDetail(RideDetail rideDetail){
		this.rideDetail = rideDetail;
	}

	public RideDetail getRideDetail(){
		return rideDetail;
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