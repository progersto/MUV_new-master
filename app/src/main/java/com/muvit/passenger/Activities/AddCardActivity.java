package com.muvit.passenger.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.R;
import com.muvit.passenger.database.AppDatabase;
import com.muvit.passenger.database.Card;
import com.muvit.passenger.database.CardDao;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AddCardActivity extends AppCompatActivity {

    Button add_card_btn;
    ImageView back_btn;
    private CardDao cardDao;
    private EditText card_num;
    private EditText card_mm;
    private EditText card_yy;
    private EditText card_cvv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        AppDatabase db = ApplicationController.getInstance().getDatabase();
        cardDao = db.cardDao();

        card_num = findViewById(R.id.card_num);
        card_mm = findViewById(R.id.mm);
        card_yy = findViewById(R.id.yy);
        card_cvv = findViewById(R.id.cvv);
        back_btn = (ImageView) findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        add_card_btn = (Button) findViewById(R.id.add_card_btn);
        add_card_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cardNum = card_num.getText().toString();
                String cardMm = card_mm.getText().toString();
                String cardYy = card_yy.getText().toString();
                String cardCvv = card_cvv.getText().toString();
                insertAccount(new Card(cardNum, cardMm, cardYy, cardCvv));
            }
        });
    }

    private void insertAccount(final Card card) {
        Completable.fromAction(() -> cardDao.insert(card))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {// Insert the new
                        Intent intent = new Intent(AddCardActivity.this,CardDetailsActivity.class);
                        intent.putExtra("cardNum", card_num.getText().toString());
                        startActivity(intent);
                    }//onComplete

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }//addProductForList
}
