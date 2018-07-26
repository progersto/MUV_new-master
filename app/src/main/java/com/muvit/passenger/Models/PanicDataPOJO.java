package com.muvit.passenger.Models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PanicDataPOJO{

	@SerializedName("dataAns")
	@Expose
	private List<PanicDataItem> panicData;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setPanicData(List<PanicDataItem> panicData){
		this.panicData = panicData;
	}

	public List<PanicDataItem> getPanicData(){
		return panicData;
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