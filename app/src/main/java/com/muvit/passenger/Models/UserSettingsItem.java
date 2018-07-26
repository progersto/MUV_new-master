package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserSettingsItem {

	@SerializedName("paypalEmail")
	@Expose
	private String paypalEmail;

	@SerializedName("panicNumber")
	@Expose
	private String panicNumber;

	@SerializedName("accountSettings")
	@Expose
	private List<AccountSettingsItem> accountSettings;

	public void setPaypalEmail(String paypalEmail){
		this.paypalEmail = paypalEmail;
	}

	public String getPaypalEmail(){
		return paypalEmail;
	}

	public void setAccountSettings(List<AccountSettingsItem> accountSettings){
		this.accountSettings = accountSettings;
	}

	public List<AccountSettingsItem> getAccountSettings(){
		return accountSettings;
	}

	public String getPanicNumber() {
		return panicNumber;
	}

	public void setPanicNumber(String panicNumber) {
		this.panicNumber = panicNumber;
	}
}