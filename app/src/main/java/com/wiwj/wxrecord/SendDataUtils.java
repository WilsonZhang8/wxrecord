package com.wiwj.wxrecord;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jh on 2017/11/1.
 */

public class SendDataUtils {

    /**
     * okHttp post同步请求
     */
    public static void sendData(String data) {
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

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
               // LogUtil.i("result = "+str);
            }

        });
    }

    public static void main(String args[]) {
        SendDataUtils.sendData("aaaa");
    }
}
