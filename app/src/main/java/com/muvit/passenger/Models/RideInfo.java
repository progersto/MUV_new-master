package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RideInfo{

	@SerializedName("driverId")
	@Expose
	private String driverId;

	@SerializedName("driverLastLat")
	@Expose
	private String driverLastLat;

	@SerializedName("brandName")
	@Expose
	private String brandName;

	@SerializedName("carName")
	@Expose
	private String carName;

	@SerializedName("typeName")
	@Expose
	private String typeName;

	@SerializedName("minFareKm")
	@Expose
	private String minFareKm;

	@SerializedName("minFareKmRate")
	@Expose
	private String minFareKmRate;

	@SerializedName("extraFareKmRate")
	@Expose
	private String extraFareKmRate;

	@SerializedName("extraFareKm")
	@Expose
	private String extraFareKm;

	@SerializedName("driverProfileImage")
	@Expose
	private String driverProfileImage;

	@SerializedName("carNumber")
	@Expose
	private String carNumber;

	@SerializedName("driverLastLong")
	@Expose
	private String driverLastLong;

	@SerializedName("totalRatting")
	@Expose
	private String totalRatting;

	@SerializedName("avgRatting")
	@Expose
	private String avgRatting;

	@SerializedName("driverArrivelTime")
	@Expose
	private String driverArrivelTime;

	@SerializedName("driverName")
	@Expose
	private String driverName;

	@SerializedName("typeImage")
	@Expose
	private String typeImage;

	@SerializedName("perMinRate")
	@Expose
	private double perMinRate;

	@SerializedName("driverContact")
	@Expose
	private String driverContact;

	@SerializedName("isLongRide")
	@Expose
	private String isLongRide;

	@SerializedName("pickUpLocation")
	@Expose
	private String pickUpLocation;

	@SerializedName("pickUpLat")
	@Expose
	private String pickUpLat;

	@SerializedName("pickUpLong")
	@Expose
	private String pickUpLong;

	@SerializedName("dropOffLocation")
	@Expose
	private String dropOffLocation;

	@SerializedName("dropOffLat")
	@Expose
	private String dropOffLat;

	@SerializedName("dropOffLong")
	@Expose
	private String dropOffLong;

	public void setDriverLastLat(String driverLastLat){
		this.driverLastLat = driverLastLat;
	}

	public String getDriverLastLat(){
		return driverLastLat;
	}

	public void setBrandName(String brandName){
		this.brandName = brandName;
	}

	public String getBrandName(){
		return brandName;
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

	public void setMinFareKm(String minFareKm){
		this.minFareKm = minFareKm;
	}

	public String getMinFareKm(){
		return minFareKm;
	}

	public void setMinFareKmRate(String minFareKmRate){
		this.minFareKmRate = minFareKmRate;
	}

	public String getMinFareKmRate(){
		return minFareKmRate;
	}

	public void setExtraFareKmRate(String extraFareKmRate){
		this.extraFareKmRate = extraFareKmRate;
	}

	public String getExtraFareKmRate(){
		return extraFareKmRate;
	}

	public void setExtraFareKm(String extraFareKm){
		this.extraFareKm = extraFareKm;
	}

	public String getExtraFareKm(){
		return extraFareKm;
	}

	public void setDriverProfileImage(String driverProfileImage){
		this.driverProfileImage = driverProfileImage;
	}

	public String getDriverProfileImage(){
		return driverProfileImage;
	}

	public void setCarNumber(String carNumber){
		this.carNumber = carNumber;
	}

	public String getCarNumber(){
		return carNumber;
	}

	public void setDriverLastLong(String driverLastLong){
		this.driverLastLong = driverLastLong;
	}

	public String getDriverLastLong(){
		return driverLastLong;
	}

	public void setTotalRatting(String totalRatting){
		this.totalRatting = totalRatting;
	}

	public String getTotalRatting(){
		return totalRatting;
	}

	public void setAvgRatting(String avgRatting){
		this.avgRatting = avgRatting;
	}

	public String getAvgRatting(){
		return avgRatting;
	}

	public void setDriverArrivelTime(String driverArrivelTime){
		this.driverArrivelTime = driverArrivelTime;
	}

	public String getDriverArrivelTime(){
		return driverArrivelTime;
	}

	public void setDriverName(String driverName){
		this.driverName = driverName;
	}

	public String getDriverName(){
		return driverName;
	}

	public void setTypeImage(String typeImage){
		this.typeImage = typeImage;
	}

	public String getTypeImage(){
		return typeImage;
	}

	public void setPerMinRate(double perMinRate){
		this.perMinRate = perMinRate;
	}

	public double getPerMinRate(){
		return perMinRate;
	}

	public void setDriverContact(String driverContact){
		this.driverContact = driverContact;
	}

	public String getDriverContact(){
		return driverContact;
	}

	public String getIsLongRide() {
		return isLongRide;
	}

	public void setIsLongRide(String isLongRide) {
		this.isLongRide = isLongRide;
	}

	public String getPickUpLocation() {
		return pickUpLocation;
	}

	public void setPickUpLocation(String pickUpLocation) {
		this.pickUpLocation = pickUpLocation;
	}

	public String getPickUpLat() {
		return pickUpLat;
	}

	public void setPickUpLat(String pickUpLat) {
		this.pickUpLat = pickUpLat;
	}

	public String getPickUpLong() {
		return pickUpLong;
	}

	public void setPickUpLong(String pickUpLong) {
		this.pickUpLong = pickUpLong;
	}

	public String getDropOffLocation() {
		return dropOffLocation;
	}

	public void setDropOffLocation(String dropOffLocation) {
		this.dropOffLocation = dropOffLocation;
	}

	public String getDropOffLat() {
		return dropOffLat;
	}

	public void setDropOffLat(String dropOffLat) {
		this.dropOffLat = dropOffLat;
	}

	public String getDropOffLong() {
		return dropOffLong;
	}

	public void setDropOffLong(String dropOffLong) {
		this.dropOffLong = dropOffLong;
	}

	public String getDriverId() {
		return driverId;
	}

	public void setDriverId(String driverId) {
		this.driverId = driverId;
	}
}