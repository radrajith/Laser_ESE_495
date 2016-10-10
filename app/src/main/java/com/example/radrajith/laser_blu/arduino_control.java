package com.example.radrajith.laser_blu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.Toast;

import java.io.OutputStream;

public class arduino_control extends AppCompatActivity {
    //private EditText sendData;
    private Button send, back;
    private NumberPicker periodPick, dutyPick, pulsesPick;
    OutputStream myOutput;
    int period = 10, duty = 50, pulses = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //sendData = (EditText)findViewById(R.id.textBox);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arduino_control);
        periodPick = (NumberPicker) findViewById(R.id.periodPick);
        periodPick.setMaxValue(100);
        periodPick.setMinValue(1);
        periodPick.setValue(period);
        periodPick.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                period = newVal;
            }
        });

        dutyPick = (NumberPicker) findViewById(R.id.dutyPick);
        dutyPick.setMaxValue(100);
        dutyPick.setMinValue(1);
        dutyPick.setValue(duty);
        dutyPick.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                duty = newVal;
            }
        });

        pulsesPick = (NumberPicker) findViewById(R.id.pulsesPick);
        pulsesPick.setMaxValue(100);
        pulsesPick.setMinValue(0);
        pulsesPick.setValue(pulses);
        pulsesPick.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                pulses = newVal;
            }
        });
        send = (Button) findViewById(R.id.sendButton);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


            }
        });
        back = (Button) findViewById(R.id.backButton);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
//    public arduino_control(MainActivity myact){
//        myact.sendData(period,duty,pulses);
//    }
}