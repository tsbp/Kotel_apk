package com.example.voodoo.kotel;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class LanConfigActivity extends Activity {


    EditText ip, port;

    String configReference = "lanConfig";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan_config);

        ip   = (EditText) findViewById(R.id.setIp);
        port = (EditText) findViewById(R.id.setPort);

        loadConfig();

        Button okBtn = (Button) findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveConfig();
                finish();
            }
        });
    }

    void saveConfig() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(configReference, ip.getText().toString() + "port" + port.getText().toString());
        editor.apply();
    }

    void loadConfig() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String  conf = sharedPreferences.getString(configReference, "") ;
        String _ip, _port;

        if(conf != null && conf.contains("port"))
        {
           _ip   = conf.substring(0,conf.indexOf("port"));
           _port = conf.substring(conf.indexOf("port")+4,conf.length());
        }
        else return;
        ip.setText(_ip);
        port.setText(_port);
    }
}
