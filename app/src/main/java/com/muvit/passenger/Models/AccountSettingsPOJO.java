package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AccountSettingsPOJO{

	@SerializedName("dataAns")
	@Expose
	private List<UserSettingsItem> userSettings;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;



	public void setUserSettings(List<UserSettingsItem> userSettings){
		this.userSettings = userSettings;
	}

	public List<UserSettingsItem> getUserSettings(){
		return userSettings;
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