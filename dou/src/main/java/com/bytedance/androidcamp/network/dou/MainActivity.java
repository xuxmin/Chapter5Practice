package com.bytedance.androidcamp.network.dou;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.model.Video;
import com.bytedance.androidcamp.network.lib.util.ImageHelper;
import com.bytedance.androidcamp.network.dou.util.ResourceUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final String TAG = "MainActivity";
    private RecyclerView mRv;
    private List<Video> mVideos = new ArrayList<>();
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    private String studentId;
    private String username;
    public Button mBtn;
    private Button mBtnRefresh;

    private Retrofit retrofit;
    private IMiniDouyinService miniDouyinService;


    // TODO 8: initialize retrofit & miniDouyinService
    private IMiniDouyinService getVideoService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(IMiniDouyinService.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())         // 可以将对象转为json
                    .build();
        }
        if (miniDouyinService == null) {
            miniDouyinService = retrofit.create(IMiniDouyinService.class);
        }
        return miniDouyinService;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRecyclerView();
        initBtns();
        studentId = "317010xxxx";
        username = "Tony";
    }

    private void initBtns() {
        mBtn = findViewById(R.id.btn);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = mBtn.getText().toString();
                if (getString(R.string.select_an_image).equals(s)) {
                    chooseImage();
                } else if (getString(R.string.select_a_video).equals(s)) {
                    chooseVideo();
                } else if (getString(R.string.post_it).equals(s)) {
                    if (mSelectedVideo != null && mSelectedImage != null) {
                        postVideo();
                    } else {
                        throw new IllegalArgumentException("error data uri, mSelectedVideo = "
                                + mSelectedVideo
                                + ", mSelectedImage = "
                                + mSelectedImage);
                    }
                } else if ((getString(R.string.success_try_refresh).equals(s))) {
                    mBtn.setText(R.string.select_an_image);
                }
            }
        });

        mBtnRefresh = findViewById(R.id.btn_refresh);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
        }

        public void bind(final Activity activity, final Video video) {
            ImageHelper.displayWebImage(video.getImageUrl(), img);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VideoActivity.launch(activity, video.getVideoUrl());
                }
            });
        }
    }

    private void initRecyclerView() {
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(new RecyclerView.Adapter<MyViewHolder>() {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new MyViewHolder(
                        LayoutInflater.from(MainActivity.this)
                                .inflate(R.layout.video_item_view, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
                final Video video = mVideos.get(i);
                viewHolder.bind(MainActivity.this, video);
            }

            @Override
            public int getItemCount() {
                return mVideos.size();
            }
        });
    }

    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE);
    }

    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() called with: requestCode = ["
                + requestCode
                + "], resultCode = ["
                + resultCode
                + "], data = ["
                + data
                + "]");

        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d(TAG, "selectedImage = " + mSelectedImage);
                mBtn.setText(R.string.select_a_video);
            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);
                mBtn.setText(R.string.post_it);
            }
        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        File f = new File(ResourceUtils.getRealPath(MainActivity.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    private void postVideo() {
        mBtn.setText("POSTING...");
        mBtn.setEnabled(false);
        final MultipartBody.Part coverImagePart = getMultipartFromUri("cover_image", mSelectedImage);
        final MultipartBody.Part videoPart = getMultipartFromUri("video", mSelectedVideo);

        // TODO 9: post video & update buttons
        Call<Video.PostVideoResponse> call = getVideoService()
                                            .postVideo(studentId, username,coverImagePart,videoPart);
        call.enqueue(new Callback<Video.PostVideoResponse>() {
            @Override
            public void onResponse(Call<Video.PostVideoResponse> call, Response<Video.PostVideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mBtn.setText("select an image");
                    mBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "上传成功", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Video.PostVideoResponse> call, Throwable throwable) {
                mBtn.setText("select an image");
                mBtn.setEnabled(true);
                Toast.makeText(getApplicationContext(), "上传失败", Toast.LENGTH_SHORT).show();
            }
        });

//        new AsyncTask<Object, Integer, String>() {
//            @Override
//            protected String doInBackground(Object... objects) {
//                try {
//                    Response<Video.PostVideoResponse> response = getVideoService()
//                            .postVideo(studentId, username, coverImagePart, videoPart)
//                            .execute();
//                    return response.body().toString();
//                } catch (Exception e) {
//                    return e.getMessage();
//                }
//            }
//
//            @Override
//            protected void onPostExecute(String s) {
//                super.onPostExecute(s);
//                Log.d(TAG, s);
//                mBtn.setText("select an image");
//                mBtn.setEnabled(true);
//                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
//
//            }
//        }.execute();
    }



    public void fetchFeed(View view) {
        mBtnRefresh.setText("requesting...");
        mBtnRefresh.setEnabled(false);
        // TODO 10: get videos & update recycler list

        Call<Video.GetVideoResponse> call = getVideoService()
                .getVideos();
        call.enqueue(new Callback<Video.GetVideoResponse>() {

            @Override
            public void onResponse(Call<Video.GetVideoResponse> call, Response<Video.GetVideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<Video> videos = response.body().getVideos();

                    mVideos.clear();
                    for (int i = 0; i < videos.size(); i ++) {
                        mVideos.add(videos.get(i));
                    }
                    Log.d(TAG, String.valueOf(mVideos.size()));

                    mRv.getAdapter().notifyDataSetChanged();
                    mBtnRefresh.setText("refresh_feed");
                    mBtnRefresh.setEnabled(true);

                    Toast.makeText(getApplicationContext(), "刷新成功", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Video.GetVideoResponse> call, Throwable throwable) {
                mBtnRefresh.setText("refresh_feed");
                mBtnRefresh.setEnabled(true);
                Toast.makeText(getApplicationContext(), "刷新失败, 请检查网络", Toast.LENGTH_SHORT).show();
            }
        });

//        new AsyncTask<Object, Integer, String>() {
//            @Override
//            protected String doInBackground(Object... objects) {
//                try {
//                    Response<Video.GetVideoResponse> response = getVideoService()
//                            .getVideos()
//                            .execute();
//                    List<Video> videos = response.body().getVideos();
//
//                    mVideos.clear();
//                    for (int i = 0; i < videos.size(); i ++) {
//                        mVideos.add(videos.get(i));
//                    }
//                    Log.d(TAG, String.valueOf(mVideos.size()));
//
//                    return response.body().toString();
//                } catch (Exception e) {
//                    return e.getMessage();
//                }
//            }
//
//            @Override
//            protected void onPostExecute(String s) {
//                super.onPostExecute(s);
//                Log.d(TAG, s);
//
//                mRv.getAdapter().notifyDataSetChanged();
//                mBtnRefresh.setText("refresh_feed");
//                mBtnRefresh.setEnabled(true);
//
//                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
//            }
//        }.execute();

    }
}
