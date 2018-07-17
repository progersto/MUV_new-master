package com.muvit.passenger.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Card {
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
}
