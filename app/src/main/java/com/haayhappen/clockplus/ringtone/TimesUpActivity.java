/*
 * Copyright (C) 2016 Phillip Hsu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haayhappen.clockplus.ringtone;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.view.ViewGroup;

import com.haayhappen.clockplus.ringtone.playback.RingtoneService;
import com.haayhappen.clockplus.ringtone.playback.TimerRingtoneService;
import com.haayhappen.clockplus.timers.ui.CountdownChronometer;
import com.haayhappen.clockplus.timers.TimerController;
import com.haayhappen.clockplus.timers.TimerNotificationService;
import com.haayhappen.clockplus.timers.data.AsyncTimersTableUpdateHandler;
import com.haayhappen.clockplus.R;
import com.haayhappen.clockplus.timers.Timer;

public class TimesUpActivity extends RingtoneActivity<Timer> {
    private static final String TAG = "TimesUpActivity";

    private TimerController mController;
    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TimerNotificationService.cancelNotification(this, getRingingObject().getId());
        mController = new TimerController(getRingingObject(),
                new AsyncTimersTableUpdateHandler(this, null));
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void finish() {
        super.finish();
        mNotificationManager.cancel(TAG, getRingingObject().getIntId());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        postExpiredTimerNote();
    }

    @Override
    protected Class<? extends RingtoneService> getRingtoneServiceClass() {
        return TimerRingtoneService.class;
    }

    @Override
    protected CharSequence getHeaderTitle() {
        return getRingingObject().label();
    }

    @Override
    protected void getHeaderContent(ViewGroup parent) {
        // Inflate the content and apply the parent's layout params, but don't
        // attach it to the parent yet. This is so the return value can be
        // the root of the inflated content, and not the parent. Alternatively,
        // we could set an id on the root of the content's layout and find it
        // from the returned parent.
        CountdownChronometer countdown = (CountdownChronometer) getLayoutInflater()
                .inflate(R.layout.content_header_timesup_activity, parent, false);
        countdown.setBase(SystemClock.elapsedRealtime());
        countdown.start();
        parent.addView(countdown);
    }

    @Override
    protected int getAutoSilencedText() {
        return R.string.timer_auto_silenced_text;
    }

    @Override
    protected int getLeftButtonText() {
        return R.string.add_one_minute;
    }

    @Override
    protected int getRightButtonText() {
        return R.string.stop;
    }

    @Override
    protected int getLeftButtonDrawable() {
        return R.drawable.ic_add_48dp;
    }

    @Override
    protected int getRightButtonDrawable() {
        return R.drawable.ic_stop_48dp;
    }

    @Override
    protected void onLeftButtonClick() {
        mController.addOneMinute();
        stopAndFinish();
    }

    @Override
    protected void onRightButtonClick() {
        mController.stop();
        stopAndFinish();
    }

    // TODO: Consider changing the return type to Notification, and move the actual
    // task of notifying to the base class.
    @Override
    protected void showAutoSilenced() {
        super.showAutoSilenced();
        postExpiredTimerNote();
    }

    private void postExpiredTimerNote() {
        Notification note = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.timer_expired))
                .setContentText(getRingingObject().label())
                .setSmallIcon(R.drawable.ic_timer_24dp)
                .build();
        mNotificationManager.notify(TAG, getRingingObject().getIntId(), note);
    }
}
