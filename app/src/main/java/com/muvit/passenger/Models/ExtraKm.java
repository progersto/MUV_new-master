package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ExtraKm{

	@SerializedName("totalExtraKm")
	@Expose
	private String totalExtraKm;

	@SerializedName("perKmPrice")
	@Expose
	private String perKmPrice;

	public void setTotalExtraKm(String totalExtraKm){
		this.totalExtraKm = totalExtraKm;
	}

	public String getTotalExtraKm(){
		return totalExtraKm;
	}

	public void setPerKmPrice(String perKmPrice){
		this.perKmPrice = perKmPrice;
	}

	public String getPerKmPrice(){
		return perKmPrice;
	}
}