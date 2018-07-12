package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Notification {

	@SerializedName("notifyType")
	@Expose
	private String notifyType;

	@SerializedName("notifyCategory")
	@Expose
	private String notifyCategory;

	@SerializedName("notifyMessage")
	@Expose
	private String notifyMessage;

	@SerializedName("user2Id")
	@Expose
	private String user2Id;

	@SerializedName("notificationId")
	@Expose
	private String notificationId;

	@SerializedName("rideId")
	@Expose
	private int rideId;

	@SerializedName("notificationDateTime")
	@Expose
	private String notificationDateTime;

	@SerializedName("status")
	@Expose
	private String status;

	public void setNotifyType(String notifyType){
		this.notifyType = notifyType;
	}

	public String getNotifyType(){
		return notifyType;
	}

	public void setNotifyCategory(String notifyCategory){
		this.notifyCategory = notifyCategory;
	}

	public String getNotifyCategory(){
		return notifyCategory;
	}

	public void setNotifyMessage(String notifyMessage){
		this.notifyMessage = notifyMessage;
	}

	public String getNotifyMessage(){
		return notifyMessage;
	}

	public void setUser2Id(String user2Id){
		this.user2Id = user2Id;
	}

	public String getUser2Id(){
		return user2Id;
	}

	public void setNotificationId(String notificationId){
		this.notificationId = notificationId;
	}

	public String getNotificationId(){
		return notificationId;
	}

	public void setRideId(int rideId){
		this.rideId = rideId;
	}

	public int getRideId(){
		return rideId;
	}

	public void setNotificationDateTime(String notificationDateTime){
		this.notificationDateTime = notificationDateTime;
	}

	public String getStatus(){
		return status;
	}

	public void setStatus(String status){
		this.status = status;
	}



	public String getNotificationDateTime(){
		return notificationDateTime;
	}
}