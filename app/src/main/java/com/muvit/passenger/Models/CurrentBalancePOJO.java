package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CurrentBalancePOJO{

	@SerializedName("dataAns")
	@Expose
	private List<BalanceItem> balance;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setBalance(List<BalanceItem> balance){
		this.balance = balance;
	}

	public List<BalanceItem> getBalance(){
		return balance;
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