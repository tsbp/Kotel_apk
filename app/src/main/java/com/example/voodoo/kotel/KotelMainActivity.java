package com.example.voodoo.kotel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TextView;


public class KotelMainActivity extends Activity implements OnClickListener{

    Button updBtn, setBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //activity_kotel_main.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_kotel_main);

        Button updBtn = (Button) findViewById(R.id.updtbtn);
        Button setBtn = (Button) findViewById(R.id.setbtn);

        TextView inTemp = (TextView) findViewById(R.id.inTemp);
        inTemp.setShadowLayer(5, 2, 2, Color.BLACK);

        TextView outTemp = (TextView) findViewById(R.id.outTemp);
        outTemp.setTextColor(Color.WHITE);
        outTemp.setShadowLayer(5, 2, 2, Color.BLACK);

        setBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setbtn:
                Intent intent = new Intent(this, settingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

}
