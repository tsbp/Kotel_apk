package com.example.voodoo.kotel;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.voodoo.plot;
import com.example.voodoo.plot2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;


public class KotelMainActivity extends Activity {//implements View.OnClickListener{

    Button updBtn, setBtn;
    ProgressBar pbWait;
    //private TextView response;
    String configReference = "lanConfig";
    public String plotValue = "";
    String timeString = "";
    String BROADCAST_ACTION = "I1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //activity_kotel_main.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_kotel_main);

        Button updBtn = (Button) findViewById(R.id.updtbtn);
        Button setBtn = (Button) findViewById(R.id.setbtn);

        final ProgressBar pbWait = (ProgressBar) findViewById(R.id.progressBar);


        //final TextView response = (TextView) findViewById(R.id.response);

        TextView inTemp = (TextView) findViewById(R.id.inTemp);
        inTemp.setShadowLayer(5, 2, 2, Color.BLACK);

        TextView outTemp = (TextView) findViewById(R.id.outTemp);
        outTemp.setTextColor(Color.WHITE);
        outTemp.setShadowLayer(5, 2, 2, Color.BLACK);


        setBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(KotelMainActivity.this, settingsActivity.class);
                startActivity(intent);
            }
        });
        updBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                loadConfig();
                pbWait.setVisibility(View.VISIBLE);
                //response.setText("Sending...");
                SendTask tsk = new SendTask();
                tsk.execute();
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_kotel_main, menu);
        return true;
    }
    //==============================================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent = new Intent(KotelMainActivity.this, LanConfigActivity.class);
        startActivity(intent);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //==============================================================================================
    String ret = "";
    int i = 0;
    class SendTask extends AsyncTask<Void, Void, Void>
    {
        //==========================================================================================
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(BROADCAST_ACTION.contains("I1"))
            {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                timeString = dateFormat.format(cal.getTime());
                BROADCAST_ACTION += timeString;
            }
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
                dp = new DatagramPacket(BROADCAST_ACTION.getBytes(), BROADCAST_ACTION.getBytes().length, addr, port);
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

        byte plotRef = 'I';

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
//            i++;
//            String st = "Sended: " + i + "\r\n" + ret;
//
//            TextView response = (TextView) findViewById(R.id.response);
//            response.setText(st);

            int u = ret.indexOf("data:") + 5;
            String resp = ret.substring(u, ret.length());
            String s;
            if(((resp.contains("I")) || (resp.contains("O"))) && ( (resp.indexOf("\n")) != -1))
            {

                if(resp.contains("I"))  plotRef = 'I';
                if(resp.contains("O"))  plotRef = 'O';

                s = ret.substring(u+1, u + 2);
                int msgNumb = Integer.parseInt(s);
                s = ret.substring(u + 2, u + 3);
                int partsCount = Integer.parseInt(s);
                s = ret.substring(u + 3, u + 4);
                int valsCount = Integer.parseInt(s);

                if(msgNumb == partsCount)
                {
                    s = resp.substring(4, resp.indexOf("\n"));
                    plotValue += s;
                    try
                    {
                        if(plotRef == 'I')
                        {
                            for(int j = 0; j < 24; j++)
                                plot.aBuf[j] = Integer.parseInt( plotValue.substring(j*3, j*3+3));

                            com.example.voodoo.plot inCanvas = (com.example.voodoo.plot) findViewById(R.id.inCanvas);
                            inCanvas.invalidate();
                        }
                        if(plotRef == 'O')
                        {
                            for(int j = 0; j < 24; j++)
                                plot2.aBuf2[j] = Integer.parseInt( plotValue.substring(j*3, j*3+3));

                            com.example.voodoo.plot2 inCanvas = (com.example.voodoo.plot2) findViewById(R.id.outCanvas);
                            inCanvas.invalidate();
                        }
                    }
                    catch (Exception e)
                    {
                    }

                    if(BROADCAST_ACTION.contains("I3"))
                    {
                        TextView inTemp = (TextView) findViewById(R.id.inTemp);
                        inTemp.setText(plotValue.substring(plotValue.length()-3, plotValue.length()-1) + '.' + plotValue.substring(plotValue.length()-1));
                        BROADCAST_ACTION = "O1";
                        SendTask tsk = new SendTask();
                        tsk.execute();
                    }
                    if(BROADCAST_ACTION.contains("O3"))
                    {
                        TextView outTemp = (TextView) findViewById(R.id.outTemp);
                        outTemp.setText(plotValue.substring(plotValue.length()-3, plotValue.length()-1) + '.' + plotValue.substring(plotValue.length()-1));
                        BROADCAST_ACTION = "I1";

                        ProgressBar pbWait = (ProgressBar) findViewById(R.id.progressBar);
                        pbWait.setVisibility(View.INVISIBLE);
                    }

                    plotValue = "";

                }
                else
                {
                    s = resp.substring(4, resp.indexOf("\n"));

                    if(resp.contains("I"))  BROADCAST_ACTION = 'I' + String.valueOf(msgNumb+1);
                    if(resp.contains("O"))  BROADCAST_ACTION = 'O' + String.valueOf(msgNumb+1);

                    plotValue += s;
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

        @Override
        protected void onCancelled() {
            super.onCancelled();

        }
    }

}
