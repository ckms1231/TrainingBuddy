package com.example.user.trainingbuddy;

/**
 * Created by Fortune on 4/30/2017.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

public class ImageGalleryFragment extends Fragment {
    private static final String ARGUMENT = "index";
    private static String[] videoResource;
    private int indexNumber;
    public  static ImageGalleryFragment newInstance(int index, String[] _videoResource){
        ImageGalleryFragment imageGalleryFragment = new ImageGalleryFragment();
        Bundle args = new Bundle();
        args.putInt(ARGUMENT, index);
        imageGalleryFragment.setArguments(args);
        videoResource = _videoResource;
        return imageGalleryFragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        indexNumber = getArguments() != null ? getArguments().getInt(ARGUMENT) : 1;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.image_display, container, false);
        VideoView mVideoView = (VideoView) v.findViewById(R.id.display_video);
        String videoResourcePath = videoResource[indexNumber];
        mVideoView.setVideoPath(videoResourcePath);
        mVideoView.setMediaController(new MediaController(getContext()));
        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        mVideoView.start();
        return mVideoView;
    }
    private Bitmap resourceToImageBitmap(int fileResource){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), fileResource);
        return bitmap;
    }
}
