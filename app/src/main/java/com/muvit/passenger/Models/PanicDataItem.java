package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PanicDataItem{

	@SerializedName("userPanicNo")
	@Expose
	private String userPanicNo;

	public void setUserPanicNo(String userPanicNo){
		this.userPanicNo = userPanicNo;
	}

	public String getUserPanicNo(){
		return userPanicNo;
	}
}