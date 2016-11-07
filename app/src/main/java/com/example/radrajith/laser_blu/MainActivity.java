//package com.jjoe64.graphview;
package com.example.radrajith.laser_blu;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.os.Handler;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;

public class MainActivity extends AppCompatActivity {
    private Button closeButton;
    private Button restartButton;

    private Button graphButton, pulseSetButton;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket mySocket;
    private BluetoothDevice myDevice;
    private Handler myHandler = new Handler();
    private TextView objectTemp;
    private String[] readData;
    private int bytesRead;
    private int count = 0;
    private boolean run = true;
    OutputStream myOutput;
    InputStream myInput;
    Thread thread;
    GraphView graph;
    double buff = 0;
    private Button send, back;
    private NumberPicker periodPick, dutyPick, pulsesPick;
    //OutputStream myOutput;
    int period = 10, duty = 50, pulses = 2;
    PointsGraphSeries<DataPoint> series;
    //ListView deviceList = findViewById(R.id.listView);
    String deviceName = "HC-06";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        objectTemp = (TextView)findViewById(R.id.objectTemp);
        btConfigure();
        closeButton = (Button)findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                closeConn(mySocket);
            }
        });
        restartButton = (Button) findViewById(R.id.restart);
        restartButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                btConfigure();
            }
        });


        graphButton = (Button) findViewById(R.id.graphButton);
        graphButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                graph.addSeries(series);
            }
        });
        graph = (GraphView) findViewById(R.id.graph);
        DataPoint[] data = new DataPoint[]{new DataPoint(0,0)};
        series = new PointsGraphSeries<DataPoint>(new DataPoint[] {
                //new DataPoint(0, 0),
        });

        periodPick = (NumberPicker) findViewById(R.id.periodPick);
        periodPick.setMaxValue(100);
        periodPick.setMinValue(1);
        periodPick.setValue(period);
        periodPick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                period = newVal;
            }
        });

        dutyPick = (NumberPicker) findViewById(R.id.dutyPick);
        dutyPick.setMaxValue(100);
        dutyPick.setMinValue(1);
        dutyPick.setValue(duty);
        dutyPick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                duty = newVal;
            }
        });

        pulsesPick = (NumberPicker) findViewById(R.id.pulsesPick);
        pulsesPick.setMaxValue(100);
        pulsesPick.setMinValue(0);
        pulsesPick.setValue(pulses);
        pulsesPick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                pulses = newVal;
            }
        });
        send = (Button) findViewById(R.id.sendButton);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               sendData(period,duty, pulses);

            }
        });

    }


    public void btConfigure() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Bluetooth adapter not present");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
        }

        if (!btAdapter.isEnabled()) {
            Intent btEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btEnable, 1);
        }
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        ArrayList devicesArray = new ArrayList();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                devicesArray.add(device.getName() + "\n" + device.getAddress());
                if (device.getName().equals(deviceName)) {
                    myDevice = device;
                    Toast.makeText(getApplicationContext(), "connected to "+deviceName, Toast.LENGTH_LONG).show();
                    openBt();
                    break;
                }
            }
        }
        else{
            dummy();
        }
    }
    public void dummy(){
        objectTemp.setText("bluetooth not connectetd");
        onPause();
    }
//code examples used from http://stackoverflow.com/questions/10327506/android-arduino-bluetooth-data-transfer
// and android bluetooth api

    public void openBt() {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        try {
            mySocket = myDevice.createRfcommSocketToServiceRecord(uuid);
            mySocket.connect();
            try {
                myInput = mySocket.getInputStream();
                myOutput = mySocket.getOutputStream();
                Toast.makeText(getApplicationContext(), "BT communication established", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                System.out.println("error at stream");
            }
            receiveData();
        } catch (IOException e) {
            System.out.println("problem at serversocket creation");
            System.out.println("didnt connect");
            Toast.makeText(getApplicationContext(), "cannot connect", Toast.LENGTH_LONG).show();
            dummy();
            //sendData.setText("Didnt Connect restart app");
            }

}
    public void sendData(int period, int duty, int pulses){
        String writeData = period +"," + duty +","+ pulses;
        try {
            myOutput.write(writeData.getBytes());
            Toast.makeText(getApplicationContext(), "Data Sent", Toast.LENGTH_LONG).show();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error sending data", Toast.LENGTH_LONG).show();
        }
    }

    public void receiveData(){
        final byte[] buffer = new byte[9];
        run = true;

        Toast.makeText(getApplicationContext(), "was here", Toast.LENGTH_LONG).show();
        thread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && run) {
                    try {
                        bytesRead = myInput.read(buffer);
                        //readData = new String(buffer).split(" | ");
                        //buffer[bytesRead] = '\0';
                        myHandler.post(new Runnable() {
                            public void run() {
                                count++;
                                String bufferData =  new String(buffer).trim();
                                try {
                                     buff = Double.parseDouble(bufferData);
                                }catch (Exception e){ buff = 0;}
                                System.out.println(new String(buffer) + "  " + buff);
                                //System.out.println(readData[0].trim());
                                //System.out.println(readData[1].trim());
                                objectTemp.setText(bufferData);
                                series.appendData(new DataPoint(count, buff), true ,1000);
                            }
                        });
                        myHandler.obtainMessage(1, bytesRead, -1, buffer).sendToTarget();
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Error reading data", Toast.LENGTH_LONG).show();
                        run = false;
                        break;
                    }
                }

            }
        });
        //
        thread.start();
    }
    public void closeConn(BluetoothSocket mySocket) {
        try {
            mySocket.close();
            myInput.close();
            myOutput.close();
        } catch (IOException e) {
            System.out.println("error at close");
        }
        finish();
    }

}
        /*
        else{
            Toast.makeText(getApplicationContext(),"No paired devices found", Toast.LENGTH_LONG).show();

        }
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, devicesArray);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(deviceListListener);

        //dont forget to add discovering bt device later
    }
    private AdapterView.OnItemClickListener deviceListListener = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView av, View v, int arg1, int arg2){
            String info =((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            Intent i = new Intent(arduinoControl.this, arduinoControl.class )
        }
    }
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ArrayList devicesArray = new ArrayList();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devicesArray.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    registerReceiver(btReceiver, filter);
}
*/
