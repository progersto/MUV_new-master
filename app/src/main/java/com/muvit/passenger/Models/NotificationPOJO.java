package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotificationPOJO{

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("dataAns")
	@Expose
	private List<Notification> notifications;

	@SerializedName("status")
	@Expose
	private boolean status;

	@SerializedName("totalRecords")
	@Expose
	private int totalRecords;

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setNotifications(List<Notification> notifications){
		this.notifications = notifications;
	}

	public List<Notification> getNotifications(){
		return notifications;
	}

	public void setStatus(boolean status){
		this.status = status;
	}

	public boolean isStatus(){
		return status;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

}