package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserLocationItem{

	@SerializedName("workLocation")
	@Expose
	private String workLocation;

	@SerializedName("homeLat")
	@Expose
	private String homeLat;

	@SerializedName("homeLocation")
	@Expose
	private String homeLocation;

	@SerializedName("workLat")
	@Expose
	private String workLat;

	@SerializedName("homeLong")
	@Expose
	private String homeLong;

	@SerializedName("workLong")
	@Expose
	private String workLong;

	public void setWorkLocation(String workLocation){
		this.workLocation = workLocation;
	}

	public String getWorkLocation(){
		return workLocation;
	}

	public void setHomeLat(String homeLat){
		this.homeLat = homeLat;
	}

	public String getHomeLat(){
		return homeLat;
	}

	public void setHomeLocation(String homeLocation){
		this.homeLocation = homeLocation;
	}

	public String getHomeLocation(){
		return homeLocation;
	}

	public void setWorkLat(String workLat){
		this.workLat = workLat;
	}

	public String getWorkLat(){
		return workLat;
	}

	public void setHomeLong(String homeLong){
		this.homeLong = homeLong;
	}

	public String getHomeLong(){
		return homeLong;
	}

	public void setWorkLong(String workLong){
		this.workLong = workLong;
	}

	public String getWorkLong(){
		return workLong;
	}
}