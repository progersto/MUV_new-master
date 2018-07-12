package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RedeemHistoryPOJO {

	@SerializedName("totalRecords")
	@Expose
	private int totalRecords;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("dataAns")
	@Expose
	private List<RedeemHistoryItem> redeemHistory;

	@SerializedName("status")
	@Expose
	private boolean status;

	public int getTotalRecords(){
		return totalRecords;
	}

	public String getMessage(){
		return message;
	}

	public List<RedeemHistoryItem> getRedeemHistory(){
		return redeemHistory;
	}

	public boolean isStatus(){
		return status;
	}
}