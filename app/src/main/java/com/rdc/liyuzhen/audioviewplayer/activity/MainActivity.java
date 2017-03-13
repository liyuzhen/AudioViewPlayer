package com.rdc.liyuzhen.audioviewplayer.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.rdc.liyuzhen.audioviewplayer.R;
import com.rdc.liyuzhen.audioviewplayer.view.AudioPlayerView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // 测试路径
    private static final String AUDIO_PATH = Environment.getExternalStorageDirectory()
            + "/netease/cloudmusic/Music/R3hab Ella Vos - White Noise (R3hab Remix).mp3";

    private AudioPlayerView mAudioPlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAudioPlayerView = (AudioPlayerView) findViewById(R.id.ap_player);
        mAudioPlayerView.setAudioPath(AUDIO_PATH);
    }
}
