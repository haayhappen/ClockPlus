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

package com.haayhappen.clockplus.alarms.data;

import android.database.Cursor;

import com.haayhappen.clockplus.alarms.Alarm;
import com.haayhappen.clockplus.data.BaseItemCursor;
import com.haayhappen.clockplus.location.Location;

import static com.haayhappen.clockplus.alarms.misc.DaysOfWeek.FRIDAY;
import static com.haayhappen.clockplus.alarms.misc.DaysOfWeek.MONDAY;
import static com.haayhappen.clockplus.alarms.misc.DaysOfWeek.SATURDAY;
import static com.haayhappen.clockplus.alarms.misc.DaysOfWeek.SUNDAY;
import static com.haayhappen.clockplus.alarms.misc.DaysOfWeek.THURSDAY;
import static com.haayhappen.clockplus.alarms.misc.DaysOfWeek.TUESDAY;
import static com.haayhappen.clockplus.alarms.misc.DaysOfWeek.WEDNESDAY;

/**
 * Created by Fynn Merlevede on 7/30/2016.
 */
// An alternative method to creating an Alarm from a cursor is to
// make an Alarm constructor that takes an Cursor param. However,
// this method has the advantage of keeping the contents of
// the Alarm class as pure Java, which can facilitate unit testing
// because it has no dependence on Cursor, which is part of the Android SDK.
public class AlarmCursor extends BaseItemCursor<Alarm> {
    private static final String TAG = "AlarmCursor";

    public AlarmCursor(Cursor c) {
        super(c);
    }

    /**
     * @return an Alarm instance configured for the current row,
     * or null if the current row is invalid
     */
    @Override
    public Alarm getItem() {
        if (isBeforeFirst() || isAfterLast())
            return null;
        Alarm alarm = Alarm.builder()
                .hour(getInt(getColumnIndexOrThrow(AlarmsTable.COLUMN_HOUR)))
                .minutes(getInt(getColumnIndexOrThrow(AlarmsTable.COLUMN_MINUTES)))
                .vibrates(isTrue(AlarmsTable.COLUMN_VIBRATES))
                .ringtone(getString(getColumnIndexOrThrow(AlarmsTable.COLUMN_RINGTONE)))
                .label(getString(getColumnIndexOrThrow(AlarmsTable.COLUMN_LABEL)))
                .origin(new Location(getString(getColumnIndexOrThrow(AlarmsTable.COLUMN_ORIGIN_ADRESS)),
                        getDouble(getColumnIndexOrThrow(AlarmsTable.COLUMN_ORIGIN_LATITUDE)),
                        getDouble(getColumnIndexOrThrow(AlarmsTable.COLUMN_ORIGIN_LONGITUDE))))
                .destination(new Location(getString(getColumnIndexOrThrow(AlarmsTable.COLUMN_DESTINATION_ADRESS)),
                        getDouble(getColumnIndexOrThrow(AlarmsTable.COLUMN_DESTINATION_LATITUDE)),
                        getDouble(getColumnIndexOrThrow(AlarmsTable.COLUMN_DESTINATION_LONGITUDE))))
                .duration(getInt(getColumnIndexOrThrow(AlarmsTable.COLUMN_DURATION)))
                .build();
        alarm.setId(getLong(getColumnIndexOrThrow(AlarmsTable.COLUMN_ID)));
        alarm.setEnabled(isTrue(AlarmsTable.COLUMN_ENABLED));
        alarm.setSnoozing(getLong(getColumnIndexOrThrow(AlarmsTable.COLUMN_SNOOZING_UNTIL_MILLIS)));
        alarm.setRecurring(SUNDAY, isTrue(AlarmsTable.COLUMN_SUNDAY));
        alarm.setRecurring(MONDAY, isTrue(AlarmsTable.COLUMN_MONDAY));
        alarm.setRecurring(TUESDAY, isTrue(AlarmsTable.COLUMN_TUESDAY));
        alarm.setRecurring(WEDNESDAY, isTrue(AlarmsTable.COLUMN_WEDNESDAY));
        alarm.setRecurring(THURSDAY, isTrue(AlarmsTable.COLUMN_THURSDAY));
        alarm.setRecurring(FRIDAY, isTrue(AlarmsTable.COLUMN_FRIDAY));
        alarm.setRecurring(SATURDAY, isTrue(AlarmsTable.COLUMN_SATURDAY));
        alarm.ignoreUpcomingRingTime(isTrue(AlarmsTable.COLUMN_IGNORE_UPCOMING_RING_TIME));
        return alarm;
    }
}
