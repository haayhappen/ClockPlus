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

package com.haayhappen.clockplus.stopwatch.data;

import android.content.Context;
import android.content.Intent;

import com.haayhappen.clockplus.data.AsyncDatabaseTableUpdateHandler;
import com.haayhappen.clockplus.list.ScrollHandler;
import com.haayhappen.clockplus.stopwatch.Lap;
import com.haayhappen.clockplus.stopwatch.StopwatchNotificationService;

/**
 * Created by Phillip Hsu on 8/9/2016.
 */
public class AsyncLapsTableUpdateHandler extends AsyncDatabaseTableUpdateHandler<Lap, LapsTableManager> {

    public AsyncLapsTableUpdateHandler(Context context, ScrollHandler scrollHandler) {
        super(context, scrollHandler);
    }

    @Override
    protected LapsTableManager onCreateTableManager(Context context) {
        return new LapsTableManager(context);
    }

    @Override
    protected void onPostAsyncInsert(Long result, Lap item) {
        if (result > 1) {
            // Update the notification's title with this lap number
            Intent intent = new Intent(getContext(), StopwatchNotificationService.class)
                    .setAction(StopwatchNotificationService.ACTION_UPDATE_LAP_TITLE)
                    .putExtra(StopwatchNotificationService.EXTRA_LAP_NUMBER, result.intValue());
            getContext().startService(intent);
        }
    }

    // ===================== DO NOT IMPLEMENT =========================

    @Override
    protected void onPostAsyncDelete(Integer result, Lap item) {
        // Leave blank.
    }

    @Override
    protected void onPostAsyncUpdate(Long result, Lap item) {
        // Leave blank.
    }
}
