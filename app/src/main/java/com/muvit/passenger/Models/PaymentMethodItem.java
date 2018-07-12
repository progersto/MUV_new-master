package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PaymentMethodItem{

	@SerializedName("defaultPaymentMethod")
	@Expose
	private String defaultPaymentMethod;

	public void setDefaultPaymentMethod(String defaultPaymentMethod){
		this.defaultPaymentMethod = defaultPaymentMethod;
	}

	public String getDefaultPaymentMethod(){
		return defaultPaymentMethod;
	}
}