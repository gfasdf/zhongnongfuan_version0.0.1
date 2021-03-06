package com.zhongnongfuan.app.network;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.zhongnongfuan.app.bean.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author qichaoqun
 * @date 2018/8/26
 * 封装的okHttp的网络请求工具
 */
public class MyNetWork {
    private static MyNetWork mInstance;
    private final OkHttpClient mOkHttpClient;
    private final Handler mHandler;
    public static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");
    //解析服务器返回数据的Gson
    private Gson mGson;

    /**
     * 创建对象时，同时设置构造方法
     * @param context 上下文
     */
    public MyNetWork(Context context) {
        mGson = new Gson();
        File sdcache = context.getExternalCacheDir();
        int cacheSize = 10 * 1024 * 1024;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .cache(new Cache(sdcache.getAbsoluteFile(), cacheSize));
        mOkHttpClient = builder.build();
        mHandler = new Handler();
    }

    /**
     * 获取本类的单例话对象
     * @param context 上下文
     * @return 本类对象
     */
    public static MyNetWork getInstance(Context context){
        if(mInstance == null){
            synchronized (MyNetWork.class){
                if(mInstance == null){
                    mInstance = new MyNetWork(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * get请求方式
     */
    public void getAsynHttp(String path, ResultCallback resultCallback){
        Request request = new Request.Builder()
                .url(path)
                .removeHeader("User-Agent")
                .addHeader("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:0.9.4)")
                .get()
                .build();
        Call call = mOkHttpClient.newCall(request);
        dealResult(call,resultCallback);
    }
    /**
     * post请求方式
     */
    public void postAsynHttp(String path, Map<String, String> map, ResultCallback resultCallback){

        String params = mGson.toJson(map);
        RequestBody body = RequestBody.create(JSON,params);
        //设置请求体
        Request request = new Request.Builder()
                .url(path)
                .removeHeader("User-Agent")
                .addHeader("User-Agent","DTU(V1.0)")
                .post(body)
                .build();
        Call call = mOkHttpClient.newCall(request);
        dealResult(call,resultCallback);
    }


    /**
     * 利用该方法进行网络请求，并获取网络请求是否成功
     * @param call 网络请求的对象
     * @param resultCallback 网络请求的结果
     */
    private void dealResult(Call call, final ResultCallback resultCallback) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailedCallback(call.request(),e,resultCallback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = new String(response.body().bytes(),"utf-8");
                Log.i("", "onResponse: 返回的新的值：：：："+str);
                sendSuccessCallback(str,resultCallback);
            }
        });
    }

    /**
     * 当网络请求成功时，要实现接口中的方法
     * @param str 请求返回的内容
     * @param callback 接口对象，用于访问后的回调
     */
    private void sendSuccessCallback(final String str, final ResultCallback callback){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(callback != null){
                    try {
                        callback.onResponse(str);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 当网络请求失败时，要实现接口中的方法
     * @param request 请求对象
     * @param e 异常对象
     * @param resultCallback 接口对象
     */
    private void sendFailedCallback(final Request request, final Exception e,
                                    final ResultCallback resultCallback){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(resultCallback != null){
                    resultCallback.onError(request,e);
                }
            }
        });
    }
}
