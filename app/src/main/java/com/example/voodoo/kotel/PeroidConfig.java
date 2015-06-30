package com.example.voodoo.kotel;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.EditText;


public class PeroidConfig extends Activity implements OnClickListener{

    Button okBtn;
    EditText etTime, etTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peroid_config);

        Button okBtn = (Button) findViewById(R.id.okBtnPeriod);
        okBtn.setOnClickListener(this);

        EditText etTime = (EditText)findViewById(R.id.setTime);
        EditText etTemp = (EditText)findViewById(R.id.setTemp);

        Intent intent = getIntent();

        String time = intent.getStringExtra("pTime");
        String temp = intent.getStringExtra("pTemp");

        etTime.setText(time);
        etTemp.setText(temp);
    }

    @Override
    public void onClick(View v) {

        EditText etTime = (EditText)findViewById(R.id.setTime);
        EditText etTemp = (EditText)findViewById(R.id.setTemp);

        Intent intent = new Intent();
        intent.putExtra("rTime", etTime.getText().toString());
        intent.putExtra("rTemp", etTemp.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

}
