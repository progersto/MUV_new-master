package com.muvit.passenger.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface CardDao {

    @Query("SELECT * FROM Card ORDER BY id")
    Flowable<List<Card>> getListCards();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Card... cardObjs);

    @Update
    void update(Card cardObj);

    @Delete
    void delete(Card... cards);

    // update bet
//    @Query("UPDATE Card SET bet = :bet WHERE id =:id")
//    void updateBet(int bet, int id);

}
