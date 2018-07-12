package com.muvit.passenger.Models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserLocationPOJO{

	@SerializedName("dataAns")
	@Expose
	private List<UserLocationItem> userLocation;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setUserLocation(List<UserLocationItem> userLocation){
		this.userLocation = userLocation;
	}

	public List<UserLocationItem> getUserLocation(){
		return userLocation;
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