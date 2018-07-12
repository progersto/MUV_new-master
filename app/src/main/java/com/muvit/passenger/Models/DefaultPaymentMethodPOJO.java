package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DefaultPaymentMethodPOJO{

	@SerializedName("dataAns")
	@Expose
	private List<PaymentMethodItem> paymentMethod;

	@SerializedName("message")
	@Expose
	private String message;

	@SerializedName("status")
	@Expose
	private boolean status;

	public void setPaymentMethod(List<PaymentMethodItem> paymentMethod){
		this.paymentMethod = paymentMethod;
	}

	public List<PaymentMethodItem> getPaymentMethod(){
		return paymentMethod;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setStatus(boolean status){
		this.status = status;
	}

	public boolean isStatus(){
		return status;
	}
}