package com.muvit.passenger.Models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConfirmRidePOJO{

	@SerializedName("dataAns")
	@Expose
	private List<ConfirmRideItem> confirmRide;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setConfirmRide(List<ConfirmRideItem> confirmRide){
		this.confirmRide = confirmRide;
	}

	public List<ConfirmRideItem> getConfirmRide(){
		return confirmRide;
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