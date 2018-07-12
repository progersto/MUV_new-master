package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FareSummary{

	@SerializedName("extraKm")
	@Expose
	private ExtraKm extraKm;

	@SerializedName("baseFare")
	@Expose
	private BaseFare baseFare;

	@SerializedName("carBrand")
	@Expose
	private String carBrand;

	@SerializedName("dropoffLat")
	@Expose
	private String dropoffLat;

	@SerializedName("pickUpLong")
	@Expose
	private String pickUpLong;

	@SerializedName("carName")
	@Expose
	private String carName;

	@SerializedName("dropOffLong")
	@Expose
	private String dropOffLong;

	@SerializedName("FinalAmount")
	@Expose
	private FinalAmount finalAmount;

	@SerializedName("carNumber")
	@Expose
	private String carNumber;

	@SerializedName("dropOffLocation")
	@Expose
	private String dropOffLocation;

	@SerializedName("carTypeImage")
	@Expose
	private String carTypeImage;

	@SerializedName("timeTaken")
	@Expose
	private TimeTaken timeTaken;

	@SerializedName("pickupLat")
	@Expose
	private String pickupLat;

	@SerializedName("pickUpLocation")
	@Expose
	private String pickUpLocation;

	@SerializedName("carTypeName")
	@Expose
	private String carTypeName;

	public void setExtraKm(ExtraKm extraKm){
		this.extraKm = extraKm;
	}

	public ExtraKm getExtraKm(){
		return extraKm;
	}

	public void setBaseFare(BaseFare baseFare){
		this.baseFare = baseFare;
	}

	public BaseFare getBaseFare(){
		return baseFare;
	}

	public void setCarBrand(String carBrand){
		this.carBrand = carBrand;
	}

	public String getCarBrand(){
		return carBrand;
	}

	public void setDropoffLat(String dropoffLat){
		this.dropoffLat = dropoffLat;
	}

	public String getDropoffLat(){
		return dropoffLat;
	}

	public void setPickUpLong(String pickUpLong){
		this.pickUpLong = pickUpLong;
	}

	public String getPickUpLong(){
		return pickUpLong;
	}

	public void setCarName(String carName){
		this.carName = carName;
	}

	public String getCarName(){
		return carName;
	}

	public void setDropOffLong(String dropOffLong){
		this.dropOffLong = dropOffLong;
	}

	public String getDropOffLong(){
		return dropOffLong;
	}

	public void setFinalAmount(FinalAmount finalAmount){
		this.finalAmount = finalAmount;
	}

	public FinalAmount getFinalAmount(){
		return finalAmount;
	}

	public void setCarNumber(String carNumber){
		this.carNumber = carNumber;
	}

	public String getCarNumber(){
		return carNumber;
	}

	public void setDropOffLocation(String dropOffLocation){
		this.dropOffLocation = dropOffLocation;
	}

	public String getDropOffLocation(){
		return dropOffLocation;
	}

	public void setCarTypeImage(String carTypeImage){
		this.carTypeImage = carTypeImage;
	}

	public String getCarTypeImage(){
		return carTypeImage;
	}

	public void setTimeTaken(TimeTaken timeTaken){
		this.timeTaken = timeTaken;
	}

	public TimeTaken getTimeTaken(){
		return timeTaken;
	}

	public void setPickupLat(String pickupLat){
		this.pickupLat = pickupLat;
	}

	public String getPickupLat(){
		return pickupLat;
	}

	public void setPickUpLocation(String pickUpLocation){
		this.pickUpLocation = pickUpLocation;
	}

	public String getPickUpLocation(){
		return pickUpLocation;
	}

	public void setCarTypeName(String carTypeName){
		this.carTypeName = carTypeName;
	}

	public String getCarTypeName(){
		return carTypeName;
	}
}