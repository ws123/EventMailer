package com.carlos.eventlibrary;

/**
 * Created by Administrator on 2016/1/11.
 * 事件框架异常类
 */
public class EventMailerException extends RuntimeException {
    public EventMailerException() {
        super();
    }

    public EventMailerException(String detailMessage) {
        super(detailMessage);
    }

    public EventMailerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public EventMailerException(Throwable throwable) {
        super(throwable);
    }
}
