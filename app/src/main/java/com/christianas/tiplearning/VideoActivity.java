package com.christianas.tiplearning;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.christianas.tiplearning.databinding.ActivityVideoBinding;

import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.PlayerListener;
import tcking.github.com.giraffeplayer2.VideoInfo;
import tv.danmaku.ijk.media.player.IjkTimedText;

public class VideoActivity extends AppCompatActivity implements PlayerListener{

    private ActivityVideoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


//        VideoInfo videoInfo = new VideoInfo("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
//                .setTitle("Demo video") //config title
//                .setShowTopBar(true) //show mediacontroller top bar
//                .setPortraitWhenFullScreen(true);//portrait when full screen
//
//        GiraffePlayer.play(this,videoInfo);

    }

    @Override
    public void onPrepared(GiraffePlayer giraffePlayer) {

    }

    @Override
    public void onBufferingUpdate(GiraffePlayer giraffePlayer, int percent) {

    }

    @Override
    public boolean onInfo(GiraffePlayer giraffePlayer, int what, int extra) {
        return false;
    }

    @Override
    public void onCompletion(GiraffePlayer giraffePlayer) {

    }

    @Override
    public void onSeekComplete(GiraffePlayer giraffePlayer) {

    }

    @Override
    public boolean onError(GiraffePlayer giraffePlayer, int what, int extra) {
        return false;
    }

    @Override
    public void onPause(GiraffePlayer giraffePlayer) {

    }

    @Override
    public void onRelease(GiraffePlayer giraffePlayer) {

    }

    @Override
    public void onStart(GiraffePlayer giraffePlayer) {

    }

    @Override
    public void onTargetStateChange(int oldState, int newState) {

    }

    @Override
    public void onCurrentStateChange(int oldState, int newState) {

    }

    @Override
    public void onDisplayModelChange(int oldModel, int newModel) {

    }

    @Override
    public void onPreparing(GiraffePlayer giraffePlayer) {

    }

    @Override
    public void onTimedText(GiraffePlayer giraffePlayer, IjkTimedText text) {

    }

    @Override
    public void onLazyLoadProgress(GiraffePlayer giraffePlayer, int progress) {

    }

    @Override
    public void onLazyLoadError(GiraffePlayer giraffePlayer, String message) {

    }
}