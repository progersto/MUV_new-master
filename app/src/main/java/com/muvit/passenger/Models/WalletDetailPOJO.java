package com.muvit.passenger.Models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WalletDetailPOJO{

	@SerializedName("dataAns")
	@Expose
	private List<WalletItem> wallet;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setWallet(List<WalletItem> wallet){
		this.wallet = wallet;
	}

	public List<WalletItem> getWallet(){
		return wallet;
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