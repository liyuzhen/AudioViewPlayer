package com.rdc.liyuzhen.audioviewplayer.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rdc.liyuzhen.audioviewplayer.R;
import com.rdc.liyuzhen.audioviewplayer.utils.ToastUtil;

import java.io.IOException;

/**
 * 播放音频的组合View
 */
public class AudioPlayerView extends LinearLayout implements View.OnClickListener, MediaPlayer.OnPreparedListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {
    private static final String TAG = "AudioPlayerView";
    private static final String NO_AUDIO_DATA = "暂无音频资源";
    private static final String DEFAULT_TIME_TEXT = "00:00";
    private static final String RESOURCE_NOT_READY = "资源未准备好，请稍后重试";

    private TextView mTvTime;
    private SeekBar mSbProgress;
    private ImageView mIvIcon;

    private Context mContext;
    private Resources mResources;
    private Thread mTimeListenerThread;
    private MediaPlayer mMediaPlayer;

    private boolean mIsPlaying = false;   // 标记是否正在播放
    private boolean mIsAbleToPlay = false;  // 标记音频资源是否已经准备好播放了
    private boolean mIsDragging = false; // 标记是否正在拖拽
    private boolean mIsSettingData = false;  // 标记是否设置了音频资源

    private float mTextSize;
    private int mCurProgress;  // 当前进度
    private int mInnerViewMargin; // 内置View的外边距
    private int mPlayIconId;

    private android.os.Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            syncProgress();
        }
    };

    public AudioPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        mResources = getResources();
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        initView();
    }

    /**
     * 初始化所有参数
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AudioPlayerView);
        if (typedArray != null) {
            mTextSize = typedArray.getDimension(R.styleable.AudioPlayerView_timeTextSize, 10);
            mInnerViewMargin = (int) typedArray.getDimension(R.styleable.AudioPlayerView_innerViewMargin, 10);
            mPlayIconId = typedArray.getResourceId(R.styleable.AudioPlayerView_playIcon, 0);
            typedArray.recycle();
        }
    }

    private void initView() {
        mIvIcon = new ImageView(mContext);
        mTvTime = new TextView(mContext);
        mSbProgress = new SeekBar(mContext);

        setParams();
        setDefaultValue();
        setListener();

        addView(mIvIcon);
        addView(mTvTime);
        addView(mSbProgress);
    }

    private void setListener() {
        mIvIcon.setOnClickListener(this);
        mSbProgress.setOnSeekBarChangeListener(this);
    }

    /**
     * 设置LayoutParams
     */
    private void setParams() {
        // 设置外边距
        LayoutParams ivParams = new LayoutParams((int) (mResources.getDimension(R.dimen.audio_player_icon_size)),
                (int) (mResources.getDimension(R.dimen.audio_player_icon_size)));
        LayoutParams tvParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams sbParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        tvParams.leftMargin = mInnerViewMargin;
        sbParams.leftMargin = mInnerViewMargin;

        mIvIcon.setLayoutParams(ivParams);
        mTvTime.setLayoutParams(tvParams);
        mSbProgress.setLayoutParams(sbParams);
    }

    /**
     * 设置默认值
     */
    private void setDefaultValue() {
        mTvTime.setText(DEFAULT_TIME_TEXT);
        mIvIcon.setImageResource(mPlayIconId);
        mTvTime.setTextSize(mTextSize);
    }

    @Override
    public void onClick(View v) {
        if (v == mIvIcon) {
            if (mIsPlaying) {
                stopPlayingAudio();
            } else {
                if (mIsAbleToPlay) {
                    startPlayingAudio();
                } else {
                    if (mIsSettingData) {
                        ToastUtil.showToast(mContext, RESOURCE_NOT_READY);
                    } else {
                        ToastUtil.showToast(mContext, NO_AUDIO_DATA);
                    }
                }
            }
        }
    }

    /**
     * 停止播放音频
     */
    private void stopPlayingAudio() {
        mIsPlaying = false;
        mIvIcon.setSelected(false);

        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    /**
     * 开始播放音频
     */
    private void startPlayingAudio() {
        mIsPlaying = true;

        mIvIcon.setSelected(true);

        startTimeListener();

        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    /**
     * 启动子线程实时更改播放时间
     */
    private void startTimeListener() {
        if (mTimeListenerThread == null) {
            mTimeListenerThread = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (mIsPlaying && !mIsDragging) {
                            mHandler.sendEmptyMessage(0);
                        }
                    }
                }
            };
            mTimeListenerThread.start();
        }
    }

    /**
     * 同步进度条进度
     */
    private void syncProgress() {
        mCurProgress = mMediaPlayer.getCurrentPosition();
        int second = mCurProgress / 1000;
        mTvTime.setText(formatTime(second));
        mSbProgress.setProgress(mCurProgress);
    }

    /**
     * 格式化时间
     *
     * @param time 以秒为单位的时间
     */
    private String formatTime(int time) {
        StringBuilder timeStringBuilder = new StringBuilder();
        int min = time / 60;
        int second = time % 60;

        if (min < 10) {
            timeStringBuilder.append("0");
        }
        timeStringBuilder.append(min);
        timeStringBuilder.append(":");

        if (second < 10) {
            timeStringBuilder.append("0");
        }
        timeStringBuilder.append(second);

        return timeStringBuilder.toString();
    }

    /**
     * 设置要播放的音频文件
     */
    public void setAudioPath(String audioPath) {
        Log.e(TAG, "audioPath : " + audioPath);
        if (TextUtils.isEmpty(audioPath)) {
            mIsSettingData = false;
            return;
        } else {
            mIsSettingData = true;
        }

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
        } else {
            resetState();
        }

        try {
            mMediaPlayer.setDataSource(audioPath);
            mMediaPlayer.prepareAsync();
            mIsAbleToPlay = false;
        } catch (IOException e) {
            mIsSettingData = false;
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsAbleToPlay = true;

        mSbProgress.setMax(mMediaPlayer.getDuration());  // 设置歌曲长度
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // 设置时间变化
        if (fromUser) {
            int second = progress / 1000;
            mTvTime.setText(formatTime(second));

            if (mIsSettingData) {
                mCurProgress = progress;
            } else {
                mCurProgress = 0;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.e(TAG, "onStartTrackingTouch");
        mIsDragging = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.e(TAG, "onStopTrackingTouch");
        mIsDragging = false;

        mSbProgress.setProgress(mCurProgress);
        mMediaPlayer.seekTo(mCurProgress);
        mTvTime.setText(formatTime(mCurProgress / 1000));
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        resetState();
    }

    /**
     * 重置各个状态
     */
    public void resetState() {
        mIsPlaying = false;
        mTvTime.setText(DEFAULT_TIME_TEXT);
        mSbProgress.setProgress(0);
        mIvIcon.setSelected(false);
        mCurProgress = 0;
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.reset();
        }
    }
}