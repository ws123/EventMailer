package com.carlos.eventlibrary;


import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

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

    public static String getFormatTime(String formatString, long timeStamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatString, Locale.getDefault());
        return simpleDateFormat.format(timeStamp);
    }


    public static void writeLog(String string) {
//        if (Environment.getExternalStorageState().equals(
//                Environment.MEDIA_MOUNTED)) {
//            File file = new File(Environment.getExternalStorageDirectory().getPath(), "eventMailer");
//            FileWriter fileWriter = null;
//            try {
//                if (!file.exists()) {
//                    file.createNewFile();
//                }
//                fileWriter = new FileWriter(file, true);
//                fileWriter.write("\n" + getFormatTime("MM月dd日 HH:mm:ss",
//                        System.currentTimeMillis()) + "  " + string);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                if (fileWriter != null) {
//                    try {
//                        fileWriter.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
    }
}
