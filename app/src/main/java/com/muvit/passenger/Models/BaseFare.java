package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BaseFare{

	@SerializedName("totalFareAmount")
	@Expose
	private String totalFareAmount;

	@SerializedName("perKmAmount")
	@Expose
	private String perKmAmount;

	public void setTotalFareAmount(String totalFareAmount){
		this.totalFareAmount = totalFareAmount;
	}

	public String getTotalFareAmount(){
		return totalFareAmount;
	}

	public void setPerKmAmount(String perKmAmount){
		this.perKmAmount = perKmAmount;
	}

	public String getPerKmAmount(){
		return perKmAmount;
	}
}