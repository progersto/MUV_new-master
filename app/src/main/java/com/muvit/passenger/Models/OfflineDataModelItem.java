package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OfflineDataModelItem{

	@SerializedName("getnotification")
	@Expose
	private Getnotification getnotification;

	@SerializedName("getuserwalletdetails")
	@Expose
	private Getuserwalletdetails getuserwalletdetails;

	@SerializedName("getusertriplist")
	@Expose
	private Getusertriplist getusertriplist;

	@SerializedName("getuserprofile")
	@Expose
	private Getuserprofile getuserprofile;

	public Getnotification getGetnotification(){
		return getnotification;
	}

	public Getuserwalletdetails getGetuserwalletdetails(){
		return getuserwalletdetails;
	}

	public Getusertriplist getGetusertriplist(){
		return getusertriplist;
	}

	public Getuserprofile getGetuserprofile(){
		return getuserprofile;
	}
}