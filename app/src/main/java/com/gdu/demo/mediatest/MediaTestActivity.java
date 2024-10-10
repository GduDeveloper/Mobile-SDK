package com.gdu.gdusocketdemo.main;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.gdu.common.error.GDUError;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.adapter.MediaListAdapter;
import com.gdu.demo.databinding.ActivityMediaTestBinding;
import com.gdu.demo.mediatest.MediaDetailActivity;
import com.gdu.demo.mediatest.MediaVideoPlayActivity;
import com.gdu.media.MediaFile;
import com.gdu.sdk.camera.GDUCamera;
import com.gdu.sdk.camera.GDUMediaManager;
import com.gdu.sdk.products.GDUAircraft;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.sdk.util.FileDownCallback;

import java.util.List;

public class MediaTestActivity extends Activity {


    private ActivityMediaTestBinding viewBinding;

    private GDUMediaManager manager;

    private Handler handler;

    private MediaListAdapter adapter;

    private int showVideoType = 1;


    private String path = "/home/zc/project/payload_SDK/samples/sample_c/module_sample/camera_emu/media_file/PSDK_0003_ORG.jpg";

    private String videoPath = "/home/zc/project/payload_SDK/samples/sample_c/module_sample/camera_emu/media_file/PSDK_0004_ORG.mp4";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMediaTestBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        handler = new Handler();
        initView();

        initData();
    }

    private void initView() {

        ImageView imageView = findViewById(R.id.iv_back);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView textView = findViewById(R.id.tv_title);
        textView.setText("媒体文件测试");

        viewBinding.tvEnterDownModel.setOnClickListener(listener);
        viewBinding.tvRefresh.setOnClickListener(listener);
        viewBinding.tvGetList.setOnClickListener(listener);
        viewBinding.tvGetSmall.setOnClickListener(listener);
        viewBinding.tvGetIcon.setOnClickListener(listener);
        viewBinding.tvGetRaw.setOnClickListener(listener);
        viewBinding.tvVideoPlay.setOnClickListener(listener);
        viewBinding.tvTestFile.setOnClickListener(listener);

        viewBinding.tvVideoPlayPause.setOnClickListener(listener);
        viewBinding.tvVideoPlayStop.setOnClickListener(listener);
        viewBinding.tvVideoPlaySeek.setOnClickListener(listener);

        adapter = new MediaListAdapter();
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 4);
        viewBinding.rvMedia.setLayoutManager(layoutManager);
        viewBinding.rvMedia.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                MediaFile bean = (MediaFile) adapter.getItem(position);

                if (bean == null) {
                    return;
                }
                Intent intent;
                if (bean.getPath().toLowerCase().endsWith(".mp4") || bean.getPath().toLowerCase().endsWith(".h264")) {
                    intent = new Intent(MediaTestActivity.this, MediaVideoPlayActivity.class);
                    intent.putExtra("path", bean.getPath());
                    intent.putExtra("type", showVideoType);
                    intent.putExtra("duration", bean.getDuration());
                } else {
                    intent = new Intent(MediaTestActivity.this, MediaDetailActivity.class);
                    intent.putExtra("path", bean.getPath());
                    intent.putExtra("raw", bean.getPath());
                    intent.putExtra("thum", bean.getPath());
                    intent.putExtra("preview", bean.getPath());
                }
                startActivity(intent);
            }
        });

    }

    private void initData() {
        GDUCamera camera = ((GDUCamera) ((GDUAircraft) SdkDemoApplication.getProductInstance()).getCamera());
        if (camera != null) {
            manager = camera.getMediaManager();
        }
    }


    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.tv_enter_down_model:
                    enterDownModel();
                    break;
                case R.id.tv_refresh:
                    refreshList();

                    break;
                case R.id.tv_get_list:
                    getListFile();
                    break;
                case R.id.tv_test_file:
                    Intent intent = new Intent(MediaTestActivity.this, MediaVideoPlayActivity.class);
                    intent.putExtra("path", videoPath);
                    startActivity(intent);
                    break;
                default:
                    break;
            }

        }
    };


    private void enterDownModel() {

        if (manager == null) {
            toastText("云台未连接");
            return;
        }
        manager.enable(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {
                if (var1 == null) {
                    toastText("开启成功");
                } else {
                    toastText("开启失败");
                }
            }
        });
    }

    private void refreshList() {
        if (manager == null) {
            toastText("云台未连接");
            return;
        }

        manager.refreshMediaList(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError var1) {
                if (var1 == null) {
                    toastText("刷新成功");
                } else {
                    toastText("刷新失败");
                }
            }
        });
    }

    private void getListFile() {

        Log.d("MediaFileList", "getListFile");
        if (manager == null) {
            toastText("云台未连接");
            return;
        }

        manager.getMediaFileList(new FileDownCallback.OnMediaListCallBack() {

            @Override
            public void onStart() {

            }

            @Override
            public void onGetMediaList(List<MediaFile> list) {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (list == null) {
                                return;
                            }
                            adapter.setNewInstance(list);
                            getThum(list, 0);
                            showVideoType = 1;
                        }
                    });
                }
            }


            @Override
            public void onFail(GDUError error) {

            }

        });

    }



    private void getThum(List<MediaFile> mediaFiles, final int index) {

        if (mediaFiles == null || mediaFiles.size() == 0 || index >= mediaFiles.size()) {
            return;
        }

        if (mediaFiles.get(index) == null || mediaFiles.get(index).getPath() == null) {
            return;
        }
        String path = mediaFiles.get(index).getPath();
        manager.getThumbnail(path,"", new FileDownCallback.OnMediaImageCallBack() {
            @Override
            public void onStart() {

            }

            @Override
            public void onGetMediaImage(Bitmap bitmap, byte[] bytes) {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mediaFiles.get(index).setThumbnail(bitmap);
                            adapter.notifyItemChanged(index);
                            if (index < 20 - 1 || index < mediaFiles.size()) {
                                int nextIndex = index + 1;
                                getThum(mediaFiles, nextIndex);
                            }
                        }
                    });
                }
            }

            @Override
            public void onProgress(long l, long l1) {

            }
            @Override
            public void onFail(GDUError gduError) {

                if (index < 20 - 1 || index < mediaFiles.size()) {
                    int nextIndex = index + 1;
                    getThum(mediaFiles, nextIndex);
                }
            }
        });
    }

    private void toastText(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MediaTestActivity.this, content, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.disable(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(GDUError gduError) {

                }
            });
        }
    }
}
