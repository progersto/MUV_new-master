package com.muvit.passenger.Models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Getusertriplist{

	@SerializedName("dataAns")
	@Expose
	private List<RidesItem> dataAns;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public List<RidesItem> getDataAns(){
		return dataAns;
	}

	public String getMessage(){
		return message;
	}

	public boolean isStatus(){
		return status;
	}
}