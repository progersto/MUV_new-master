package com.muvit.passenger.Models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MyRidesPOJO{

	@SerializedName("message")
	@Expose
	private String message;


	@SerializedName("dataAns")
	@Expose
	private List<RidesItem> rides;

	@SerializedName("status")
	@Expose
	private boolean status;

	@SerializedName("totalRecords")
	@Expose
	private int totalRecords;

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setRides(List<RidesItem> rides){
		this.rides = rides;
	}

	public List<RidesItem> getRides(){
		return rides;
	}

	public void setStatus(boolean status){
		this.status = status;
	}

	public boolean isStatus(){
		return status;
	}

	public int getTotalRecords() {
		return totalRecords;
	}
}