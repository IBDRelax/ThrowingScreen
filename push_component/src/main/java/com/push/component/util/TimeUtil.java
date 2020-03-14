package com.push.component.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具
 *
 * @author relax
 * @date 2020/3/14 12:56 PM
 */
public class TimeUtil {

    public static String getTimeStr() {
        SimpleDateFormat format = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");

        return format.format(new Date());
    }

}
