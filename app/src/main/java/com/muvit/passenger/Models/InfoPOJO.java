package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InfoPOJO{

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("dataAns")
	@Expose
	private List<InfoItem> info;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setInfo(List<InfoItem> info){
		this.info = info;
	}

	public List<InfoItem> getInfo(){
		return info;
	}

	public void setStatus(boolean status){
		this.status = status;
	}

	public boolean isStatus(){
		return status;
	}
}