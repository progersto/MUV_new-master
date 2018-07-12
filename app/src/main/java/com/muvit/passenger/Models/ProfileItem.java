package com.muvit.passenger.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProfileItem{

	@SerializedName("defaultPaymentMethod")
	@Expose
	private String defaultPaymentMethod;

	@SerializedName("lastName")
	@Expose
	private String lastName;

	@SerializedName("lastLoginDateTime")
	@Expose
	private String lastLoginDateTime;

	@SerializedName("depositFund")
	@Expose
	private String depositFund;

	@SerializedName("currenctBalance")
	@Expose
	private String currenctBalance;

	@SerializedName("isOnline")
	@Expose
	private String isOnline;

	@SerializedName("profileImage")
	@Expose
	private String profileImage;

	@SerializedName("updatedDate")
	@Expose
	private String updatedDate;

	@SerializedName("isActive")
	@Expose
	private String isActive;

	@SerializedName("workLocation")
	@Expose
	private String workLocation;

	@SerializedName("password")
	@Expose
	private String password;

	@SerializedName("countryCode")
	@Expose
	private String countryCode;

	@SerializedName("email")
	@Expose
	private String email;

	@SerializedName("lastActiveLong")
	@Expose
	private String lastActiveLong;

	@SerializedName("homeLat")
	@Expose
	private String homeLat;

	@SerializedName("workLat")
	@Expose
	private String workLat;

	@SerializedName("mobileNo")
	@Expose
	private String mobileNo;

	@SerializedName("paypalEmail")
	@Expose
	private String paypalEmail;

	@SerializedName("uId")
	@Expose
	private int uId;

	@SerializedName("firstName")
	@Expose
	private String firstName;

	@SerializedName("createdDate")
	@Expose
	private String createdDate;

	@SerializedName("redeemRequest")
	@Expose
	private String redeemRequest;

	@SerializedName("homeLocation")
	@Expose
	private String homeLocation;

	@SerializedName("homeLong")
	@Expose
	private String homeLong;

	@SerializedName("userType")
	@Expose
	private String userType;

	@SerializedName("activationCode")
	@Expose
	private String activationCode;

	@SerializedName("workLong")
	@Expose
	private String workLong;

	@SerializedName("lastActiveLat")
	@Expose
	private String lastActiveLat;

	public void setDefaultPaymentMethod(String defaultPaymentMethod){
		this.defaultPaymentMethod = defaultPaymentMethod;
	}

	public String getDefaultPaymentMethod(){
		return defaultPaymentMethod;
	}

	public void setLastName(String lastName){
		this.lastName = lastName;
	}

	public String getLastName(){
		return lastName;
	}

	public void setLastLoginDateTime(String lastLoginDateTime){
		this.lastLoginDateTime = lastLoginDateTime;
	}

	public String getLastLoginDateTime(){
		return lastLoginDateTime;
	}

	public void setDepositFund(String depositFund){
		this.depositFund = depositFund;
	}

	public String getDepositFund(){
		return depositFund;
	}

	public void setCurrenctBalance(String currenctBalance){
		this.currenctBalance = currenctBalance;
	}

	public String getCurrenctBalance(){
		return currenctBalance;
	}

	public void setIsOnline(String isOnline){
		this.isOnline = isOnline;
	}

	public String getIsOnline(){
		return isOnline;
	}

	public void setProfileImage(String profileImage){
		this.profileImage = profileImage;
	}

	public String getProfileImage(){
		return profileImage;
	}

	public void setUpdatedDate(String updatedDate){
		this.updatedDate = updatedDate;
	}

	public String getUpdatedDate(){
		return updatedDate;
	}

	public void setIsActive(String isActive){
		this.isActive = isActive;
	}

	public String getIsActive(){
		return isActive;
	}

	public void setWorkLocation(String workLocation){
		this.workLocation = workLocation;
	}

	public String getWorkLocation(){
		return workLocation;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return password;
	}

	public void setCountryCode(String countryCode){
		this.countryCode = countryCode;
	}

	public String getCountryCode(){
		return countryCode;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return email;
	}

	public void setLastActiveLong(String lastActiveLong){
		this.lastActiveLong = lastActiveLong;
	}

	public String getLastActiveLong(){
		return lastActiveLong;
	}

	public void setHomeLat(String homeLat){
		this.homeLat = homeLat;
	}

	public String getHomeLat(){
		return homeLat;
	}

	public void setWorkLat(String workLat){
		this.workLat = workLat;
	}

	public String getWorkLat(){
		return workLat;
	}

	public void setMobileNo(String mobileNo){
		this.mobileNo = mobileNo;
	}

	public String getMobileNo(){
		return mobileNo;
	}

	public void setPaypalEmail(String paypalEmail){
		this.paypalEmail = paypalEmail;
	}

	public String getPaypalEmail(){
		return paypalEmail;
	}

	public void setUId(int uId){
		this.uId = uId;
	}

	public int getUId(){
		return uId;
	}

	public void setFirstName(String firstName){
		this.firstName = firstName;
	}

	public String getFirstName(){
		return firstName;
	}

	public void setCreatedDate(String createdDate){
		this.createdDate = createdDate;
	}

	public String getCreatedDate(){
		return createdDate;
	}

	public void setRedeemRequest(String redeemRequest){
		this.redeemRequest = redeemRequest;
	}

	public String getRedeemRequest(){
		return redeemRequest;
	}

	public void setHomeLocation(String homeLocation){
		this.homeLocation = homeLocation;
	}

	public String getHomeLocation(){
		return homeLocation;
	}

	public void setHomeLong(String homeLong){
		this.homeLong = homeLong;
	}

	public String getHomeLong(){
		return homeLong;
	}

	public void setUserType(String userType){
		this.userType = userType;
	}

	public String getUserType(){
		return userType;
	}

	public void setActivationCode(String activationCode){
		this.activationCode = activationCode;
	}

	public String getActivationCode(){
		return activationCode;
	}

	public void setWorkLong(String workLong){
		this.workLong = workLong;
	}

	public String getWorkLong(){
		return workLong;
	}

	public void setLastActiveLat(String lastActiveLat){
		this.lastActiveLat = lastActiveLat;
	}

	public String getLastActiveLat(){
		return lastActiveLat;
	}
}