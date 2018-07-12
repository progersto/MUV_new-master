package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RideDetail{

	@SerializedName("fareDistanceCharges")
	@Expose
	private String fareDistanceCharges;

	@SerializedName("userProfileImage")
	@Expose
	private String userProfileImage;

	@SerializedName("carName")
	@Expose
	private String carName;

	@SerializedName("typeName")
	@Expose
	private String typeName;

	@SerializedName("rating")
	@Expose
	private String rating;

	@SerializedName("totalFareCharges")
	@Expose
	private String totalFareCharges;

	@SerializedName("dropOffLong")
	@Expose
	private String dropOffLong;

	@SerializedName("rideId")
	@Expose
	private int rideId;

	@SerializedName("driverProfileImage")
	@Expose
	private String driverProfileImage;

	@SerializedName("payByCash")
	@Expose
	private String payByCash;

	@SerializedName("dropOffLocation")
	@Expose
	private String dropOffLocation;

	@SerializedName("feedback")
	@Expose
	private String feedback;

	@SerializedName("totalExtraCharges")
	@Expose
	private String totalExtraCharges;

	@SerializedName("driverLastName")
	@Expose
	private String driverLastName;

	@SerializedName("totalDistance")
	@Expose
	private String totalDistance;

	@SerializedName("dropOffLat")
	@Expose
	private String dropOffLat;

	@SerializedName("payByWallet")
	@Expose
	private String payByWallet;

	@SerializedName("brandName")
	@Expose
	private String brandName;

	@SerializedName("extraDistance")
	@Expose
	private String extraDistance;

	@SerializedName("pickUpLong")
	@Expose
	private String pickUpLong;

	@SerializedName("totalTime")
	@Expose
	private String totalTime;

	@SerializedName("fareTime")
	@Expose
	private String fareTime;

	@SerializedName("totalRating")
	@Expose
	private String totalRating;

	@SerializedName("fareTimeCharges")
	@Expose
	private String fareTimeCharges;

	@SerializedName("mobileNo")
	@Expose
	private String mobileNo;

	@SerializedName("userFirstName")
	@Expose
	private String userFirstName;

	@SerializedName("fareAdditionalCharges")
	@Expose
	private String fareAdditionalCharges;

	@SerializedName("userlastName")
	@Expose
	private String userlastName;

	@SerializedName("fareDistance")
	@Expose
	private String fareDistance;

	@SerializedName("pickUpLat")
	@Expose
	private String pickUpLat;

	@SerializedName("driverCountryCode")
	@Expose
	private String driverCountryCode;

	@SerializedName("carNumber")
	@Expose
	private String carNumber;

	@SerializedName("fareAdditionalKm")
	@Expose
	private String fareAdditionalKm;

	@SerializedName("totalTimeCharges")
	@Expose
	private String totalTimeCharges;

	@SerializedName("driverId")
	@Expose
	private String driverId;

	@SerializedName("driverFirstName")
	@Expose
	private String driverFirstName;

	@SerializedName("avgRatting")
	@Expose
	private String avgRatting;

	@SerializedName("totalCharges")
	@Expose
	private String totalCharges;

	@SerializedName("perMinCharges")
	@Expose
	private String perMinCharges;

	@SerializedName("typeImage")
	@Expose
	private String typeImage;

	@SerializedName("fareAdditionalChargesPerKm")
	@Expose
	private String fareAdditionalChargesPerKm;

	@SerializedName("pickUpLocation")
	@Expose
	private String pickUpLocation;

	@SerializedName("rideDateTime")
	@Expose
	private String rideDateTime;

	@SerializedName("status")
	@Expose
	private String status;

	public void setFareDistanceCharges(String fareDistanceCharges){
		this.fareDistanceCharges = fareDistanceCharges;
	}

	public String getFareDistanceCharges(){
		return fareDistanceCharges;
	}

	public void setUserProfileImage(String userProfileImage){
		this.userProfileImage = userProfileImage;
	}

	public String getUserProfileImage(){
		return userProfileImage;
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

	public void setRating(String rating){
		this.rating = rating;
	}

	public String getRating(){
		return rating;
	}

	public void setTotalFareCharges(String totalFareCharges){
		this.totalFareCharges = totalFareCharges;
	}

	public String getTotalFareCharges(){
		return totalFareCharges;
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

	public void setPayByCash(String payByCash){
		this.payByCash = payByCash;
	}

	public String getPayByCash(){
		return payByCash;
	}

	public void setDropOffLocation(String dropOffLocation){
		this.dropOffLocation = dropOffLocation;
	}

	public String getDropOffLocation(){
		return dropOffLocation;
	}

	public void setFeedback(String feedback){
		this.feedback = feedback;
	}

	public String getFeedback(){
		return feedback;
	}

	public void setTotalExtraCharges(String totalExtraCharges){
		this.totalExtraCharges = totalExtraCharges;
	}

	public String getTotalExtraCharges(){
		return totalExtraCharges;
	}

	public void setDriverLastName(String driverLastName){
		this.driverLastName = driverLastName;
	}

	public String getDriverLastName(){
		return driverLastName;
	}

	public void setTotalDistance(String totalDistance){
		this.totalDistance = totalDistance;
	}

	public String getTotalDistance(){
		return totalDistance;
	}

	public void setDropOffLat(String dropOffLat){
		this.dropOffLat = dropOffLat;
	}

	public String getDropOffLat(){
		return dropOffLat;
	}

	public void setPayByWallet(String payByWallet){
		this.payByWallet = payByWallet;
	}

	public String getPayByWallet(){
		return payByWallet;
	}

	public void setBrandName(String brandName){
		this.brandName = brandName;
	}

	public String getBrandName(){
		return brandName;
	}

	public void setExtraDistance(String extraDistance){
		this.extraDistance = extraDistance;
	}

	public String getExtraDistance(){
		return extraDistance;
	}

	public void setPickUpLong(String pickUpLong){
		this.pickUpLong = pickUpLong;
	}

	public String getPickUpLong(){
		return pickUpLong;
	}

	public void setTotalTime(String totalTime){
		this.totalTime = totalTime;
	}

	public String getTotalTime(){
		return totalTime;
	}

	public void setFareTime(String fareTime){
		this.fareTime = fareTime;
	}

	public String getFareTime(){
		return fareTime;
	}

	public void setTotalRating(String totalRating){
		this.totalRating = totalRating;
	}

	public String getTotalRating(){
		return totalRating;
	}

	public void setFareTimeCharges(String fareTimeCharges){
		this.fareTimeCharges = fareTimeCharges;
	}

	public String getFareTimeCharges(){
		return fareTimeCharges;
	}

	public void setMobileNo(String mobileNo){
		this.mobileNo = mobileNo;
	}

	public String getMobileNo(){
		return mobileNo;
	}

	public void setUserFirstName(String userFirstName){
		this.userFirstName = userFirstName;
	}

	public String getUserFirstName(){
		return userFirstName;
	}

	public void setFareAdditionalCharges(String fareAdditionalCharges){
		this.fareAdditionalCharges = fareAdditionalCharges;
	}

	public String getFareAdditionalCharges(){
		return fareAdditionalCharges;
	}

	public void setUserlastName(String userlastName){
		this.userlastName = userlastName;
	}

	public String getUserlastName(){
		return userlastName;
	}

	public void setFareDistance(String fareDistance){
		this.fareDistance = fareDistance;
	}

	public String getFareDistance(){
		return fareDistance;
	}

	public void setPickUpLat(String pickUpLat){
		this.pickUpLat = pickUpLat;
	}

	public String getPickUpLat(){
		return pickUpLat;
	}

	public void setDriverCountryCode(String driverCountryCode){
		this.driverCountryCode = driverCountryCode;
	}

	public String getDriverCountryCode(){
		return driverCountryCode;
	}

	public void setCarNumber(String carNumber){
		this.carNumber = carNumber;
	}

	public String getCarNumber(){
		return carNumber;
	}

	public void setFareAdditionalKm(String fareAdditionalKm){
		this.fareAdditionalKm = fareAdditionalKm;
	}

	public String getFareAdditionalKm(){
		return fareAdditionalKm;
	}

	public void setTotalTimeCharges(String totalTimeCharges){
		this.totalTimeCharges = totalTimeCharges;
	}

	public String getTotalTimeCharges(){
		return totalTimeCharges;
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

	public void setAvgRatting(String avgRatting){
		this.avgRatting = avgRatting;
	}

	public String getAvgRatting(){
		return avgRatting;
	}

	public void setTotalCharges(String totalCharges){
		this.totalCharges = totalCharges;
	}

	public String getTotalCharges(){
		return totalCharges;
	}

	public void setPerMinCharges(String perMinCharges){
		this.perMinCharges = perMinCharges;
	}

	public String getPerMinCharges(){
		return perMinCharges;
	}

	public void setTypeImage(String typeImage){
		this.typeImage = typeImage;
	}

	public String getTypeImage(){
		return typeImage;
	}

	public void setFareAdditionalChargesPerKm(String fareAdditionalChargesPerKm){
		this.fareAdditionalChargesPerKm = fareAdditionalChargesPerKm;
	}

	public String getFareAdditionalChargesPerKm(){
		return fareAdditionalChargesPerKm;
	}

	public void setPickUpLocation(String pickUpLocation){
		this.pickUpLocation = pickUpLocation;
	}

	public String getPickUpLocation(){
		return pickUpLocation;
	}

	public void setRideDateTime(String rideDateTime){
		this.rideDateTime = rideDateTime;
	}

	public String getRideDateTime(){
		return rideDateTime;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}
}