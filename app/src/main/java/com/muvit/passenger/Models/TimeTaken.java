package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TimeTaken{

	@SerializedName("totalTime")
	@Expose
	private String totalTime;

	@SerializedName("perMinFareAmount")
	@Expose
	private String perMinFareAmount;

	public void setTotalTime(String totalTime){
		this.totalTime = totalTime;
	}

	public String getTotalTime(){
		return totalTime;
	}

	public void setPerMinFareAmount(String perMinFareAmount){
		this.perMinFareAmount = perMinFareAmount;
	}

	public String getPerMinFareAmount(){
		return perMinFareAmount;
	}
}