package com.carlos.eventlibrary;


/**
 * Created by carlos on 2016/3/12.
 * 工具类
 */
class EventUtil {

    /**
     * 检测EventMail要接收者是否实现了IEventReceiver接口
     *
     * @param eventMail 要检查的EventMail
     * @return 返回true，则表示EventMail可以发送，否则不可以
     */
    static boolean interfaceCheck(EventMail eventMail) {
        try {
            Class<?> member = Class.forName(eventMail.getAddress_className());
            for (Class<?> oneInterface : member.getInterfaces()) {
                if (oneInterface.equals(IEventReceiver.class)) {
                    return true;
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
