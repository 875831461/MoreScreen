package com.thomas.screen;

import android.app.Presentation;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

/**
 * 副屏
 */
public class CustomPresentation extends Presentation {
    private boolean isChange;
    private int changeCount;

    private TextView tv_vice_change;
    private VideoView videoView;

    boolean isChange() {
        return isChange;
    }

    public void setChange(){
        changeCount ++ ;
        tv_vice_change.setText(String.valueOf(changeCount));
    }


    CustomPresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_presentation);
        tv_vice_change = findViewById(R.id.tv_vice_change);
        videoView = findViewById(R.id.videoView);
        Uri uri = Uri.parse("android.resource://" + getContext().getPackageName() + File.separator + R.raw.test_movie);
        videoView.setVideoURI(uri);
        MediaController mediaController = new MediaController(getContext());
        videoView.setMediaController(mediaController);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });
    }

    /**
     * 副屏发生改变调用(息屏触发)
     */
    @Override
    public void onDisplayChanged() {
        super.onDisplayChanged();
        isChange = true;
        dismiss();
    }
}
