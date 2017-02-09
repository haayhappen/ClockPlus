/*
 * Copyright (C) 2016 Fynn Merlevede
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

package com.haayhappen.clockplus.alarms.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.haayhappen.clockplus.alarms.Alarm;
import com.haayhappen.clockplus.alarms.data.AlarmCursor;
import com.haayhappen.clockplus.alarms.data.AlarmsTableManager;
import com.haayhappen.clockplus.alarms.misc.AlarmController;

import static com.haayhappen.clockplus.util.Preconditions.checkNotNull;

/**
 * Used to reschedule recurring alarms that were dismissed in their upcoming state, so {@link Alarm#ringsAt()}
 * still refers to the time it rings today. This class receives
 * your intent at the Alarm instance's normal ring time, so by the time you make a subsequent call
 * to {@link Alarm#ringsAt()}, the value returned refers to the next time the alarm will recur.
 */
// TODO: Consider registering this locally instead of in the manifest.
public class PendingAlarmScheduler extends BroadcastReceiver {
    // We include the class name in the string to distinguish this constant from the one defined
    // in UpcomingAlarmReceiver.
    public static final String EXTRA_ALARM_ID = "com.philliphsu.clock2.alarms.background.PendingAlarmScheduler.extra.ALARM_ID";

    @Override
    public void onReceive(final Context context, Intent intent) {
        final long id = intent.getLongExtra(EXTRA_ALARM_ID, -1);
        if (id < 0) {
            throw new IllegalStateException("No alarm id received");
        }
        // Start our own thread to load the alarm instead of:
        //  * using a Loader, because we have no complex lifecycle and thus
        //  BroadcastReceiver has no built-in LoaderManager, AND getting a Loader
        //  to work here might be a hassle, let alone it might not even be appropriate to
        //  use Loaders outside of an Activity/Fragment, since it does depend on LoaderCallbacks.
        //  * using an AsyncTask, because we don't need to do anything on the UI thread
        //  after the background work is complete.
        // TODO: Verify using a Runnable like this won't cause a memory leak.
        // It *probably* won't because a BroadcastReceiver doesn't hold a Context,
        // and it also doesn't have a lifecycle, so it likely won't stick around
        // in memory.
        new Thread(new Runnable() {
            @Override
            public void run() {
                AlarmCursor cursor = new AlarmsTableManager(context).queryItem(id);
                Alarm alarm = checkNotNull(cursor.getItem());
                if (!alarm.isEnabled()) {
                    throw new IllegalStateException("Alarm must be enabled!");
                }
                alarm.ignoreUpcomingRingTime(false); // allow #ringsWithinHours() to behave normally
                // No UI work is done
                AlarmController controller = new AlarmController(context, null);
                controller.scheduleAlarm(alarm, false);
                controller.save(alarm);
            }
        }).start();
    }
}
