package com.carlos.eventlibrary;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by carlos on 2016/1/11.
 * 事件机制中的传递分发者
 */
public class EventMailer {
    private static EventMailer missileMailer;
    private Map<String, WeakReference<IEventReceiver>> softList;
    private static MyHandler myHandler;
    private static Queue<EventMail> mails;
    private static boolean isHold;
    private static List<EventMail> eventMailList;
    /**
     * 所能存储的，静态邮件的数量
     */
    private static int staticLength;

    /**
     * 私有构造方法
     */
    private EventMailer() {
        softList = Collections.synchronizedMap(new HashMap<String, WeakReference<IEventReceiver>>());
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
     *                     只能存储一封静态邮件，后者会顶掉前者
     */
    public static void init(boolean isHoldToSend) {
        missileMailer = new EventMailer();
        mails = new ConcurrentLinkedQueue<>();
        myHandler = new MyHandler();
        if (isHoldToSend) {
            isHold = true;
            staticLength = 1;
            if (eventMailList == null)
                eventMailList = Collections.synchronizedList(new ArrayList<EventMail>(staticLength));
        }
    }

    /**
     * 初使化方法
     *
     * @param isHoldToSend 在发送给某个界面EventMail的时候,如果那个界面当前不存在，是否存储信息，直到界面出现主动来拿
     *                     即使存储的有信息，但APP如果关闭了，所有的信息都会清空
     *                     init(false) 等价于 init()
     * @param staticLength 可以存储静态邮件的数量
     */
    public static void init(boolean isHoldToSend, int staticLength) {
        missileMailer = new EventMailer();
        mails = new ConcurrentLinkedQueue<>();
        myHandler = new MyHandler();
        if (isHoldToSend) {
            isHold = true;
            EventMailer.staticLength = staticLength;
            if (eventMailList == null)
                eventMailList = Collections.synchronizedList(new ArrayList<EventMail>(EventMailer.staticLength));
        }
    }

    /**
     * 注册监听，其它IReceiver必须用这个方法注册监听，才可以接收到消息
     *
     * @param iReceiver 要注册的IReceiver
     */
    public void register(IEventReceiver iReceiver) {
        if(softList.containsKey(iReceiver.getClass().getName())){
            EventUtil.writeLog("已经存在" + "  " + iReceiver.getClass().getName() + "   " + softList.get(iReceiver.getClass().getName()).hashCode());
        }
        checkEventMailer();
        WeakReference<IEventReceiver> iReceiverWeakReference = new WeakReference<>(iReceiver);
        softList.put(iReceiver.getClass().getName(), iReceiverWeakReference);
        if(softList.containsKey(iReceiver.getClass().getName())){
            EventUtil.writeLog("存储以后" + "  " + iReceiver.getClass().getName() + "   " + softList.get(iReceiver.getClass().getName()).hashCode());
        }
    }

    /**
     * 用于receiver解除自己的注册
     *
     * @param iReceiver 要解除的IEventReceiver
     */
    public synchronized void unregisterReceiver(IEventReceiver iReceiver) {
        checkEventMailer();
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
    public synchronized boolean sendMail(EventMail mail) {
        checkEventMail(mail);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return sendAction(mail);
        } else {
            mails.offer(mail);
            myHandler.sendEmptyMessage(0);
            return true;
        }
    }

    /**
     * 发送一个只有目标类的空的事件
     *
     * @param address 目标类的类名
     */
    public void sendMail(String address) {
        EventMail eventMail = new EventMail();
        eventMail.setAddress_className(address);
        sendMail(eventMail);
    }

    /**
     * 发送一个不包含目标类的事件
     *
     * @param address 目标类的类名
     * @param flag    事件的标记
     */
    public void sendMail(String address, int flag) {
        EventMail eventMail = new EventMail();
        eventMail.setAddress_className(address);
        eventMail.setFlag(flag);
        sendMail(eventMail);
    }

    /**
     * 发送EventMail，但这个EventMail不会立即发送，而是必须要接收者调用getMyEventMail或者pushMyEventMail才能收到
     *
     * @param eventMail 要发磅的EventMail
     * @return 返回发送结果，为true表示正常
     */
    public synchronized boolean sendStaticMail(EventMail eventMail) {
        checkEventMail(eventMail);
        isHold = true;
        if (eventMailList == null)
            eventMailList = Collections.synchronizedList(new ArrayList<EventMail>(staticLength));
        while (eventMailList.size() >= staticLength) {
            eventMailList.remove(0);
        }
        eventMailList.add(eventMail);
        return true;
    }

    /**
     * 主动来询问是否有自己EventMail
     *
     * @param address_className 自己的地址名，就是EventMail的address_className
     * @return 返回一个List，如果没有EventMail，返回null
     */
    public List<EventMail> getMyEventMail(String address_className) {
        checkEventMailer();
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
        checkEventMailer();
        if (!isHold) return;
        if (eventMailList == null) return;
        List<EventMail> eventMails = null;
        for (EventMail eventMail : eventMailList) {
            if (eventMail.getAddress_className().equals(address_className)) {
                if (eventMails == null) {
                    eventMails = new ArrayList<>();
                }
                eventMails.add(eventMail);
            }
        }
        if (eventMails != null) {
            eventMailList.removeAll(eventMails);
            for (EventMail eventMail : eventMails) {
                sendMail(eventMail);
            }
        }
    }

    private synchronized boolean sendAction(EventMail mail) {
        if (softList != null) {
            if (softList.containsKey(mail.getAddress_className())) {
                EventUtil.writeLog("这里发送邮件" + "   " + mail.getAddress_className());
                EventUtil.writeLog("存储的收件人" + "   " + softList.get(mail.getAddress_className()).hashCode());
                IEventReceiver receiver = softList.get(mail.getAddress_className()).get();
                if (receiver == null) {
                    EventUtil.writeLog("收件人已经被GC回收" + "   " + mail.getAddress_className());
                    if (isHold) {
                        while (eventMailList.size() >= staticLength) {
                            eventMailList.remove(0);
                        }
                        eventMailList.add(mail);
                    }
                } else {
                    receiver.MailBox(mail);
                    if (mail.getDuplicateClassNameList() != null) {
                        for (String address : mail.getDuplicateClassNameList()) {
                            if (softList.containsKey(address)) {
                                IEventReceiver receiverDuplicate = softList.get(address).get();
                                receiverDuplicate.MailBox(mail);
                            }
                        }
                    }
                }
            } else {
                if (isHold) {
                    while (eventMailList.size() >= staticLength) {
                        eventMailList.remove(0);
                    }
                    eventMailList.add(mail);
                }
            }
        }
        return true;
    }

    private static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            while (!mails.isEmpty()) {
                EventMailer.getInstance().sendMail(mails.poll());
            }
        }
    }

    /**
     * 检查EventMail是否有错
     *
     * @param eventMail 要检查的EventMail
     * @return 返回true的时候表示没有错误
     */
    private boolean checkEventMail(EventMail eventMail) {
        checkEventMailer();
        if (eventMail == null) throw new EventMailerException("不能发送null");
        if (eventMail.getAddress_className() == null) {
            throw new EventMailerException("请注明要发送给谁，className不能为空");
        }
        return true;
    }

    /**
     * 检查EventMailer是否被初使化
     */
    private void checkEventMailer() {
        if (missileMailer == null) throw new EventMailerException("你没有初使化，初使化以后才可以发送EventMail");
    }
}
