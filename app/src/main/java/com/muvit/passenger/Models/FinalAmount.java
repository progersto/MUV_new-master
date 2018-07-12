package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FinalAmount{

	@SerializedName("payByWallet")
	@Expose
	private String payByWallet;

	@SerializedName("finalTotalRidePrice")
	@Expose
	private String finalTotalRidePrice;

	@SerializedName("totalKm")
	@Expose
	private String totalKm;

	@SerializedName("payByCash")
	@Expose
	private String payByCash;

	public void setPayByWallet(String payByWallet){
		this.payByWallet = payByWallet;
	}

	public String getPayByWallet(){
		return payByWallet;
	}

	public void setFinalTotalRidePrice(String finalTotalRidePrice){
		this.finalTotalRidePrice = finalTotalRidePrice;
	}

	public String getFinalTotalRidePrice(){
		return finalTotalRidePrice;
	}

	public void setTotalKm(String totalKm){
		this.totalKm = totalKm;
	}

	public String getTotalKm(){
		return totalKm;
	}

	public void setPayByCash(String payByCash){
		this.payByCash = payByCash;
	}

	public String getPayByCash(){
		return payByCash;
	}
}