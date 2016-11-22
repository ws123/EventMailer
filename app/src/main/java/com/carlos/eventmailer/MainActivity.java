package com.carlos.eventmailer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.carlos.eventlibrary.EventMailer;


public class MainActivity extends AppCompatActivity {
    private FragmentFirst fragmentFirst;
    private FragmentSecond fragmentSecond;
    private FragmentThird fragmentThird;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventMailer.init(true);
        fragmentFirst = new FragmentFirst();
        fragmentSecond = new FragmentSecond();
        fragmentThird = new FragmentThird();
        getSupportFragmentManager().beginTransaction().replace(R.id.firstFragment, fragmentFirst).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.secondFragment, fragmentSecond).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.thirdFragment, fragmentThird).commit();
    }



}
