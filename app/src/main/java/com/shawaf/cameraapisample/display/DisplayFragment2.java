package com.shawaf.cameraapisample.display;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.bumptech.glide.Glide;
import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.ui.widget.EMVideoView;
import com.shawaf.cameraapisample.CameraActivity;
import com.shawaf.cameraapisample.R;
import com.shawaf.cameraapisample.utilites.CommonStrings;
import com.shawaf.cameraapisample.utilites.ImageUtilites;
import com.shawaf.cameraapisample.utilites.Utilites;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * Created by Shawaf on 11/9/2016.
 */

public class DisplayFragment2 extends Fragment {
    private View v;
    private Bundle recBundle;
    private String type, path;
    @BindView(R.id.pic_lay)
    View picLay;
    @BindView(R.id.takenphoto_iv)
    ImageView photoIV;
    @BindView(R.id.video_lay)
    View videoLay;
    @BindView(R.id.takenvideo)
    EMVideoView videoView;
    @BindView(R.id.play_lay)
    LinearLayout playLay;
    @BindView(R.id.play_btn)
    ImageView playBtn;

    private int stopPosition = 0;
    private CameraActivity cameraViewActivity;
    private String centerCrop = "crop";
    private String center = "inside";
    private String curPicSet = "crop";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_photo2, container, false);

        getSharedData();
        cameraViewActivity = (CameraActivity) getActivity();
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, getActivity());

        initImageAndVideoView();
        updateStatusBarColor();
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraViewActivity.setTitle(CommonStrings.uploadFile_title);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateStatusBarColor() {
        if (Utilites.isAndroid5()) {
            getActivity().getWindow().setStatusBarColor(0xff111111);
        }
    }

    private void getSharedData() {
        recBundle = this.getArguments();
        type = recBundle.getString(CameraActivity.TYPE_KEY);
        path = recBundle.getString(CameraActivity.PATH_KEY);
    }

    private void initImageAndVideoView() {
        showAndHideViewsLayout(type);
        if (type.equals(CommonStrings.IMG_TYPE)) {
            // Current File is Image
            loadImage(path);
        } else {
            // Current File is Image
            setUpVideoView();
            startVideoPlayer();
        }
    }

    private void showAndHideViewsLayout(String type) {
        switch (type) {
            case CommonStrings.IMG_TYPE:
                picLay.setVisibility(View.VISIBLE);
                videoLay.setVisibility(View.GONE);
                break;
            case CommonStrings.VIDEO_TYPE:
                //Show Video Layout
                picLay.setVisibility(View.GONE);
                videoLay.setVisibility(View.VISIBLE);
                break;
        }
    }

    //Show the selected image from Gallery
    private void loadImage(String imgPath) {
        if (curPicSet.equals(center)) {
            Glide.with(this)
                    .load(imgPath)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .into(photoIV);

            curPicSet = centerCrop;
        } else {
            Glide.with(this)
                    .load(imgPath)
                    .skipMemoryCache(true)
                    .fitCenter()
                    .into(photoIV);
            curPicSet = center;
        }

    }

    private void setUpVideoView() {
        initVideoViewParameters();

    }

    private void initVideoViewParameters() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int h = displaymetrics.heightPixels;
        int w = displaymetrics.widthPixels;
        Uri video = Uri.fromFile(new File(path));
        videoView.setVideoURI(video);
        videoView.setMinimumHeight(h);
        videoView.setMinimumWidth(w);
        videoView.seekTo(100);
        videoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion() {
                // playBtn.setVisibility(View.VISIBLE);
                stopPosition = 0;
                videoView.restart();
            }
        });
    }


    @Nullable
    @Optional
    @OnClick({R.id.play_lay, R.id.wider_btn, R.id.confirm_btn_lay})
    public void onBtnsClicked(View view) {
        switch (view.getId()) {
            case R.id.play_lay:
                playViewClicked();
                break;
            case R.id.wider_btn:
                changeImageviewScaleType();
                break;
            case R.id.confirm_btn_lay:
                uploadFile();
                break;

        }
    }

    private void playViewClicked() {
        String videoPlayerState = getCurrentVideoPlayerState();
        switch (videoPlayerState) {
            case CommonStrings.play_state:
                pauseVideoPlayer();
                break;
            case CommonStrings.pause_state:
                resumeVideoPlayer();
                break;
            case CommonStrings.start_state:
                startVideoPlayer();
                break;

        }
    }

    /**
     * Video Player States
     */
    private String getCurrentVideoPlayerState() {
        if (videoView.isPlaying()) {
            return CommonStrings.play_state;
        } else if (stopPosition != 0) {
            return CommonStrings.pause_state;
        } else {
            return CommonStrings.start_state;
        }
    }

    private void pauseVideoPlayer() {
        videoView.pause();
        stopPosition = videoView.getCurrentPosition();
        playBtn.setVisibility(View.VISIBLE);
    }

    private void resumeVideoPlayer() {
        videoView.seekTo(stopPosition);
        videoView.start();
        playBtn.setVisibility(View.GONE);
    }

    private void startVideoPlayer() {
        playBtn.setVisibility(View.GONE);
        videoView.start();
    }

    //On Wider Button Clicked
    private void changeImageviewScaleType() {
        loadImage(path);
    }

    //Upload File To Server and Close Activity
    public void uploadFile() {
        //Compress Image Before Upload
        if (type.equals("image"))
            ImageUtilites.compressImage(path);
        //Back To Favorite Fragment TO Upload
        cameraViewActivity.closeActivity(path, type);
    }

    @Override
    public void onPause() {
        super.onPause();

    }



}
