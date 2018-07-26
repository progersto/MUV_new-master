package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WebContentItem{

	@SerializedName("html_content")
	@Expose
	private String htmlContent;

	@SerializedName("title")
	@Expose
	private String title;

	public void setHtmlContent(String htmlContent){
		this.htmlContent = htmlContent;
	}

	public String getHtmlContent(){
		return htmlContent;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return title;
	}
}