package com.carlos.eventmailer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.carlos.eventlibrary.EventMail;
import com.carlos.eventlibrary.EventMailer;
import com.carlos.eventlibrary.IEventReceiver;

import java.util.List;

/**
 * Created by Administrator on 2016/3/12.
 */
public class SecondActivity extends AppCompatActivity implements IEventReceiver {
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_second);
        EventMailer.getInstance().register(this);
        textView = (TextView) findViewById(R.id.textView);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<EventMail> eventMails = EventMailer.getInstance().getMyEventMail(SecondActivity.class.getName());
                if (eventMails == null) return;
                textView.setText("这是主动从EventMailer那里拿的数据");
                for (EventMail eventMail : eventMails) {
                    textView.setText(textView.getText() + "\n" + eventMail.getData(SecondActivity.class.getName()));
                }
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("这是要求EventMailer发送到邮箱的数据");
                EventMailer.getInstance().pushMyEventMail(SecondActivity.class.getName());
            }
        });
    }

    @Override
    protected void onDestroy() {
        EventMailer.getInstance().unregisterReceiver(this);
        super.onDestroy();
    }

    @Override
    public void MailBox(EventMail mail) {
        textView.setText(textView.getText() + "\n" + mail.getData(SecondActivity.class.getName()));
    }
}
