package com.muvit.passenger.Models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Response{

	@SerializedName("dataAns")
	@Expose
	private List<OfflineDataModelItem> offlineDataModel;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public List<OfflineDataModelItem> getOfflineDataModel(){
		return offlineDataModel;
	}

	public String getMessage(){
		return message;
	}

	public boolean isStatus(){
		return status;
	}
}