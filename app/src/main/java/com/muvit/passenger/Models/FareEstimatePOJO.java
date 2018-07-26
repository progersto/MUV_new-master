package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FareEstimatePOJO {

	@SerializedName("dataAns")
	@Expose
	private List<FareEstimateItem> fareSummary;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setFareSummary(List<FareEstimateItem> fareSummary){
		this.fareSummary = fareSummary;
	}

	public List<FareEstimateItem> getFareSummary(){
		return fareSummary;
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