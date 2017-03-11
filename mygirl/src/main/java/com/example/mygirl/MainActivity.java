package com.example.mygirl;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener {


    @BindView(R.id.lv_girl)
    ListView mLvGirl;
    private String tag = "MainActivity";
    private boolean isLoading=false;
    private List<MyResultBean.ResultsBean> mListData = new ArrayList<>();
    private Gson mGson = new Gson();
    private GirlAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
//        sendSyncRequest();
        sendAsyncRequest();

        mAdapter = new GirlAdapter();
        mLvGirl.setAdapter(mAdapter);
        setListener();
    }

    private void setListener() {
        mLvGirl.setOnScrollListener(this);
    }

    private void sendAsyncRequest() {
        OkHttpClient http = new OkHttpClient();
        String url = "http://gank.io/api/data/福利/10/1";
        Request request = new Request.Builder().get().url(url).build();
        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String body = response.body().string();
                MyResultBean bean = mGson.fromJson(body, MyResultBean.class);
                mListData.addAll(bean.getResults());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        mAdapter.notifyDataSetChanged();
                    }
                });
                Log.d(tag, "bean==" + bean.getResults().get(0).getUrl().toString());


            }
        });
    }


    private void sendSyncRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient http = new OkHttpClient();
                String url = "http://gank.io/api/data/福利/10/1";
                Request request = new Request.Builder().get().url(url).build();
                try {
                    Response response = http.newCall(request).execute();
                    Log.d(tag, "sendSyncRequest=" + response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //  test
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState==SCROLL_STATE_IDLE){
            if(mLvGirl.getLastVisiblePosition()==mListData.size()-1 && !isLoading){
                loadMore();
            }
        }
    }

    private void loadMore() {
        isLoading=true;
        OkHttpClient http = new OkHttpClient();
        int i = mListData.size() / 10 + 1;
        String url = "http://gank.io/api/data/福利/10/"+i;
        Request request = new Request.Builder().get().url(url).build();
        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String body = response.body().string();
                MyResultBean bean = mGson.fromJson(body, MyResultBean.class);
                mListData.addAll(bean.getResults());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        mAdapter.notifyDataSetChanged();
                    }
                });
                Log.d(tag, "bean==" + bean.getResults().get(0).getUrl().toString());

                isLoading=false;

            }
        });
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    private class GirlAdapter extends BaseAdapter{

        public GirlAdapter() {
            super();
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;
            if(convertView==null){
                holder=new ViewHolder();
                convertView=View.inflate(MainActivity.this,R.layout.myadapter_girl,null);
                holder.iv= (ImageView) convertView.findViewById(R.id.iv);
                holder.tv= (TextView) convertView.findViewById(R.id.tv);
                convertView.setTag(holder);
            }else{
                holder= (ViewHolder) convertView.getTag();
            }
            MyResultBean.ResultsBean bean = mListData.get(position);
            holder.tv.setText(bean.getPublishedAt());
            Log.d(tag,"getPublishedAt=="+bean.getPublishedAt());
            String url = bean.getUrl();
            Glide.with(MainActivity.this).load(url).centerCrop().bitmapTransform(new CropCircleTransformation(MainActivity.this)).into(holder.iv);

            return convertView;
        }
    }

    static class ViewHolder{
        ImageView iv;
        TextView tv;
    }

}
