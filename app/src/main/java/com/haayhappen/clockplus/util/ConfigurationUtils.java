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

import android.content.res.Resources;

/**
 * Created by Fynn Merlevede on 8/30/2016.
 */
public final class ConfigurationUtils {

    public static int getOrientation(Resources res) {
        return res.getConfiguration().orientation;
    }

    private ConfigurationUtils() {}

}
