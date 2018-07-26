package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AccountSettingsItem{

	@SerializedName("notifyAns")
	@Expose
	private String notifyAns;

	@SerializedName("notifyType")
	@Expose
	private String notifyType;

	public void setNotifyAns(String notifyAns){
		this.notifyAns = notifyAns;
	}

	public String getNotifyAns(){
		return notifyAns;
	}

	public void setNotifyType(String notifyType){
		this.notifyType = notifyType;
	}

	public String getNotifyType(){
		return notifyType;
	}
}