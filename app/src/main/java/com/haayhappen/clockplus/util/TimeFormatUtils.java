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

package com.haayhappen.clockplus.util;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;

import static android.text.format.DateFormat.getTimeFormat;

/**
 * Created by Fynn Merlevede on 6/3/2016.
 */
public final class TimeFormatUtils {

    private TimeFormatUtils() {}

    /**
     *
     * @param context
     * @param millis
     * @return String time
     */
    public static String formatTime(Context context, long millis) {
        return getTimeFormat(context).format(new Date(millis));
    }

    /**
     *
     * @param context
     * @param hourOfDay
     * @param minute
     * @return String time
     */
    public static String formatTime(Context context, int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        return formatTime(context, cal.getTimeInMillis());
    }
}
