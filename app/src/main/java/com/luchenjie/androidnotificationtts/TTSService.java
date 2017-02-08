package com.luchenjie.androidnotificationtts;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.LinkedList;

/**
 * Created by luchenjie on 07/02/2017.
 */
public class TTSService extends Service {

    private static final String TAG = TTSService.class.getSimpleName();

    public static LinkedList<String> ttsTextLinkedList = new LinkedList<String>();
    private String tempTTSStr = "";

    private AudioManager mAudioManager;

    private TextToSpeech mTextToSpeech;

    private Thread ttsThread;

    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mTextToSpeech = new TextToSpeech(getApplicationContext(), mOnInitListener);

        ttsThread = new Thread() {

            public void run() {
                Log.i(TAG, "tts thread开始运行...");
                while(true) {
                    if(!MainActivity.bSwitch) {
                        ttsTextLinkedList.clear();
                        continue;
                    }

                    if(!ttsTextLinkedList.isEmpty()) {
                        Log.i(TAG, "检测到消息,请求audiofocus");
                        if (requestAudioFocus()) {
                            tempTTSStr = ttsTextLinkedList.pop();
                            Log.i(TAG, "请求audiofocus成功,开始播放:" + tempTTSStr);
                            mTextToSpeech.speak(tempTTSStr, TextToSpeech.QUEUE_ADD, null);
                            abandonAudioFocus();
                            continue;
                        } else {
                            Log.i(TAG, "请求audiofocus失败");
                        }
                    }

                    try {
                        Thread.sleep(500);
                    } catch(Exception e) {
                        Log.e(TAG, "start service sleep err");
                    }

                }
            }
        };

        startService();

    }

    public void startService() {
        ttsThread.start();
    }

    private boolean requestAudioFocus() {
        if(mOnAudioFocusChangeListener != null) {
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                    mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                        AudioManager.STREAM_NOTIFICATION,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }
        return false;
    }

    private boolean abandonAudioFocus() {
        if(mOnAudioFocusChangeListener != null) {
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                    mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
        return false;
    }

    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.i(TAG, "onAudioFocusChange: AUDIOFOCUS_GAIN");
                    ttsThread.notify();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.i(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS");
                    try {
                        ttsThread.wait();
                    } catch (Exception e) {
                        Log.e(TAG, "thread wait err");
                    }
                    abandonAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.i(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS_TRANSIENT");
                    ttsThread.notify();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.i(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    ttsThread.notify();
                    break;
            }
        }
    };

    TextToSpeech.OnInitListener mOnInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int i) {

        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return new TTSServiceBinder();
    }

    public class TTSServiceBinder extends Binder{

        public TTSService getService() {
            return TTSService.this;
        }
    }

}
