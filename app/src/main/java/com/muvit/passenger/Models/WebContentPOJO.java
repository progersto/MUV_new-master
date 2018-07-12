package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WebContentPOJO{

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("dataAns")
	@Expose
	private List<WebContentItem> webContent;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setWebContent(List<WebContentItem> webContent){
		this.webContent = webContent;
	}

	public List<WebContentItem> getWebContent(){
		return webContent;
	}

	public void setStatus(boolean status){
		this.status = status;
	}

	public boolean isStatus(){
		return status;
	}
}