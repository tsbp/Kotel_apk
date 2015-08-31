package com.example.voodoo.kotel;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.voodoo.plot;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class KotelMainActivity extends Activity {

    String configReference = "lanConfig";
    public String plotValue = "";
    String timeString = "";
    String BROADCAST_ACTION = "I1";
    TextView response, inTemp, outTemp;
    ProgressBar pbWait;
    com.example.voodoo.plot inCanvas;
    com.example.voodoo.plot outCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kotel_main);

        Button updBtn = (Button) findViewById(R.id.updtbtn);
        Button setBtn = (Button) findViewById(R.id.setbtn);

        inTemp = (TextView) findViewById(R.id.inTemp);
        inTemp.setShadowLayer(5, 2, 2, Color.BLACK);

        outTemp = (TextView) findViewById(R.id.outTemp);
        outTemp.setTextColor(Color.WHITE);
        outTemp.setShadowLayer(5, 2, 2, Color.BLACK);

        response = (TextView) findViewById(R.id.response);
        pbWait = (ProgressBar) findViewById(R.id.progressBar);

        inCanvas  = (com.example.voodoo.plot) findViewById(R.id.inCanvas);
        outCanvas = (com.example.voodoo.plot) findViewById(R.id.outCanvas);

        update();

        setBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(KotelMainActivity.this, settingsActivity.class);
                startActivity(intent);
            }
        });
        updBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                update();
            }
        });
    }
    //==============================================================================================
    void update()
    {
        if (!loadConfig())
        {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "ip not valid", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        pbWait.setVisibility(View.VISIBLE);
        BROADCAST_ACTION = "I1";
        SendTask tsk = new SendTask();
        tsk.execute();
    }
    //==============================================================================================
    int[] ip = new int[4];
    int port;
    //==============================================================================================
    boolean loadConfig() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String  cString = sharedPreferences.getString(configReference, "") ;

        if(cString == null) return false;
        String _ip, _port;


        if(cString.contains("port"))
        {
            _ip   = cString.substring(0,cString.indexOf("port"));
            _port = cString.substring(cString.indexOf("port")+4,cString.length());
        }
        else return false;

        int i;
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
        return  true;
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
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.UK);
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

                ds.setSoTimeout(10000);

                try {
                    ds.receive(receivePacket);
                    String modifiedSentence =
                            new String(receivePacket.getData());
                    InetAddress returnIPAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();
                    ret = returnIPAddress + ":" + port + "\r\ndata:" + modifiedSentence;
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
            i++;
            String st = "Sended: " + i + "\r\n" + ret;
            response.setText(st);

            int u = ret.indexOf("data:") + 5;
            String resp = ret.substring(u, ret.length());
            String s;
            if(((resp.contains("I")) || (resp.contains("O"))) && ( (resp.indexOf("\n")) != -1))
            {
                if(resp.contains("I"))  plotRef = 'I';
                if(resp.contains("O"))  plotRef = 'O';
                int msgNumb;
                int partsCount;
                try
                {
                    s = ret.substring(u+1, u + 2);
                    msgNumb = Integer.parseInt(s);
                    s = ret.substring(u + 2, u + 3);
                    partsCount = Integer.parseInt(s);
                    if(msgNumb == partsCount)
                    {
                        s = resp.substring(4, resp.indexOf("\n"));
                        plotValue += s;
                        try
                        {
                            String ss ;
                            for(int j = 0; j < 24; j++)
                            {
                                ss =  plotValue.substring(j * 4, j * 4 + 4);
                                plot.aBuf[j] = Integer.parseInt(ss.substring(1));
                                if(plotValue.contains("-"))
                                    plot.aBuf[j] *= -1;
                            }
                            if(plotRef == 'I') {plot.aColor = new int[]{150, 102, 204, 255}; inCanvas.invalidate(); }
                            if(plotRef == 'O') {plot.aColor = new int[]{120, 255, 255, 0};   outCanvas.invalidate();}

                            ss = plotValue.substring(plotValue.length()-4, plotValue.length()-1);
                            ss = ss.replace("00", "0") + ".";
                            if (ss.contains("+0") && !ss.contains("+0.")) ss = ss.replace("+0", "+");
                            if (ss.contains("-0") && !ss.contains("-0.")) ss = ss.replace("-0", "-");

                            if(BROADCAST_ACTION.contains("I4"))
                            {
                                inTemp.setText(ss + plotValue.substring(plotValue.length()-1));
                                BROADCAST_ACTION = "O1";
                                SendTask tsk = new SendTask();
                                tsk.execute();
                            }
                            if(BROADCAST_ACTION.contains("O4"))
                            {
                                outTemp.setText(ss + plotValue.substring(plotValue.length() - 1));
                                pbWait.setVisibility(View.INVISIBLE);
                                response.setText("Done");
                            }
                        }
                        catch (Exception e)
                        {
                            Toast t = Toast.makeText(getApplicationContext(),
                                    "Error in data,renewing...", Toast.LENGTH_SHORT);
                            update();
                            t.show();
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
                catch (Exception e)
                {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Error in data, renewing...", Toast.LENGTH_SHORT);
                    update();
                    t.show();
                    pbWait.setVisibility(View.INVISIBLE);
                    return;
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