package com.example.voodoo.kotel;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class LanConfigActivity extends Activity {

    SharedPreferences sPref;
    EditText ip, port;
    Button okBtn;

    String configReference = "lanConfig";
    String config;

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
        editor.commit();
    }

    void loadConfig() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String  savedText = sharedPreferences.getString(configReference, "") ;
        String _ip, _port;
        int i = savedText.indexOf("port");
        if(i != -1)
        {
           _ip   = savedText.substring(0,i);
           _port = savedText.substring(i+4,savedText.length());
        }
        else return;
        ip.setText(_ip);
        port.setText(_port);
    }
}
