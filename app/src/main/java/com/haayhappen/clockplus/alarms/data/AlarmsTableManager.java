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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.haayhappen.clockplus.alarms.Alarm;
import com.haayhappen.clockplus.data.DatabaseTableManager;

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
public class AlarmsTableManager extends DatabaseTableManager<Alarm> {

    public AlarmsTableManager(Context context) {
        super(context);
    }

    @Override
    protected String getQuerySortOrder() {
        return AlarmsTable.NEW_SORT_ORDER;
    }

    @Override
    public AlarmCursor queryItem(long id) {
        return wrapInAlarmCursor(super.queryItem(id));
    }

    @Override
    public AlarmCursor queryItems() {
        return wrapInAlarmCursor(super.queryItems());
    }

    public AlarmCursor queryEnabledAlarms() {
        return queryItems(AlarmsTable.COLUMN_ENABLED + " = 1", null);
    }

    @Override
    protected AlarmCursor queryItems(String where, String limit) {
        return wrapInAlarmCursor(super.queryItems(where, limit));
    }

    @Override
    protected String getTableName() {
        return AlarmsTable.TABLE_ALARMS;
    }

    @Override
    protected ContentValues toContentValues(Alarm alarm) {
        ContentValues values = new ContentValues();
        values.put(AlarmsTable.COLUMN_HOUR, alarm.hour());
        values.put(AlarmsTable.COLUMN_MINUTES, alarm.minutes());
        values.put(AlarmsTable.COLUMN_LABEL, alarm.label());
        values.put(AlarmsTable.COLUMN_ORIGIN_ADRESS, alarm.origin().getAdress());
        values.put(AlarmsTable.COLUMN_ORIGIN_LATITUDE, alarm.origin().getLatitude());
        values.put(AlarmsTable.COLUMN_ORIGIN_LONGITUDE, alarm.origin().getLongitude());
        values.put(AlarmsTable.COLUMN_DESTINATION_ADRESS,alarm.destination().getAdress());
        values.put(AlarmsTable.COLUMN_DESTINATION_LATITUDE, alarm.destination().getLatitude());
        values.put(AlarmsTable.COLUMN_DESTINATION_LONGITUDE, alarm.destination().getLongitude());
        values.put(AlarmsTable.COLUMN_RINGTONE, alarm.ringtone());
        values.put(AlarmsTable.COLUMN_VIBRATES, alarm.vibrates());
        values.put(AlarmsTable.COLUMN_ENABLED, alarm.isEnabled());
        values.put(AlarmsTable.COLUMN_RING_TIME_MILLIS, alarm.ringsAt());
        values.put(AlarmsTable.COLUMN_SNOOZING_UNTIL_MILLIS, alarm.snoozingUntil());
        values.put(AlarmsTable.COLUMN_SUNDAY, alarm.isRecurring(SUNDAY));
        values.put(AlarmsTable.COLUMN_MONDAY, alarm.isRecurring(MONDAY));
        values.put(AlarmsTable.COLUMN_TUESDAY, alarm.isRecurring(TUESDAY));
        values.put(AlarmsTable.COLUMN_WEDNESDAY, alarm.isRecurring(WEDNESDAY));
        values.put(AlarmsTable.COLUMN_THURSDAY, alarm.isRecurring(THURSDAY));
        values.put(AlarmsTable.COLUMN_FRIDAY, alarm.isRecurring(FRIDAY));
        values.put(AlarmsTable.COLUMN_SATURDAY, alarm.isRecurring(SATURDAY));
        values.put(AlarmsTable.COLUMN_IGNORE_UPCOMING_RING_TIME, alarm.isIgnoringUpcomingRingTime());
        return values;
    }

    @Override
    protected String getOnContentChangeAction() {
        return AlarmsListCursorLoader.ACTION_CHANGE_CONTENT;
    }

    private AlarmCursor wrapInAlarmCursor(Cursor c) {
        return new AlarmCursor(c);
    }
}
