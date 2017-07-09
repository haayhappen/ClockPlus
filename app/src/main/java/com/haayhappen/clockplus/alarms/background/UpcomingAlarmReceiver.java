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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.haayhappen.clockplus.MainActivity;
import com.haayhappen.clockplus.R;
import com.haayhappen.clockplus.alarms.Alarm;
import com.haayhappen.clockplus.alarms.misc.AlarmController;
import com.haayhappen.clockplus.location.DistanceHandler;
import com.haayhappen.clockplus.util.ContentIntentUtils;
import com.haayhappen.clockplus.util.ParcelableUtil;

import java.util.concurrent.TimeUnit;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static com.haayhappen.clockplus.util.TimeFormatUtils.formatTime;

// TODO: Consider registering this locally instead of in the manifest.
public class UpcomingAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "UpcomingAlarmReceiver";
    /*TOneverDO: not private*/
    private static final String ACTION_DISMISS_NOW = "com.philliphsu.clock2.action.DISMISS_NOW";

    public static final String ACTION_CANCEL_NOTIFICATION = "com.philliphsu.clock2.action.CANCEL_NOTIFICATION";
    public static final String ACTION_SHOW_SNOOZING = "com.philliphsu.clock2.action.SHOW_SNOOZING";
    public static final String EXTRA_ALARM = "com.philliphsu.clock2.extra.ALARM";
    //public static final String EXTRA_ALARM = "com.haayhappen.clockplus.extra.ALARM";

    private AlarmController mAlarmController;


    @Override
    public void onReceive(final Context context, final Intent intent) {

        //get extras bundle from pending intent
        Bundle extras = intent.getExtras();
        //get the byte array out of the intents' extras
        byte[] byteArray = extras.getByteArray(EXTRA_ALARM);
        //unmarshall the array to our parcel(alarm)
        final Alarm alarm = ParcelableUtil.unmarshall(byteArray, Alarm.CREATOR);

        if (alarm == null) {
            throw new IllegalStateException("No alarm received");
        }
        Log.d(TAG, "Received Alarm which rings in: " + alarm.ringsIn());
        Log.d(TAG, "Received Alarm which rings at: " + alarm.ringsAt());
        Log.d(TAG, "Does the alarm ring within 1 hour?: " + alarm.ringsWithinHours(1));

        final long id = alarm.getId();
        final NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (ACTION_CANCEL_NOTIFICATION.equals(intent.getAction())) {
            nm.cancel(TAG, (int) id);
        } else {
            if (ACTION_DISMISS_NOW.equals(intent.getAction())) {
                new AlarmController(context, null).cancelAlarm(alarm, false, true);
            } else {
                // Prepare notification
                // http://stackoverflow.com/a/15803726/5055032
                // Notifications aren't updated on the UI thread, so we could have
                // done this in the background. However, no lengthy operations are
                // done here, so doing so is a premature optimization.
                String title;
                String text;
                if (ACTION_SHOW_SNOOZING.equals(intent.getAction())) {
                    if (!alarm.isSnoozed())
                        throw new IllegalStateException("Can't show snoozing notif. if alarm not snoozed!");
                    title = "Fix me";
                    //TODO fix this
                    //title = alarm.label().isEmpty() ? context.getString(alarm) : alarm.label();
                    text = context.getString(R.string.title_snoozing_until,
                            formatTime(context, alarm.snoozingUntil()));
                } else {
                    // No intent action required for default behavior
                    title = context.getString(R.string.upcoming_alarm);
                    text = formatTime(context, alarm.ringsAt());
                    if (!alarm.label().isEmpty()) {
                        text = alarm.label() + ", " + text;
                    }
                }

                Intent dismissIntent = new Intent(context, UpcomingAlarmReceiver.class)
                        .setAction(ACTION_DISMISS_NOW)
                        .putExtra(EXTRA_ALARM, alarm);
                PendingIntent piDismiss = PendingIntent.getBroadcast(context, (int) id, dismissIntent, FLAG_ONE_SHOT);
                Notification note = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_alarm_24dp)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setContentIntent(ContentIntentUtils.create(context, MainActivity.PAGE_ALARMS, id))
                        .addAction(R.drawable.ic_dismiss_alarm_24dp, context.getString(R.string.dismiss_now), piDismiss)
                        .build();
                nm.notify(TAG, (int) id, note);
            }
        }
       // Check that alarm rings within 1 hours
        if (alarm.ringsWithinHours(1) && !alarm.reschedules()) {
            mAlarmController = new AlarmController(context, null);
            DistanceHandler asyncTask = new DistanceHandler(alarm, new DistanceHandler.AsyncResponse() {
                @Override
                public void processFinish(long delaySecs) {
                    String newRingtime ="";
                    int minutesUntilRing = (int) TimeUnit.MILLISECONDS.toMinutes(alarm.ringsIn());
                    int testseconds = 240;
                    int delayMinutes = (int) TimeUnit.SECONDS.toMinutes(testseconds);
                    int ringsAtMinutes = (int) ((alarm.ringsAt() / (1000*60)) % 60);

                    //check that delay is less than the alarm is actually away in terms of time
                    if (delayMinutes < minutesUntilRing) {
                        Log.d(TAG, "Alarm: "+ alarm.toString()+" rescheduled? : "+alarm.reschedules());
                        //if (!alarm.reschedules()) {
                        //subtract delay minutes from ringing minutes
                        if (ringsAtMinutes < delayMinutes){
                            //that would happen when the ring time is set right after a new hour started...we have to adjust the hour too then
                            int restmin = ringsAtMinutes-delayMinutes;
                            int newmin = 60 - Math.abs(restmin);
                            int newhour = alarm.hour() -1;

                            Alarm newAlarm = alarm.toBuilder()
                                    .minutes(newmin)
                                    .hour(newhour)
                                    .reschedules(true)
                                    .build();
                            alarm.copyMutableFieldsTo(newAlarm);
                            persistUpdatedAlarm(newAlarm, false);

                            newRingtime += newhour+":"+newmin;

                        }else {
                            //no hours to schedules
                            Alarm newAlarm = alarm.toBuilder()
                                    .minutes(ringsAtMinutes - delayMinutes)
                                    .reschedules(true)
                                    .build();
                            alarm.copyMutableFieldsTo(newAlarm);
                            persistUpdatedAlarm(newAlarm, false);
                            newRingtime +=  alarm.hour()+":"+(ringsAtMinutes - delayMinutes);
                        }
                            Log.d(TAG, "Rescheduled alarm, so user will be waked up earlier.");
                       // }


                        NotificationCompat.Builder builder =
                                new NotificationCompat.Builder(context)
                                        .setSmallIcon(R.drawable.ic_alarm_24dp)
                                        .setContentTitle("TrafficAlarm")
                                        .setContentText("Alarm rescheduled, new ringtime: "+newRingtime );

                        // Add as notification
                        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.notify(0, builder.build());
                    }

                }
            });

            try {
                asyncTask.execute();
            } catch (Exception e) {
                e.getMessage();
            }
        }

        //TODO FOR TESTING
        Log.d(TAG, "alarm ringing in: " + alarm.ringsIn());
        //180000 = 3 minutes -> if alarm rings in less than 3 minutes and isn't rescheduled
