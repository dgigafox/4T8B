package com.a4t8bfarm.a4t8b;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ThanksActivity extends AppCompatActivity {

    final String LOG = "ThanksActivity";

    Button home;
    TextView orderNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanks);


        home = (Button)findViewById(R.id.btnHome);
        orderNumber = (TextView)findViewById(R.id.orderNmbr);


        String OrderNumber = getIntent().getStringExtra("oNumber");
        orderNumber.setText(OrderNumber);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ThanksActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);

            }
        });
    }
    @Override
    public void onBackPressed(){

    }

}
