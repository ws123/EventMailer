package com.carlos.eventmailer;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.carlos.eventlibrary.EventMail;
import com.carlos.eventlibrary.EventMailer;
import com.carlos.eventlibrary.IEventReceiver;


/**
 * Created by Administrator on 2016/1/12.
 */
public class FragmentFirst extends Fragment implements IEventReceiver, View.OnClickListener {
    private View rootView;
    private Button buttonOne, buttonTwo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_first, container, false);
        buttonOne = (Button) rootView.findViewById(R.id.button_one);
        buttonTwo = (Button) rootView.findViewById(R.id.button_two);
        buttonTwo.setOnClickListener(this);
        buttonOne.setOnClickListener(this);
        EventMailer.getInstance().register(this);
        return rootView;
    }

    @Override
    public void MailBox(EventMail mail) {

    }

    @Override
    public void onDestroyView() {
        EventMailer.getInstance().unregisterReceiver(this);
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_one:
                Thread thread = new Thread(runnable);
                thread.start();
                break;
            case R.id.button_two:
                EventMail evnetMail2 = new EventMail();
                evnetMail2.setAddress_className(FragmentSecond.class.getName());
                evnetMail2.putData(FragmentSecond.class.getName(), "这个是从UI线程发送");
                EventMailer.getInstance().sendMail(evnetMail2);
                break;
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            EventMail evnetMail = new EventMail();
            evnetMail.setAddress_className(FragmentSecond.class.getName());
            evnetMail.putData(FragmentSecond.class.getName(), "这是从非Ui线程发送的");
            EventMailer.getInstance().sendMail(evnetMail);
        }
    };
}
