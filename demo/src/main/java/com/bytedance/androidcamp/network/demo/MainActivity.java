package com.bytedance.androidcamp.network.demo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bytedance.androidcamp.network.demo.model.Cat;
import com.bytedance.androidcamp.network.demo.newtork.ICatService;
import com.bytedance.androidcamp.network.demo.utils.NetworkUtils;
import com.bytedance.androidcamp.network.lib.util.ImageHelper;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    public static String CAT_JSON =
            "{\"breeds\":[],\"id\":\"293\",\"url\":\"https://cdn2.thecatapi.com/images/293.jpg\",\"width\":429,\"height\":500}";

    private Retrofit retrofit;
    private ICatService catService;
    private Gson gson;

    public TextView tvOut;
    public ImageView ivOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvOut = findViewById(R.id.tv_out);
        ivOut = findViewById(R.id.iv_out);
    }

    public void testJSONObject(View view) {
        // TODO 1: Parse CAT_JSON using JSONObject
        try {

            JSONObject cat = new JSONObject(CAT_JSON);
            String id = cat.getString("id");
            tvOut.setText(id);

        } catch (JSONException ex) {
            tvOut.setText("网络错误");
        }
    }

    public void testGson(View view) {
        // TODO 2: Parse CAT_JSON using Gson
        getGson();
        Cat cat = gson.fromJson(CAT_JSON, Cat.class);
        String url = cat.getUrl();
        Log.d("MainActivity", url);
        ImageHelper.displayWebImage(url, ivOut);
    }

    public void testHttpURLConnectionSync(View view) {
        // TODO 4: Fix crash of NetworkOnMainThreadException

//        HttpConnectionAsyncTask asyncTask = new HttpConnectionAsyncTask(this);
//        asyncTask.execute();
        new AsyncTask<Object, Integer, String>() {
            @Override
            protected String doInBackground(Object... objects) {
                return NetworkUtils.getResponseWithHttpURLConnection(ICatService.HOST + ICatService.PATH);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                tvOut.setText(s);
            }
        }.execute();
    }

    public void testRetrofitSync(View view) throws Exception {
        // TODO 5: Making request in retrofit

//        RetrofitAsyncTask asyncTask = new RetrofitAsyncTask(this);
//        asyncTask.execute();
        new AsyncTask<Object, Integer, String>() {

            @Override
            protected String doInBackground(Object... objects) {
                try{
                    Response<List<Cat>> response = getCatService().randomCat(1).execute();
                    // Call<List<Cat>> call = getCatService().randomCat(1);
                    // return  call.execute().body().get(0).toString();
                    return response.body().get(0).toString();

                } catch (Exception ex) {
                    return ex.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                tvOut.setText(s);
            }
        }.execute();

    }

    public void testUpdateUI(View view) {
        // TODO 6: Fix crash of CalledFromWrongThreadException
        new Thread() {
            @Override public void run() {
                final String s = NetworkUtils.getResponseWithHttpURLConnection(ICatService.HOST + ICatService.PATH);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvOut.setText(s);   // 不能在非主线程更新 UI
                    }
                });
            }
        }.start();
    }

    public void testHttpURLConnectionAsync(View view) {
        // HttpURLConnection Async
        new Thread() {
            @Override public void run() {
                final String s = NetworkUtils.getResponseWithHttpURLConnection(ICatService.HOST + ICatService.PATH);
                try {
                    JSONArray cats = new JSONArray(s);
                    JSONObject cat = cats.getJSONObject(0);
                    final String id = cat.getString("id");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvOut.setText(id);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "retrofit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
    }

    public void testRetrofitAsync(View view) {
        Call<List<Cat>> call = getCatService().randomCat(1);
        call.enqueue(new Callback<List<Cat>>() {
            @Override
            public void onResponse(Call<List<Cat>> call, Response<List<Cat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Cat> cats = response.body();
                    Cat cat = cats.get(0);
                    tvOut.setText(cat.getUrl());
                }
            }

            @Override
            public void onFailure(Call<List<Cat>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "retrofit: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    private ICatService getCatService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(ICatService.HOST)
                    .addConverterFactory(GsonConverterFactory.create())         // 可以将对象转为json
                    .build();
        }
        if (catService == null) {
            catService = retrofit.create(ICatService.class);
        }
        return catService;
    }



    static private class HttpConnectionAsyncTask extends AsyncTask<Object, Integer, String> {

        private final WeakReference<MainActivity> weakActivity;

        private HttpConnectionAsyncTask(MainActivity activity) {
            this.weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(Object[] objects) {
            return NetworkUtils.getResponseWithHttpURLConnection(ICatService.HOST + ICatService.PATH);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            MainActivity activity = weakActivity.get();
            // 判断 activity 是否存在
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return ;
            }
            activity.tvOut.setText(s);
        }
    }

    static private class RetrofitAsyncTask extends AsyncTask<Object, Integer, String> {

        private final WeakReference<MainActivity> weakActivity;

        private RetrofitAsyncTask(MainActivity activity) {
            this.weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(Object[] objects) {
            try{
                final Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(ICatService.HOST)
                        .addConverterFactory(GsonConverterFactory.create())         // 可以将对象转为json
                        .build();
                ICatService catService = retrofit.create(ICatService.class);
                Response<List<Cat>> response = catService.randomCat(1).execute();
                List<Cat> cats = response.body();
                return cats.get(0).toString();

            } catch (Exception ex) {
                return ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            MainActivity activity = weakActivity.get();
            // 判断 activity 是否存在
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return ;
            }
            super.onPostExecute(s);
            activity.tvOut.setText(s);
        }
    }
}