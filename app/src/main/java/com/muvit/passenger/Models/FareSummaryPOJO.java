package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FareSummaryPOJO{

	@SerializedName("dataAns")
	@Expose
	private FareSummary fareSummary;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setFareSummary(FareSummary fareSummary){
		this.fareSummary = fareSummary;
	}

	public FareSummary getFareSummary(){
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