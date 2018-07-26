package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WalletItem{

	@SerializedName("depositFund")
	@Expose
	private String depositFund;

	@SerializedName("currenctBalance")
	@Expose
	private String currenctBalance;

	@SerializedName("redeemRequest")
	@Expose
	private String redeemRequest;

	public void setDepositFund(String depositFund){
		this.depositFund = depositFund;
	}

	public String getDepositFund(){
		return depositFund;
	}

	public void setCurrenctBalance(String currenctBalance){
		this.currenctBalance = currenctBalance;
	}

	public String getCurrenctBalance(){
		return currenctBalance;
	}

	public void setRedeemRequest(String redeemRequest){
		this.redeemRequest = redeemRequest;
	}

	public String getRedeemRequest(){
		return redeemRequest;
	}
}