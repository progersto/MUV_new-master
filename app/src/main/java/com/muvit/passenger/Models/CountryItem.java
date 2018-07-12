package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CountryItem{

	@SerializedName("country_code")
	@Expose
	private String countryCode;

	@SerializedName("id")
	@Expose
	private int id;

	@SerializedName("isActive")
	@Expose
	private String isActive;

	public void setCountryCode(String countryCode){
		this.countryCode = countryCode;
	}

	public String getCountryCode(){
		return countryCode;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public void setIsActive(String isActive){
		this.isActive = isActive;
	}

	public String getIsActive(){
		return isActive;
	}

	@Override
	public String toString() {
		return countryCode;
	}
}