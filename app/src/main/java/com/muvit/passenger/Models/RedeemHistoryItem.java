package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RedeemHistoryItem {

	@SerializedName("emailAddress")
	@Expose
	private String emailAddress;

	@SerializedName("amount")
	@Expose
	private String amount;

	@SerializedName("description")
	@Expose
	private String description;

	@SerializedName("createdDateTime")
	@Expose
	private String createdDateTime;

	@SerializedName("id")
	@Expose
	private int id;

	@SerializedName("status")
	@Expose
	private String status;

	public String getEmailAddress(){
		return emailAddress;
	}

	public String getAmount(){
		return amount;
	}

	public String getDescription(){
		return description;
	}

	public String getCreatedDateTime(){
		return createdDateTime;
	}

	public int getId(){
		return id;
	}

	public String getStatus(){
		return status;
	}
}