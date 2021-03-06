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

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Fynn Merlevede on 7/30/2016.
 */
public final class AlarmsTable {
    private AlarmsTable() {
    }

    // TODO: Consider defining index constants for each column,
    // and then removing all cursor getColumnIndex() calls.
    public static final String TABLE_ALARMS = "alarms";

    // TODO: Consider implementing BaseColumns instead to get _id column.
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTES = "minutes";
    public static final String COLUMN_LABEL = "label";
    public static final String COLUMN_ORIGIN_ADRESS = "origin_adress";
    public static final String COLUMN_ORIGIN_LATITUDE = "origin_latitude";
    public static final String COLUMN_ORIGIN_LONGITUDE = "origin_longitude";
    public static final String COLUMN_DESTINATION_ADRESS = "destination_adress";
    public static final String COLUMN_DESTINATION_LATITUDE = "destination_latitude";
    public static final String COLUMN_DESTINATION_LONGITUDE = "destination_longitude";
    public static final String COLUMN_DURATION="duration";
    public static final String COLUMN_REAL_DURATION="real_duration";
    public static final String COLUMN_RINGTONE = "ringtone";
    public static final String COLUMN_VIBRATES = "vibrates";
    public static final String COLUMN_ENABLED = "enabled";


    // TODO: Delete this column, becuase new sort order does not consider it
    @Deprecated
    public static final String COLUMN_RING_TIME_MILLIS = "ring_time_millis";

    public static final String COLUMN_SNOOZING_UNTIL_MILLIS = "snoozing_until_millis";
    public static final String COLUMN_SUNDAY = "sunday";
    public static final String COLUMN_MONDAY = "monday";
    public static final String COLUMN_TUESDAY = "tuesday";
    public static final String COLUMN_WEDNESDAY = "wednesday";
    public static final String COLUMN_THURSDAY = "thursday";
    public static final String COLUMN_FRIDAY = "friday";
    public static final String COLUMN_SATURDAY = "saturday";
    public static final String COLUMN_IGNORE_UPCOMING_RING_TIME = "ignore_upcoming_ring_time";

    // First sort by ring time in ascending order (smaller values first),
    // then break ties by sorting by id in ascending order.
    @Deprecated
    private static final String SORT_ORDER =
            COLUMN_RING_TIME_MILLIS + " ASC, " + COLUMN_ID + " ASC";

    public static final String NEW_SORT_ORDER = COLUMN_HOUR + " ASC, "
            + COLUMN_MINUTES + " ASC, "
            // TOneverDO: Sort COLUMN_ENABLED or else alarms could be reordered
            // if you toggle them on/off, which looks confusing.
            // TODO: Figure out how to get the order to be:
            // No recurring days ->
            // Recurring earlier in user's weekday order ->
            // Recurring everyday
            // As written now, this is incorrect! For one, it assumes
            // the standard week order (starting on Sunday).
            // DESC gives us (Sunday -> Saturday -> No recurring days),
            // ASC gives us the reverse (No recurring days -> Saturday -> Sunday).
            // TODO: If assuming standard week order, try ASC for all days but
            // write COLUMN_SATURDAY first, then COLUMN_FRIDAY, ... , COLUMN_SUNDAY.
            // Check if that gives us (No recurring days -> Sunday -> Saturday).
//            + COLUMN_SUNDAY + " DESC, "
//            + COLUMN_MONDAY + " DESC, "
//            + COLUMN_TUESDAY + " DESC, "
//            + COLUMN_WEDNESDAY + " DESC, "
//            + COLUMN_THURSDAY + " DESC, "
//            + COLUMN_FRIDAY + " DESC, "
//            + COLUMN_SATURDAY + " DESC, "
            // All else equal, newer alarms first
            + COLUMN_ID + " DESC"; // TODO: If duplicate alarm times disallowed, delete this

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ALARMS + " ("
                // https://sqlite.org/autoinc.html
                // If the AUTOINCREMENT keyword appears after INTEGER PRIMARY KEY, that changes the
                // automatic ROWID assignment algorithm to prevent the reuse of ROWIDs over the
                // lifetime of the database. In other words, the purpose of AUTOINCREMENT is to
                // prevent the reuse of ROWIDs from previously deleted rows.
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_HOUR + " INTEGER NOT NULL, "
                + COLUMN_MINUTES + " INTEGER NOT NULL, "
                + COLUMN_LABEL + " TEXT, "
                + COLUMN_ORIGIN_ADRESS + " TEXT, "
                + COLUMN_ORIGIN_LATITUDE + " REAL, "
                + COLUMN_ORIGIN_LONGITUDE + " REAL, "
                + COLUMN_DESTINATION_ADRESS + " TEXT, "
                + COLUMN_DESTINATION_LATITUDE + " REAL, "
                + COLUMN_DESTINATION_LONGITUDE + " REAL, "
                + COLUMN_DURATION + " INTEGER, "
                + COLUMN_REAL_DURATION + " INTEGER, "
                + COLUMN_RINGTONE + " TEXT NOT NULL, "
                + COLUMN_VIBRATES + " INTEGER NOT NULL, "
                + COLUMN_ENABLED + " INTEGER NOT NULL, "
                + COLUMN_RING_TIME_MILLIS + " INTEGER NOT NULL, "
                + COLUMN_SNOOZING_UNTIL_MILLIS + " INTEGER, "
                + COLUMN_SUNDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_MONDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_TUESDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_WEDNESDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_THURSDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_FRIDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_SATURDAY + " INTEGER NOT NULL DEFAULT 0, "
                + COLUMN_IGNORE_UPCOMING_RING_TIME + " INTEGER NOT NULL);");
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS);
        onCreate(db);
    }
}
