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

package com.haayhappen.clockplus.dialogs;

import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.haayhappen.clockplus.dialogs.DialogFragmentController;
import com.haayhappen.clockplus.dialogs.RingtonePickerDialog;

/**
 * Created by Phillip Hsu on 9/20/2016.
 */
public class RingtonePickerDialogController extends DialogFragmentController<RingtonePickerDialog> {
    private static final String TAG = "RingtonePickerCtrller";

    private final RingtonePickerDialog.OnRingtoneSelectedListener mListener;

    public RingtonePickerDialogController(FragmentManager fragmentManager, RingtonePickerDialog.OnRingtoneSelectedListener l) {
        super(fragmentManager);
        mListener = l;
    }

    public void show(Uri initialUri, String tag) {
        RingtonePickerDialog dialog = RingtonePickerDialog.newInstance(mListener, initialUri);
        show(dialog, tag);
    }

    @Override
    public void tryRestoreCallback(String tag) {
        RingtonePickerDialog dialog = findDialog(tag);
        if (dialog != null) {
            Log.i(TAG, "Restoring on ringtone selected callback");
            dialog.setOnRingtoneSelectedListener(mListener);
        }
    }
}