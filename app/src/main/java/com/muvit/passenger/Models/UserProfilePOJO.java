package com.muvit.passenger.Models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserProfilePOJO{

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("dataAns")
	@Expose
	private List<ProfileItem> profile;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setProfile(List<ProfileItem> profile){
		this.profile = profile;
	}

	public List<ProfileItem> getProfile(){
		return profile;
	}

	public void setStatus(boolean status){
		this.status = status;
	}

	public boolean isStatus(){
		return status;
	}
}