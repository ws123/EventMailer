package com.carlos.eventlibrary;

import android.text.TextUtils;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/11.
 * 事件机制中的数据传递封装类
 */
public class EventMail {
    /**
     * 要把mail发送给哪个类，要以直接指定类名，是包含包名的完整类名
     * 如：
     * Activity.class.getName()
     * this.getClass().getName()
     */
    private String address_className;
    /**
     * 事件mail的抄送地址
     * 发送的EventMail，可以添加到抄送
     * 抄送地址存储在这个List里面
     */
    private List<String> duplicateClassNameList;

    /**
     * 一个可以自定义的标记符
     */
    private int flag;

    private SparseArray<Object> map;

    /**
     * 可以往mail里添加数据，使用hashmap来存储数据，所以请保持key的唯一性
     *
     * @param key          存储数据的key,是int整型，为了更优更高的内存使用
     * @param object_value 要发送的目标的数据对象
     */
    public void putData(int key, Object object_value) {
        if (map == null) {
            map = new SparseArray<>();
        }
        map.put(key, object_value);
    }

    /**
     * 取出mail里面数据的方法
     *
     * @param key 取数据的key
     * @return 返回数据对象，有可能为null，可以事先检验是否包含这个key
     */
    public Object getData(int key) {
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public String getAddress_className() {
        return address_className;
    }

    public void setAddress_className(String address_className) {
        this.address_className = address_className;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    /**
     * @param duplicateClassName 添加一个抄送的类名
     */
    public void addDuplicate(String duplicateClassName) {
        if (duplicateClassName == null || TextUtils.isEmpty(duplicateClassName))
            throw new EventMailerException("抄送类名不能为null或者为空");
        if (duplicateClassNameList == null) {
            duplicateClassNameList = new ArrayList<>(2);
        }
        duplicateClassNameList.add(duplicateClassName);
    }

    List<String> getDuplicateClassNameList() {
        return duplicateClassNameList;
    }
}
