package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RidesItem{

	@SerializedName("brandName")
	@Expose
	private String brandName;

	@SerializedName("pickUpLong")
	@Expose
	private String pickUpLong;

	@SerializedName("carName")
	@Expose
	private String carName;

	@SerializedName("typeName")
	@Expose
	private String typeName;

	@SerializedName("createdDateTime")
	@Expose
	private String createdDateTime;

	@SerializedName("dropOffLong")
	@Expose
	private String dropOffLong;

	@SerializedName("rideId")
	@Expose
	private int rideId;

	@SerializedName("driverProfileImage")
	@Expose
	private String driverProfileImage;

	@SerializedName("pickUpLat")
	@Expose
	private String pickUpLat;

	@SerializedName("dropOffLocation")
	@Expose
	private String dropOffLocation;

	@SerializedName("driverId")
	@Expose
	private String driverId;

	@SerializedName("driverFirstName")
	@Expose
	private String driverFirstName;

	@SerializedName("driverLastName")
	@Expose
	private String driverLastName;

	@SerializedName("typeImage")
	@Expose
	private String typeImage;

	@SerializedName("pickUpLocation")
	@Expose
	private String pickUpLocation;

	@SerializedName("dropOffLat")
	@Expose
	private String dropOffLat;

	@SerializedName("status")
	@Expose
	private String status;

	@SerializedName("rejectedBy")
	@Expose
	private String rejectedBy;

	public void setBrandName(String brandName){
		this.brandName = brandName;
	}

	public String getBrandName(){
		return brandName;
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

	public void setTypeName(String typeName){
		this.typeName = typeName;
	}

	public String getTypeName(){
		return typeName;
	}

	public void setCreatedDateTime(String createdDateTime){
		this.createdDateTime = createdDateTime;
	}

	public String getCreatedDateTime(){
		return createdDateTime;
	}

	public void setDropOffLong(String dropOffLong){
		this.dropOffLong = dropOffLong;
	}

	public String getDropOffLong(){
		return dropOffLong;
	}

	public void setRideId(int rideId){
		this.rideId = rideId;
	}

	public int getRideId(){
		return rideId;
	}

	public void setDriverProfileImage(String driverProfileImage){
		this.driverProfileImage = driverProfileImage;
	}

	public String getDriverProfileImage(){
		return driverProfileImage;
	}

	public void setPickUpLat(String pickUpLat){
		this.pickUpLat = pickUpLat;
	}

	public String getPickUpLat(){
		return pickUpLat;
	}

	public void setDropOffLocation(String dropOffLocation){
		this.dropOffLocation = dropOffLocation;
	}

	public String getDropOffLocation(){
		return dropOffLocation;
	}

	public void setDriverId(String driverId){
		this.driverId = driverId;
	}

	public String getDriverId(){
		return driverId;
	}

	public void setDriverFirstName(String driverFirstName){
		this.driverFirstName = driverFirstName;
	}

	public String getDriverFirstName(){
		return driverFirstName;
	}

	public void setDriverLastName(String driverLastName){
		this.driverLastName = driverLastName;
	}

	public String getDriverLastName(){
		return driverLastName;
	}

	public void setTypeImage(String typeImage){
		this.typeImage = typeImage;
	}

	public String getTypeImage(){
		return typeImage;
	}

	public void setPickUpLocation(String pickUpLocation){
		this.pickUpLocation = pickUpLocation;
	}

	public String getPickUpLocation(){
		return pickUpLocation;
	}

	public void setDropOffLat(String dropOffLat){
		this.dropOffLat = dropOffLat;
	}

	public String getDropOffLat(){
		return dropOffLat;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}

	public String getRejectedBy() {
		return rejectedBy;
	}

	public void setRejectedBy(String rejectedBy) {
		this.rejectedBy = rejectedBy;
	}
}