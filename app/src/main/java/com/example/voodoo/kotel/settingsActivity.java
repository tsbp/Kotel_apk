package com.example.voodoo.kotel;

import com.example.voodoo.kotel.util.SystemUiHider;
import com.example.voodoo.plot;
import com.example.voodoo.plot2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class settingsActivity extends Activity {

    Button bSave, bLoad, bAdd, bDel;
    String configReference = "lanConfig";
    String[] time = {"12.30", "18.30", "22.30", "00.30"};
    String[] temp = {"22", "24", "26.7", "13.9"};

    GridView gvMain;
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        adapter = new ArrayAdapter<String>(this, R.layout.item, R.id.tvText, time);
        gvMain = (GridView) findViewById(R.id.gvTime);
        gvMain.setAdapter(adapter);

        adapter = new ArrayAdapter<String>(this, R.layout.item, R.id.tvText, temp);
        gvMain = (GridView) findViewById(R.id.gvTemprature);
        gvMain.setAdapter(adapter);


        Button bSave = (Button) findViewById(R.id.btnSave);
        Button bLoad = (Button) findViewById(R.id.btnLoad);
        Button bAdd = (Button) findViewById(R.id.btnAdd);
        Button bDel = (Button) findViewById(R.id.btnDel);

        final TextView response = (TextView) findViewById(R.id.confResponse);

        //================================================
        bSave.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

            }
        });
        //================================================
        bLoad.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                loadConfig();
                //pbWait.setVisibility(View.VISIBLE);

                response.setText("Sending...");
                SendTask tsk = new SendTask();
                tsk.execute();

            }
        });
        //================================================
        bAdd.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

            }
        });
        //================================================
        bDel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

            }
        });
    }
    //==============================================================================================
    SharedPreferences sPref;
    public String config;
    int[] ip = new int[4];
    int port;
    //==============================================================================================
    void loadConfig() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String  cString = sharedPreferences.getString(configReference, "") ;

//        sPref = getPreferences(MODE_PRIVATE);
//        String cString = sPref.getString(configReference, "");
        String _ip, _port;

        int i = cString.indexOf("port");
        if(i != -1)
        {
            _ip   = cString.substring(0,i);
            _port = cString.substring(i+4,cString.length());
        }
        else return;

        String tmp;
        for(int k = 0; k < 4; k++)
        {
            i = _ip.indexOf(".");
            if(i != -1)  tmp = _ip.substring(0,i);
            else tmp = _ip;
            ip[k] = Integer.parseInt(tmp);
            if(k < 3) _ip = _ip.substring(i+1, _ip.length());
        }
        port = Integer.parseInt(_port);

    }
    //==============================================================================================
    String REQUEST_ACTION = "CONF1";
    String ret = "";
    int i = 0;

    class SendTask extends AsyncTask<Void, Void, Void>
    {
        //==========================================================================================
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        //==========================================================================================
        @Override
        protected Void doInBackground(Void... params) {

            DatagramSocket ds = null;
            try
            {

                byte[] ipAddr = new byte[]{ (byte)(ip[0] & 0xff), (byte) ip[1], (byte) ip[2], (byte) ip[3]};
                InetAddress addr = InetAddress.getByAddress(ipAddr);
                ds = new DatagramSocket(port);
                DatagramPacket dp;
                dp = new DatagramPacket(REQUEST_ACTION.getBytes(), REQUEST_ACTION.getBytes().length, addr, port);
                ds.setBroadcast(true);
                ds.send(dp);
                //===================
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket =
                        new DatagramPacket(receiveData, receiveData.length);

                //response.setText("Waiting for answer...");
                ds.setSoTimeout(10000);

                try {
                    ds.receive(receivePacket);
                    String modifiedSentence =
                            new String(receivePacket.getData());
                    InetAddress returnIPAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();
                    ret = "ip:" + returnIPAddress + "\r\nport:" + port + "\r\ndata:" + modifiedSentence;
                }
                catch (SocketTimeoutException ste)
                {
                    System.out.println("Timeout Occurred: Packet assumed lost");
                }
                //===================
                ds.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (ds != null)
                {
                    ds.close();
                }
            }
            return null;
        }



        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            i++;
            String st = "Sended: " + i + "\r\n" + ret;

            TextView response = (TextView) findViewById(R.id.confResponse);
            response.setText(st);

            ret = "no answer";
        }


    }

}
