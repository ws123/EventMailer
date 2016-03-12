package com.carlos.eventmailer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.carlos.eventlibrary.EventMail;
import com.carlos.eventlibrary.EventMailer;
import com.carlos.eventlibrary.IEventReceiver;

/**
 * Created by Administrator on 2016/1/12.
 */
public class FragmentThird extends Fragment implements IEventReceiver {
    private View rootView;
    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_third, container, false);
        textView = (TextView) rootView.findViewById(R.id.textView);
        EventMailer.getInstance().register(this);
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        EventMailer.getInstance().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        EventMailer.getInstance().unregisterReceiver(this);
        super.onDestroy();
    }

    @Override
    public void MailBox(EventMail mail) {
        textView.setText(((String) mail.getData(FragmentThird.class.getName())));
    }
}
