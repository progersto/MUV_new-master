package com.muvit.passenger.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.muvit.passenger.R;

public class CardDetailsActivity extends AppCompatActivity {
    ImageView back_btn;
    Button change_card;
    private TextView textCardNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_details);
        back_btn = (ImageView) findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        change_card = (Button) findViewById(R.id.change_card);
        change_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        textCardNum = findViewById(R.id.textCardNum);
        StringBuffer sb = new StringBuffer(getIntent().getStringExtra("cardNum"));
        sb.insert(4," ");
        sb.insert(9," ");
        sb.insert(14," ");

        textCardNum.setText(sb.toString());
    }
}
