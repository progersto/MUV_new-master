package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubCarTypePOJO{

	@SerializedName("dataAns")
	@Expose
	private List<SubCarTypeItem> subCarType;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setSubCarType(List<SubCarTypeItem> subCarType){
		this.subCarType = subCarType;
	}

	public List<SubCarTypeItem> getSubCarType(){
		return subCarType;
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