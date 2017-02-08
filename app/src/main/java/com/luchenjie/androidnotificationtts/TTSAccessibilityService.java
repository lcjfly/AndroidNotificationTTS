package com.luchenjie.androidnotificationtts;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;

/**
 * Created by luchenjie on 07/02/2017.
 */
public class TTSAccessibilityService extends AccessibilityService {

    private static final String TAG = TTSAccessibilityService.class.getSimpleName();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                handleNotification(event);
                break;
        }
    }

    private void handleNotification(AccessibilityEvent event) {
        List<CharSequence> texts = event.getText();
        if(MainActivity.bSwitch && !texts.isEmpty()) {
            for(CharSequence text : texts) {
                String ttsText = text.toString().replaceFirst(":", "说");
                Log.i(TAG, "消息放入队列:"+ttsText);
                TTSService.ttsTextLinkedList.push(ttsText);
            }
        }
    }

    @Override
    public void onInterrupt() {

    }


}
