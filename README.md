# AudioViewPlayer

该控件是从之前做过的一个项目“南方电网巡视系统”中单独抽取出来的，并进行简单的重构。

实现很简单，继承LinearLayout， 在新类内部将需要的基本控件添加进去， 组成一个组合控件。

### 集成在南网项目中的控件
![image](https://github.com/liyuzhen/AudioViewPlayer/raw/master/screenshots/first.png)

![image](https://github.com/liyuzhen/AudioViewPlayer/raw/master/screenshots/second.png)

### 布局配置
```xml
<com.rdc.liyuzhen.audioviewplayer.view.AudioPlayerView
        android:id="@+id/ap_player"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#1A7D7D7D"
        android:padding="2dp"
        app:innerViewMargin="1dp"
        app:playIcon="@drawable/ic_audio_player_play"
        app:timeTextSize="4sp" />
```

### Java代码中的使用
```java
    mAudioPlayerView = (AudioPlayerView) findViewById(R.id.ap_player);
    mAudioPlayerView.setAudioPath(AUDIO_PATH);  // 将音频文件的路径set进去就好了
```
