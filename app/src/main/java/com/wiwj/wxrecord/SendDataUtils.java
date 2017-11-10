package com.wiwj.wxrecord;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.wiwj.wxrecord.domain.Qun;
import com.wiwj.wxrecord.oracle.JdbcUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by zghw on 2017/11/1.
 */

public class SendDataUtils {

    /**
     * okHttp post同步请求
     */
    public static void sendData(final String data) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("data", data)
                .build();
        Request request = new Request.Builder()
                .url("http://www.laiduer.com/wx/recevieData")
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.i("发送失败！ " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                LogUtil.i("发送成功,响应结果 = " + str);
                if ("ok".equals(str)) {
                    try {
                        //试着转换为
                        Gson gson = new Gson();
                        Map<String, Object> jsonMap = gson.fromJson(data, Map.class);
                        Qun qun = (Qun) jsonMap.get("qun");
                        String talker = qun.getQunId();
                        SharedPreferences sharedPreferences = MyApplication.getSharedPreferences();
                        String key = talker + "lastMsgSeq";
                        String keyPre = talker + "lastMsgSeqPre";
                        String lastMsgSeqPre = sharedPreferences.getString(keyPre, "0");
                        LogUtil.i("===预备消息序列号" + keyPre + "=" + lastMsgSeqPre);
                        if (!"0".equals(lastMsgSeqPre)) {
                            //记录最后一个消息标志
                            SharedPreferences.Editor editor = sharedPreferences.edit(); //获取编辑器
                            //最后更新最后的msgSeq值
                            editor.putString(key, lastMsgSeqPre);
                            //预加载重置为0
                            editor.putString(keyPre, "0");
                            editor.commit();
                            LogUtil.i("===最后消息序列号" + key + "=" + sharedPreferences.getString(key, "0"));
                        }
                    } catch (Exception e) {
                        //啥也不做 容错处理
                    }
                }
            }
        });
    }

    public static void storeData(Map<String, Object> data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JdbcUtil jdbcUtil = new JdbcUtil();

                try {
                    jdbcUtil.getConnection();
                    List<Map<String, Object>> result = jdbcUtil.findResult(
                            "select * from EX_A_UPDATE_RECORD", null);
                    for (Map<String, Object> m : result) {
                        System.out.println(m);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    jdbcUtil.releaseConn();
                }
            }
        }).start();
    }

    public static void main(String args[]) {
        SendDataUtils.sendData("aaaa");
    }
}
