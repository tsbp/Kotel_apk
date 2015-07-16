package com.example.voodoo.kotel;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class settingsActivity extends Activity {


    String configReference = "lanConfig";
    String[] time;
    String[] temp;


    ListView lvMain;
    List<String> aStrings = new ArrayList<>();
    List<String> bStrings = new ArrayList<>();

    final int MODE_RECEIVE_CONFIG = 0;
    final int MODE_SEND_CONFIG    = 1;
    int mode = MODE_RECEIVE_CONFIG;
    int currentPeroid;

    int selectedRow;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button bSave = (Button) findViewById(R.id.btnSave);
        Button bLoad = (Button) findViewById(R.id.btnLoad);
        Button bAdd  = (Button) findViewById(R.id.btnAdd);
        Button bDel  = (Button) findViewById(R.id.btnDel);
        //================================================
        bSave.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                if(time != null)
                {
                    mode = MODE_SEND_CONFIG;
                    currentPeroid = 1;
                    formBuffer();
                    ProgressBar pb = (ProgressBar)findViewById(R.id.pbConfig);
                    pb.setVisibility(View.VISIBLE);
                    SendTask tsk = new SendTask();
                    tsk.execute();
                }

            }
        });
        //================================================
        bLoad.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                mode = MODE_RECEIVE_CONFIG;
                REQUEST_ACTION = "CONF1";
                ProgressBar pb = (ProgressBar)findViewById(R.id.pbConfig);
                pb.setVisibility(View.VISIBLE);
                loadConfig();
                SendTask tsk = new SendTask();
                tsk.execute();

            }
        });
        //================================================
        bAdd.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(time.length < 9)
                {
                    List<String> tmpTime = new ArrayList<>();
                    List<String> tmpTemp = new ArrayList<>();
                    if(time != null)
                        for(int k = 0; k < time.length; k++)
                        {
                            tmpTime.add(time[k]);
                            tmpTemp.add(temp[k]);
                        }
                    tmpTime.add("??:??");
                    tmpTemp.add("??.?");

                    time = new String[tmpTime.size()];
                    temp = new String[tmpTemp.size()];

                    tmpTime.toArray(time);
                    tmpTemp.toArray(temp);
                    updateConfigTable();
                }
            }
        });
        //================================================
        bDel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (time != null) {
                    List<String> tmpTime = new ArrayList<>();
                    List<String> tmpTemp = new ArrayList<>();
                    for (int k = 0; k < (time.length - 1); k++) {
                        tmpTime.add(time[k]);
                        tmpTemp.add(temp[k]);
                    }
                    time = new String[tmpTime.size()];
                    temp = new String[tmpTemp.size()];

                    tmpTime.toArray(time);
                    tmpTemp.toArray(temp);
                    updateConfigTable();
                }

            }
        });
    }
    //==============================================================================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        String aTime = data.getStringExtra("rTime");
        String aTemp = data.getStringExtra("rTemp");
        time[selectedRow] = aTime;
        temp[selectedRow] = aTemp;
        updateConfigTable();
    }
    //==============================================================================================
    void formBuffer()
    {
        REQUEST_ACTION = "";
        REQUEST_ACTION = "CSAV" +
                currentPeroid +
                time.length +
                time[currentPeroid-1].substring(0,2) + time[currentPeroid-1].substring(3,5)+
                temp[currentPeroid-1].substring(0,2) + temp[currentPeroid-1].substring(3,4);
    }
    //==============================================================================================
    // ????? ????????? ??? Map
    final String ATTRIBUTE_NAME_REF = "ref";
    final String ATTRIBUTE_NAME_TIME = "time";
    final String ATTRIBUTE_NAME_TEMP = "temper";
    //==============================================================================================
    void updateConfigTable()
    {

        ArrayList<Map<String, Object>> data = new ArrayList<>(
                time.length);
        Map<String, Object> m;
        for (int i = 0; i < time.length; i++) {
            m = new HashMap<>();
            m.put(ATTRIBUTE_NAME_REF, i+1);
            m.put(ATTRIBUTE_NAME_TIME, time[i]);
            m.put(ATTRIBUTE_NAME_TEMP, temp[i]);

            data.add(m);
        }
        String[] from = {ATTRIBUTE_NAME_REF, ATTRIBUTE_NAME_TIME, ATTRIBUTE_NAME_TEMP};
        int[] to = {R.id.tvRef, R.id.tvTime, R.id.tvTemp};
        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.item, from, to);
        lvMain = (ListView) findViewById(R.id.lvMain);
        lvMain.setAdapter(sAdapter);
        //==========================================================
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectedRow = position;
                Intent intent = new Intent(settingsActivity.this, PeroidConfig.class);
                intent.putExtra("pTime", time[position]);
                intent.putExtra("pTemp", temp[position]);
                startActivityForResult(intent, 1);
            }
        });
    }
    //==============================================================================================
    int[] ip = new int[4];
    int port;
    //==============================================================================================
    void loadConfig() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String  cString = sharedPreferences.getString(configReference, "") ;

        String _ip, _port;

        if( cString != null && cString.contains("port"))
        {
            _ip   = cString.substring(0,cString.indexOf("port"));
            _port = cString.substring(cString.indexOf("port")+4,cString.length());
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

            switch(mode)
            {
                case MODE_RECEIVE_CONFIG:
                    receiveConfig( ret.substring(ret.indexOf("data:") + 5, ret.length()));
                    break;

                case MODE_SEND_CONFIG:
                    if(ret.length() > 10)
                        sendConfig(ret.substring(ret.indexOf("data:") + 5, ret.length()));
                    break;
            }

            ret = "no answer";
        }
    }
    //==============================================================================================
    void sendConfig(String resp)
    {
        if(resp.contains("OK"))
        {
            if(currentPeroid <= time.length)
            {
                formBuffer();
                SendTask tsk = new SendTask();
                tsk.execute();
                currentPeroid++;
            }
            else
            {
                ProgressBar pb = (ProgressBar)findViewById(R.id.pbConfig);
                pb.setVisibility(View.INVISIBLE);
                TextView t = (TextView)findViewById(R.id.confResponse);
                t.setText("Done");
            }
        }
        else
        {
            formBuffer();
            SendTask tsk = new SendTask();
            tsk.execute();
        }
    }
    //==============================================================================================
    void receiveConfig(String resp)
    {
        String s;

        if((resp.indexOf("\n\r") == 10) && (resp.indexOf("C") == 0) )
        {
            resp = resp.substring(0, 10);
            s = resp.substring(1, 2);
            int msgNumb = Integer.parseInt(s);
            s = resp.substring(2, 3);
            int partsCount = Integer.parseInt(s);

            if(msgNumb == partsCount)
            {
                aStrings.add(resp.substring(3));
                bStrings.add(resp.substring(3));
                time = new String[aStrings.size()];
                temp = new String[bStrings.size()];
                time = aStrings.toArray(time);
                temp = bStrings.toArray(temp);

                for(int j = 0; j < time.length; j++)
                    time[j] = time[j].substring(0,2) + ":" + time[j].substring(2,4);
                for(int j = 0; j < temp.length; j++)
                {
                    String a1 = temp[j].substring(4, 6);
                    String a2 = temp[j].substring(6);
                    temp[j] = a1 + "." + a2;
                }

                updateConfigTable();
                ProgressBar pb = (ProgressBar)findViewById(R.id.pbConfig);
                pb.setVisibility(View.INVISIBLE);
                TextView t = (TextView)findViewById(R.id.confResponse);
                t.setText("Done");
                aStrings.clear();
                bStrings.clear();
            }
            else
            {
                aStrings.add(resp.substring(3));
                bStrings.add(resp.substring(3));
                REQUEST_ACTION = "CONF" + String.valueOf(msgNumb+1);
                SendTask tsk = new SendTask();
                tsk.execute();
            }
        }
        else
        {
            SendTask tsk = new SendTask();
            tsk.execute();
        }

    }

}