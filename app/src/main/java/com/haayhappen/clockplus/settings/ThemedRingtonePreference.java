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

package com.haayhappen.clockplus.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;

import com.haayhappen.clockplus.dialogs.RingtonePickerDialog;
import com.haayhappen.clockplus.dialogs.RingtonePickerDialogController;

/**
 * Created by Phillip Hsu on 9/20/2016.
 *
 * <p>A modified version of the framework's {@link RingtonePreference} that
 * uses our {@link RingtonePickerDialog} instead of the system's ringtone picker.</p>
 */
public class ThemedRingtonePreference extends RingtonePreference
        implements RingtonePickerDialog.OnRingtoneSelectedListener {
    private static final String TAG = "ThemedRingtonePreference";
    
    private RingtonePickerDialogController mController;

    @TargetApi(21)
    public ThemedRingtonePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ThemedRingtonePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ThemedRingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemedRingtonePreference(Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        if (mController == null) {
            mController = newController();
        }
        mController.show(onRestoreRingtone(), TAG);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if (mController == null) {
            mController = newController();
        }
        mController.tryRestoreCallback(TAG);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Our picker does not show a 'Default' item, so our defaultValue
        // "content://settings/system/alarm_alert" set in XML will not show as initially selected.
        // The default ringtone and its sound file URI is different for every device,
        // so there isn't a string literal we can specify in XML.
        // This is the same as calling:
        //     `RingtoneManager.getActualDefaultRingtoneUri(
        //         getContext(), RingtoneManager.TYPE_ALARM).toString();`
        // but skips the toString().
        return Settings.System.getString(getContext().getContentResolver(), Settings.System.ALARM_ALERT);
    }

    @Override
    public void onRingtoneSelected(Uri uri) {
        if (callChangeListener(uri != null ? uri.toString() : "")) {
            onSaveRingtone(uri);
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }

    private RingtonePickerDialogController newController() {
        // TODO: BAD!
        AppCompatActivity a = (AppCompatActivity) getContext();
        return new RingtonePickerDialogController(a.getSupportFragmentManager(), this);
    }
}
