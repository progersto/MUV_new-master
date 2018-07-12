package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubCarTypeItem{

	@SerializedName("subTypeName")
	@Expose
	private String subTypeName;

	@SerializedName("id")
	@Expose
	private int id;

	@SerializedName("subTypeImage")
	@Expose
	private String subTypeImage;

	public Boolean isSelected = false;

	public void setSubTypeName(String subTypeName){
		this.subTypeName = subTypeName;
	}

	public String getSubTypeName(){
		return subTypeName;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public void setSubTypeImage(String subTypeImage){
		this.subTypeImage = subTypeImage;
	}

	public String getSubTypeImage(){
		return subTypeImage;
	}

	public Boolean getSelected() {
		return isSelected;
	}

	public void setSelected(Boolean selected) {
		isSelected = selected;
	}
}