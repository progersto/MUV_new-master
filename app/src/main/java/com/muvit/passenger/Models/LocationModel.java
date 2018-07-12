package com.muvit.passenger.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nct4 on 9/28/2016.
 */
public class LocationModel implements Parcelable {

    String lat;
    String longitude;
    String distance;

    public LocationModel(){

    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(lat);
        parcel.writeString(longitude);

    }

    public LocationModel(Parcel parcel){
        this.lat = parcel.readString();
        this.longitude = parcel.readString();
    }

    public static final Creator CREATOR = new Creator<LocationModel>(){

        @Override
        public LocationModel createFromParcel(Parcel parcel) {
            return new LocationModel(parcel);
        }

        @Override
        public LocationModel[] newArray(int i) {
            return new LocationModel[i];
        }
    };

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
