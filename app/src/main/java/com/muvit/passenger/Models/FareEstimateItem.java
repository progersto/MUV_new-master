package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FareEstimateItem {

	@SerializedName("fareDistanceCharges")
	@Expose
	private String fareDistanceCharges;

	@SerializedName("estimateTime")
	@Expose
	private int estimateTime;

	@SerializedName("fareTime")
	@Expose
	private String fareTime;

	@SerializedName("fareTimeCharges")
	@Expose
	private String fareTimeCharges;

	@SerializedName("fareAdditionalCharges")
	@Expose
	private String fareAdditionalCharges;

	@SerializedName("estimateExtraKm")
	@Expose
	private double estimateExtraKm;

	@SerializedName("fareDistance")
	@Expose
	private String fareDistance;

	@SerializedName("estmatedTimeCharges")
	@Expose
	private String estmatedTimeCharges;

	@SerializedName("dropOffLocation")
	@Expose
	private String dropOffLocation;

	@SerializedName("carTypeImage")
	@Expose
	private String carTypeImage;

	@SerializedName("reachedLocation")
	@Expose
	private boolean reachedLocation;

	@SerializedName("totalExtraCharges")
	@Expose
	private double totalExtraCharges;

	@SerializedName("fareAdditionalKm")
	@Expose
	private String fareAdditionalKm;

	@SerializedName("timeChargesPerMin")
	@Expose
	private double timeChargesPerMin;

	@SerializedName("finalEstimatedTotal")
	@Expose
	private String finalEstimatedTotal;

	@SerializedName("totalDistance")
	@Expose
	private double totalDistance;

	@SerializedName("pickUpLocation")
	@Expose
	private String pickUpLocation;

	@SerializedName("carTypeName")
	@Expose
	private String carTypeName;

	@SerializedName("ridePathString")
	@Expose
	private String ridePathString;

	@SerializedName("isLongRide")
	@Expose
	private String isLongRide;

	public void setFareDistanceCharges(String fareDistanceCharges){
		this.fareDistanceCharges = fareDistanceCharges;
	}

	public String getFareDistanceCharges(){
		return fareDistanceCharges;
	}

	public void setEstimateTime(int estimateTime){
		this.estimateTime = estimateTime;
	}

	public int getEstimateTime(){
		return estimateTime;
	}

	public void setFareTime(String fareTime){
		this.fareTime = fareTime;
	}

	public String getFareTime(){
		return fareTime;
	}

	public void setFareTimeCharges(String fareTimeCharges){
		this.fareTimeCharges = fareTimeCharges;
	}

	public String getFareTimeCharges(){
		return fareTimeCharges;
	}

	public void setFareAdditionalCharges(String fareAdditionalCharges){
		this.fareAdditionalCharges = fareAdditionalCharges;
	}

	public String getFareAdditionalCharges(){
		return fareAdditionalCharges;
	}

	public void setEstimateExtraKm(double estimateExtraKm){
		this.estimateExtraKm = estimateExtraKm;
	}

	public double getEstimateExtraKm(){
		return estimateExtraKm;
	}

	public void setFareDistance(String fareDistance){
		this.fareDistance = fareDistance;
	}

	public String getFareDistance(){
		return fareDistance;
	}

	public void setEstmatedTimeCharges(String estmatedTimeCharges){
		this.estmatedTimeCharges = estmatedTimeCharges;
	}

	public String getEstmatedTimeCharges(){
		return estmatedTimeCharges;
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

	public void setReachedLocation(boolean reachedLocation){
		this.reachedLocation = reachedLocation;
	}

	public boolean isReachedLocation(){
		return reachedLocation;
	}

	public void setTotalExtraCharges(int totalExtraCharges){
		this.totalExtraCharges = totalExtraCharges;
	}

	public double getTotalExtraCharges(){
		return totalExtraCharges;
	}

	public void setFareAdditionalKm(String fareAdditionalKm){
		this.fareAdditionalKm = fareAdditionalKm;
	}

	public String getFareAdditionalKm(){
		return fareAdditionalKm;
	}

	public void setTimeChargesPerMin(double timeChargesPerMin){
		this.timeChargesPerMin = timeChargesPerMin;
	}

	public double getTimeChargesPerMin(){
		return timeChargesPerMin;
	}

	public void setFinalEstimatedTotal(String finalEstimatedTotal){
		this.finalEstimatedTotal = finalEstimatedTotal;
	}

	public String getFinalEstimatedTotal(){
		return finalEstimatedTotal;
	}

	public void setTotalDistance(double totalDistance){
		this.totalDistance = totalDistance;
	}

	public double getTotalDistance(){
		return totalDistance;
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

	public String getRidePathString() {
		return ridePathString;
	}

	public void setRidePathString(String ridePathString) {
		this.ridePathString = ridePathString;
	}

	public String getIsLongRide() {
		return isLongRide;
	}

	public void setIsLongRide(String isLongRide) {
		this.isLongRide = isLongRide;
	}
}