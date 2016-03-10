package com.carlos.eventlibrary;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Administrator on 2016/1/11.
 * 事件机制中的传递分发者
 */
public class EventMailer {
    private static EventMailer missileMailer;
    private Map<String, WeakReference<IEventReceiver>> weakList;
    private static MyHandler myHandler;
    private static Queue<EventMail> mails;

    /**
     * 私有构造方法
     */
    private EventMailer() {
        weakList = Collections.synchronizedMap(new HashMap<String, WeakReference<IEventReceiver>>());
    }

    /**
     * 单例方法
     *
     * @return 返回MissileMailer的单例
     */
    public static EventMailer getInstance() {
        return missileMailer;
    }

    /**
     * 初使化MissleMailer，只有初使化以后才可以发出消息或者接收消息
     */
    public static void init() {
        missileMailer = new EventMailer();
        mails = new ConcurrentLinkedQueue<>();
        myHandler = new MyHandler();
    }

    /**
     * 注册监听，其它IReceiver必须用这个方法注册监听，才可以接收到消息
     *
     * @param iReceiver 要注册的IReceiver
     */
    public void register(IEventReceiver iReceiver) {
        if (missileMailer == null) {
            throw new EventMailerException("没有初使化EventMailer");
        }
        WeakReference<IEventReceiver> iReceiverWeakReference = new WeakReference<>(iReceiver);
        weakList.put(iReceiver.getClass().getName(), iReceiverWeakReference);
    }

    /**
     * 用于receiver解除自己的注册
     *
     * @param iReceiver 要解除的IEventReceiver
     */
    public synchronized void unregisterReceiver(IEventReceiver iReceiver) {
        if (weakList != null && weakList.containsKey(iReceiver.getClass().getName())) {
            weakList.remove(iReceiver.getClass().getName());
        }
    }

    /**
     * 发送EventMail的方法
     *
     * @return 返回是否发送成功，如果为true，表示发送成功，否则发送失败
     */
    public boolean sendMail(EventMail mail) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return sendAction(mail);
        } else {
            mails.offer(mail);
            myHandler.sendEmptyMessage(0);
            return true;
        }
    }

    private synchronized boolean sendAction(EventMail mail) {
        if (mail.getAddress_className() == null) {
            throw new EventMailerException("请注明要发送给谁，className不能为空");
        }
        if (weakList != null) {
            if (weakList.containsKey(mail.getAddress_className())) {
                IEventReceiver receiver = weakList.get(mail.getAddress_className()).get();
                receiver.MailBox(mail);
            }
        }
        return true;
    }

    static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            while (!mails.isEmpty()) {
                EventMailer.getInstance().sendMail(mails.poll());
            }
        }
    }
}
