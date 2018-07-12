package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ContactUsPOJO{

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("lastInsertedId")
	@Expose
	private int lastInsertedId;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setLastInsertedId(int lastInsertedId){
		this.lastInsertedId = lastInsertedId;
	}

	public int getLastInsertedId(){
		return lastInsertedId;
	}

	public void setStatus(boolean status){
		this.status = status;
	}

	public boolean isStatus(){
		return status;
	}
}