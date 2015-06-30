package com.example.voodoo.kotel;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
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

    Button bSave, bLoad, bAdd, bDel;
    String configReference = "lanConfig";
    String[] time;
    String[] temp;
    ProgressBar pbConfig;

    GridView gvMain;
    ListView lvMain;
    ArrayAdapter<String> adapter;
    List<String> aStrings = new ArrayList<String>();
    List<String> bStrings = new ArrayList<String>();

    int selectedRow;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final TextView response = (TextView) findViewById(R.id.confResponse);

        Button bSave = (Button) findViewById(R.id.btnSave);
        Button bLoad = (Button) findViewById(R.id.btnLoad);


        //================================================
        bSave.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

            }
        });
        //================================================
        bLoad.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                ProgressBar pb = (ProgressBar)findViewById(R.id.pbConfig);
                pb.setVisibility(View.VISIBLE);
                loadConfig();
                response.setText("Sending...");
                SendTask tsk = new SendTask();
                tsk.execute();

            }
        });
//        //================================================
//        bAdd.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//
//            }
//        });
//        //================================================
//        bDel.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//
//            }
//        });
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
    // имена атрибутов для Map
    final String ATTRIBUTE_NAME_TIME = "time";
    final String ATTRIBUTE_NAME_TEMP = "temper";
    //final String ATTRIBUTE_NAME_IMAGE = "image";
    //==============================================================================================
    void updateConfigTable()
    {
        int img = R.drawable.timeicon;

        // упаковываем данные в понятную для адаптера структуру
        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(
                time.length);
        Map<String, Object> m;
        for (int i = 0; i < time.length; i++) {
            m = new HashMap<String, Object>();
            m.put(ATTRIBUTE_NAME_TIME, time[i]);
            m.put(ATTRIBUTE_NAME_TEMP, temp[i]);
            //m.put(ATTRIBUTE_NAME_IMAGE, img);
            data.add(m);
        }

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = { ATTRIBUTE_NAME_TIME, ATTRIBUTE_NAME_TEMP};
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = { R.id.tvTime, R.id.tvTemp };

        // создаем адаптер
        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.item,
                from, to);

        // определяем список и присваиваем ему адаптер
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
                //startActivity(intent);
                startActivityForResult(intent, 1);

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

        String _ip, _port;

        if( cString.contains("port"))
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

            int dataIndex = ret.indexOf("data:") + 5;
            String resp = ret.substring(dataIndex, ret.length());
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
                        temp[j] = a1 + "," + a2;
                    }

                    updateConfigTable();
                    ProgressBar pb = (ProgressBar)findViewById(R.id.pbConfig);
                    pb.setVisibility(View.INVISIBLE);
                    aStrings.clear();
                    bStrings.clear();
                    REQUEST_ACTION = "CONF1";
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
            ret = "no answer";
        }


    }

}
