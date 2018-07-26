package com.muvit.passenger.Models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginPOJO{

	@SerializedName("dataAns")
	@Expose
	private List<DataAnsItem> dataAns;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setDataAns(List<DataAnsItem> dataAns){
		this.dataAns = dataAns;
	}

	public List<DataAnsItem> getDataAns(){
		return dataAns;
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