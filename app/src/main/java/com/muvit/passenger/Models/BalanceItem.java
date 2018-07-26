package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BalanceItem{

	@SerializedName("currenctBalance")
	@Expose
	private String currenctBalance;

	public void setCurrenctBalance(String currenctBalance){
		this.currenctBalance = currenctBalance;
	}

	public String getCurrenctBalance(){
		return currenctBalance;
	}
}