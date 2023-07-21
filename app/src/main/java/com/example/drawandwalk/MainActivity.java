package com.example.drawandwalk;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.mylibrary.Location;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button draw=findViewById(R.id.draw);
        draw.setOnClickListener(view->{
            System.out.println("Draw Button has been pressed");
            Intent intent= new Intent(MainActivity.this,Draw.class);
            startActivity(intent);
        });

        Button walk=findViewById(R.id.walk);
        walk.setOnClickListener(view->{
//            Intent intent= new Intent(MainActivity.this,Walk.class);
//            startActivity(intent);
            Location l=new Location();
            System.out.println("Walk Button has been pressed");
            l.StartLocationService(MainActivity.this,1.00f);
        });
       Button stop=findViewById(R.id.stop);
        stop.setOnClickListener(view ->{
            Location l = new Location();
            System.out.println("Stop Button has been pressed");
            l.StopLocationService(MainActivity.this);

                });
    }
}