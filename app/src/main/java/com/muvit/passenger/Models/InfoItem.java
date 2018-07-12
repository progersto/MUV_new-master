package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InfoItem{

	@SerializedName("constant")
	@Expose
	private String constant;

	@SerializedName("id")
	@Expose
	private int id;

	public void setConstant(String constant){
		this.constant = constant;
	}

	public String getConstant(){
		return constant;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}
}