//        if (alarm.ringsIn() < 180000 && !alarm.reschedules()) {
//            mAlarmController = new AlarmController(context, null);
//            DistanceHandler asyncTask = new DistanceHandler(alarm, new DistanceHandler.AsyncResponse() {
//                @Override
//                public void processFinish(long delaySecs) {
//                    int minutesUntilRing = (int) TimeUnit.MILLISECONDS.toMinutes(alarm.ringsIn());
//                    int testseconds = 120;
//                    int delayMinutes = (int) TimeUnit.SECONDS.toMinutes(testseconds);
//
//                    if (delayMinutes < minutesUntilRing) {
//                        Log.d(TAG, "Alarm: " + alarm.toString() + " rescheduled? : " + alarm.reschedules());
//                        //if (!alarm.reschedules()) {
//                            Alarm newAlarm = alarm.toBuilder()
//                                    .minutes(minutesUntilRing - delayMinutes)
//                                    .reschedules(true)
//                                    .build();
//                            alarm.copyMutableFieldsTo(newAlarm);
//                            persistUpdatedAlarm(newAlarm, false);
//                            Log.d(TAG, "Rescheduled alarm, so user will be waked up earlier.");
//                       // }
//
//
//                        NotificationCompat.Builder builder =
//                                new NotificationCompat.Builder(context)
//                                        .setSmallIcon(R.drawable.ic_alarm_24dp)
//                                        .setContentTitle("TrafficAlarm")
//                                        .setContentText("Alarm has been rescheduled: " + (minutesUntilRing - delayMinutes) + " minutes");
//
//                        // Add as notification
//                        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//                        manager.notify(0, builder.build());
//                    }
//
//                }
//            });
//
//            try {
//                asyncTask.execute();
//            } catch (Exception e) {
//                e.getMessage();
//            }
//        }


    }

    final void persistUpdatedAlarm(Alarm newAlarm, boolean showSnackbar) {
        mAlarmController.scheduleAlarm(newAlarm, showSnackbar);
        mAlarmController.save(newAlarm);
    }
}
