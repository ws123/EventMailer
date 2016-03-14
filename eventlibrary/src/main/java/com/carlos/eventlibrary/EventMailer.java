package com.carlos.eventlibrary;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Administrator on 2016/1/11.
 * 事件机制中的传递分发者
 */
public class EventMailer {
    private static EventMailer missileMailer;
    private Map<String, SoftReference<IEventReceiver>> softList;
    private static MyHandler myHandler;
    private static Queue<EventMail> mails;
    private static boolean isHold;
    private static List<EventMail> eventMailList;

    /**
     * 私有构造方法
     */
    private EventMailer() {
        softList = Collections.synchronizedMap(new HashMap<String, SoftReference<IEventReceiver>>());
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
     * 初使化方法
     *
     * @param isHoldToSend 在发送给某个界面EventMail的时候,如果那个界面当前不存在，是否存储信息，直到界面出现主动来拿
     *                     即使存储的有信息，但APP如果关闭了，所有的信息都会清空
     *                     init(false) 等价于 init()
     */
    public static void init(boolean isHoldToSend) {
        missileMailer = new EventMailer();
        mails = new ConcurrentLinkedQueue<>();
        myHandler = new MyHandler();
        if (isHoldToSend) {
            isHold = true;
            eventMailList = Collections.synchronizedList(new ArrayList<EventMail>());
        }
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
        SoftReference<IEventReceiver> iReceiverWeakReference = new SoftReference<>(iReceiver);
        softList.put(iReceiver.getClass().getName(), iReceiverWeakReference);
    }

    /**
     * 用于receiver解除自己的注册
     *
     * @param iReceiver 要解除的IEventReceiver
     */
    public synchronized void unregisterReceiver(IEventReceiver iReceiver) {
        if (missileMailer == null) throw new EventMailerException("没有初使化EventMailer");
        if (softList != null && softList.containsKey(iReceiver.getClass().getName())) {
            softList.remove(iReceiver.getClass().getName());
        }
    }

    /**
     * 发送EventMail的方法
     *
     * @param mail 要发送的EventMail
     * @return 返回是否发送成功，如果为true，表示发送成功，否则发送失败
     */
    public boolean sendMail(EventMail mail) {
        if (missileMailer == null) throw new EventMailerException("你没有初使化，初使化以后才可以发送EventMail");
        if (mail == null) throw new EventMailerException("不能发送null");
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return sendAction(mail);
        } else {
            mails.offer(mail);
            myHandler.sendEmptyMessage(0);
            return true;
        }
    }

    /**
     * 主动来询问是否有自己EventMail
     *
     * @param address_className 自己的地址名，就是EventMail的address_className
     * @return 返回一个List，如果没有EventMail，返回null
     */
    public List<EventMail> getMyEventMail(String address_className) {
        if (missileMailer == null) throw new EventMailerException("你没有初使化，初使化以后才可以发送EventMail");
        if (!isHold) return null;
        if (eventMailList == null) return null;
        List<EventMail> eventMails = null;
        for (EventMail eventMail : eventMailList) {
            if (eventMail.getAddress_className().equals(address_className)) {
                if (eventMails == null) {
                    eventMails = new ArrayList<>();
                }
                eventMails.add(eventMail);
            }
        }
        if (eventMails == null) {
            return null;
        } else {
            eventMailList.removeAll(eventMails);
            return eventMails;
        }
    }

    /**
     * 也是界面可以检查是否有自己的EventMail，但如果有的话，不是直接返回，而是push到了MainBox方法里
     *
     * @param address_className 自己的地址名，就是EventMail的address_className
     */
    public void pushMyEventMail(String address_className) {
        if (missileMailer == null) throw new EventMailerException("你没有初使化，初使化以后才可以发送EventMail");
        if (!isHold) return;
        if (eventMailList == null) return;
        List<EventMail> eventMails = null;
        for (EventMail eventMail : eventMailList) {
            if (eventMail.getAddress_className().equals(address_className)) {
                if (eventMails == null) {
                    eventMails = new ArrayList<>();
                }
                sendMail(eventMail);
                eventMails.add(eventMail);
            }
        }
        if (eventMails != null) eventMailList.removeAll(eventMails);
    }

    private synchronized boolean sendAction(EventMail mail) {
        if (mail.getAddress_className() == null) {
            throw new EventMailerException("请注明要发送给谁，className不能为空");
        }
        if (softList != null) {
            if (softList.containsKey(mail.getAddress_className())) {
                IEventReceiver receiver = softList.get(mail.getAddress_className()).get();
                if (receiver == null) {
                    if (isHold) {
                        eventMailList.add(mail);
                    }
                } else {
                    receiver.MailBox(mail);
                }
            } else {
                if (isHold) {
                    eventMailList.add(mail);
                }
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
