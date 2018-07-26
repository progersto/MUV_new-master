package com.muvit.passenger.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity
public class Card implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "numberCard")
    public String numberCard;

    @ColumnInfo(name = "month")
    public String month;

    @ColumnInfo(name = "year")
    public String year;

    @ColumnInfo(name = "cvv")
    public String cvv;


    public Card(String numberCard, String month, String year, String cvv) {
        this.numberCard = numberCard;
        this.month = month;
        this.year = year;
        this.cvv = cvv;
    }

    protected Card(Parcel in) {
        id = in.readLong();
        numberCard = in.readString();
        month = in.readString();
        year = in.readString();
        cvv = in.readString();
    }

    public static final Creator<Card> CREATOR = new Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumberCard() {
        return numberCard;
    }

    public void setNumberCard(String numberCard) {
        this.numberCard = numberCard;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(numberCard);
        parcel.writeString(month);
        parcel.writeString(year);
        parcel.writeString(cvv);
    }
}
