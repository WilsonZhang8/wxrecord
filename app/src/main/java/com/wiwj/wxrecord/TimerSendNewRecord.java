package com.wiwj.wxrecord;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zghw on 2017/11/2.
 */

public class TimerSendNewRecord {
    private final Timer timer = new Timer();

    public void timerStart(String password) {
        TimerTask task = new TimerTaskNewRecord(password);
        LogUtil.i("启动定时任务。。。。");
        //定时任务时间设置
        timer.schedule(task, 1000, 1000*60*2);
    }

    public void timerCancel() {
        timer.cancel();
    }

}
