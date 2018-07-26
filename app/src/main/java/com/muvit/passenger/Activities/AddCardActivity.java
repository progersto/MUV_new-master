package com.muvit.passenger.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.GeoLocation.logger.Log;
import com.muvit.passenger.R;
import com.muvit.passenger.database.AppDatabase;
import com.muvit.passenger.database.Card;
import com.muvit.passenger.database.CardDao;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class AddCardActivity extends AppCompatActivity {

    Button add_card_btn;
    Button delete_card_btn;
    ImageView back_btn;
    private CardDao cardDao;
    private EditText card_num;
    private EditText card_mm;
    private EditText card_yy;
    private EditText card_cvv;
    private Card card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        AppDatabase db = ApplicationController.getInstance().getDatabase();
        cardDao = db.cardDao();

        card = getIntent().getParcelableExtra("card");

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

        delete_card_btn = (Button) findViewById(R.id.delete_card_btn);
        add_card_btn = (Button) findViewById(R.id.add_card_btn);
        add_card_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cardNum = card_num.getText().toString();
                String cardMm = card_mm.getText().toString();
                String cardYy = card_yy.getText().toString();
                String cardCvv = card_cvv.getText().toString();
                if (cardNum.length() == 19 && cardMm.length() == 2 && cardYy.length() == 2 && cardCvv.length() == 3) {
                    if (card == null){
                        insertCard(new Card(cardNum, cardMm, cardYy, cardCvv));
                    }else {
                        card.setNumberCard(cardNum);
                        card.setMonth(cardMm);
                        card.setYear(cardYy);
                        card.setCvv(cardCvv);
                        updateCard(card);
                    }

                }else {
                    Toast toast = Toast.makeText(AddCardActivity.this, "Enter data card!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
        card_num.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("fff", "fff");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                StringBuffer sb = new StringBuffer(charSequence.toString());
                if (charSequence.length() == 4){
                    sb.insert(4, " ");
                    card_num.setText(sb.toString());
                    card_num.setSelection(card_num.getText().length());
                }
                if (charSequence.length() == 9){
                    sb.insert(9, " ");
                    card_num.setText(sb.toString());
                    card_num.setSelection(card_num.getText().length());
                }
                if (charSequence.length() == 14){
                    sb.insert(14, " ");
                    card_num.setText(sb.toString());
                    card_num.setSelection(card_num.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("fff", "fff");
            }
        });

        delete_card_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCard(card);
            }
        });
        insertDataCard(card);
    }


    public void deleteCard(final Card card) {
        Completable.fromAction(() -> cardDao.delete(card)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        card_num.setText("");
                        card_mm.setText("");
                        card_yy.setText("");
                        card_cvv.setText("");
                        delete_card_btn.setVisibility(View.GONE);
                        add_card_btn.setText("ADD CARD");
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }


    private void insertDataCard(final Card card) {
        if (card != null){
            delete_card_btn.setVisibility(View.VISIBLE);
            add_card_btn.setText("Change card");
            card_num.setText(card.getNumberCard());
            card_mm.setText(card.getMonth());
            card_yy.setText(card.getYear());
            card_cvv.setText(card.getCvv());
            //обновляем данные карты
//            updateCard(new Card());
        }
    }

    private void insertCard(final Card card) {
        Completable.fromAction(() -> cardDao.insert(card))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {// Insert the new
                        Intent intent = new Intent(AddCardActivity.this, CardDetailsActivity.class);
                        intent.putExtra("card", card);
                        startActivity(intent);
                        finish();
                    }//onComplete

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }


    private void updateCard(final Card card) {
        Completable.fromAction(() -> cardDao.update(card))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers
                        .mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        Intent intent = new Intent(AddCardActivity.this, CardDetailsActivity.class);
                        intent.putExtra("card", card);
                        startActivity(intent);
                        finish();
                    }//onComplete

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }//updateImageObj

}
