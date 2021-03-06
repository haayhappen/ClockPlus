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

package com.haayhappen.clockplus.alarms.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.haayhappen.clockplus.MainActivity;
import com.haayhappen.clockplus.R;
import com.haayhappen.clockplus.alarms.Alarm;
import com.haayhappen.clockplus.alarms.misc.AlarmController;
import com.haayhappen.clockplus.alarms.misc.DaysOfWeek;
import com.haayhappen.clockplus.dialogs.RingtonePickerDialog;
import com.haayhappen.clockplus.dialogs.RingtonePickerDialogController;
import com.haayhappen.clockplus.list.OnListItemInteractionListener;
import com.haayhappen.clockplus.location.DistanceHandler;
import com.haayhappen.clockplus.location.Location;
import com.haayhappen.clockplus.timepickers.Utils;
import com.haayhappen.clockplus.util.FragmentTagUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.OnClick;

//import static com.haayhappen.clockplus.location.DistanceHandler.getDistanceInfo;

/**
 * Created by Fynn Merlevede on 7/31/2016.
 */
public class ExpandedAlarmViewHolder extends BaseAlarmViewHolder {
    private static final String TAG = "ExpandedAlarmViewHolder";
    private final ColorStateList mDayToggleColors;
    private final ColorStateList mVibrateColors;

    private final RingtonePickerDialogController mRingtonePickerController;
    @Bind(R.id.ok)
    ImageButton mOk;
    @Bind(R.id.delete)
    ImageButton mDelete;
    @Bind(R.id.ringtone)
    Button mRingtone;
    @Bind(R.id.vibrate)
    TempCheckableImageButton mVibrate;
    @Bind({R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6})
    ToggleButton[] mDays;
    @Bind(R.id.from)
    TextView fromText;
    @Bind(R.id.to)
    TextView toText;
    @Bind(R.id.duration)
    TextView duration;
    @Bind(R.id.b_clear_from)
    ImageButton clearFromButton;
    @Bind(R.id.b_clear_to)
    ImageButton clearToButton;

    public ExpandedAlarmViewHolder(Activity activity, ViewGroup parent, final OnListItemInteractionListener<Alarm> listener,
                                   AlarmController controller) {
        super(activity, parent, R.layout.item_expanded_alarm, listener, controller);
        // Manually bind listeners, or else you'd need to write a getter for the
        // OnListItemInteractionListener in the BaseViewHolder for use in method binding.
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onListItemDeleted(getAlarm());
            }
        });
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Since changes are persisted as soon as they are made, there's really
                // nothing we have to persist here. Let the listener know we should
                // collapse this VH.
                // While this works, it also makes an update to the DB and thus reschedules
                // the alarm, so the snackbar will show up as well. We want to avoid that..
//                listener.onListItemUpdate(getAlarm(), getAdapterPosition());
                // TODO: This only works because we know what the implementation looks like..
                // This is bad because we just made the proper function of this dependent
                // on the implementation.
                listener.onListItemClick(getAlarm(), getAdapterPosition());
            }
        });

        // https://code.google.com/p/android/issues/detail?id=177282
        // https://stackoverflow.com/questions/15673449/is-it-confirmed-that-i-cannot-use-themed-color-attribute-in-color-state-list-res
        // Programmatically create the ColorStateList for our day toggles using themed color
        // attributes, "since prior to M you can't create a themed ColorStateList from XML but you
        // can from code." (quote from google)
        // The first array level is analogous to an XML node defining an item with a state list.
        // The second level lists all the states considered by the item from the first level.
        // An empty list of states represents the default stateless item.
        int[][] states = {
                /*item 1*/{/*states*/android.R.attr.state_checked},
                /*item 2*/{/*states*/}
        };
        // TODO: Phase out Utils.getColorFromThemeAttr because it doesn't work for text colors.
        // WHereas getTextColorFromThemeAttr works for both regular colors and text colors.
        int[] dayToggleColors = {
                /*item 1*/Utils.getTextColorFromThemeAttr(getContext(), R.attr.colorAccent),
                /*item 2*/Utils.getTextColorFromThemeAttr(getContext(), android.R.attr.textColorHint)
        };
        int[] vibrateColors = {
                /*item 1*/Utils.getTextColorFromThemeAttr(getContext(), R.attr.colorAccent),
                /*item 2*/Utils.getTextColorFromThemeAttr(getContext(), R.attr.themedIconTint)
        };
        mDayToggleColors = new ColorStateList(states, dayToggleColors);
        mVibrateColors = new ColorStateList(states, vibrateColors);

        mRingtonePickerController = new RingtonePickerDialogController(mFragmentManager,
                new RingtonePickerDialog.OnRingtoneSelectedListener() {
                    @Override
                    public void onRingtoneSelected(Uri ringtoneUri) {
                        Log.d(TAG, "Selected ringtone: " + ringtoneUri.toString());
                        final Alarm oldAlarm = getAlarm();
                        Alarm newAlarm = oldAlarm.toBuilder()
                                .ringtone(ringtoneUri.toString())
                                .build();
                        oldAlarm.copyMutableFieldsTo(newAlarm);
                        persistUpdatedAlarm(newAlarm, false);
                    }
                }
        );
    }

    @Override
    public void onBind(Alarm alarm) {
        super.onBind(alarm);
        mRingtonePickerController.tryRestoreCallback(makeTag(R.id.ringtone));
        bindDays(alarm);
        bindRingtone();
        bindFrom(alarm);
        bindClearFrom(alarm);
        bindClearTo(alarm);
        bindTo(alarm);
        bindVibrate(alarm.vibrates());
    }

    @Override
    protected void bindLabel(boolean visible, String label) {
        super.bindLabel(true, label);
    }

    @OnClick(R.id.ok)
    void save() {
        // TODO
    }

//    @OnClick(R.id.delete)
//    void delete() {
//        // TODO
//    }

    @OnClick(R.id.ringtone)
    void showRingtonePickerDialog() {
//        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
//        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
//                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
//                // The ringtone to show as selected when the dialog is opened
//                .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, getSelectedRingtoneUri())
//                // Whether to show "Default" item in the list
//                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
//        // The ringtone that plays when default option is selected
//        //.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, DEFAULT_TONE);
//        // TODO: This is VERY BAD. Use a Controller/Presenter instead.
//        // The result will be delivered to MainActivity, and then delegated to AlarmsFragment.
//        ((Activity) getContext()).startActivityForResult(intent, AlarmsFragment.REQUEST_PICK_RINGTONE);

        mRingtonePickerController.show(getSelectedRingtoneUri(), makeTag(R.id.ringtone));
    }


    @OnClick(R.id.vibrate)
    void onVibrateToggled() {
        final boolean checked = mVibrate.isChecked();
        if (checked) {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(300);
        }
        final Alarm oldAlarm = getAlarm();
        Alarm newAlarm = oldAlarm.toBuilder()
                .vibrates(checked)
                .build();
        oldAlarm.copyMutableFieldsTo(newAlarm);
        persistUpdatedAlarm(newAlarm, false);
    }

    @OnClick(R.id.from)
    void onFromClicked() {
        ((MainActivity) getActivity()).setLocationPicker(new MainActivity.LocationPicker() {
            @Override
            public void onLocationPicked(Place place) {
                fromText.setText(place.getAddress());
                setDuration();
                final Alarm oldAlarm = getAlarm();
                Alarm newAlarm = oldAlarm.toBuilder()
                        .origin(new Location(String.valueOf(place.getAddress()), place.getLatLng().latitude, place.getLatLng().longitude))
                        .build();
                oldAlarm.copyMutableFieldsTo(newAlarm);
                persistUpdatedAlarm(newAlarm, false);
            }
        });
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            getActivity().startActivityForResult(builder.build(getActivity()), MainActivity.REQUEST_CODE_PICK_LOCATION);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.b_clear_from)
    void onClearFrom() {
        fromText.setText(getContext().getString(R.string.my_location));
        duration.setText("");
        final Alarm oldAlarm = getAlarm();
        Alarm newAlarm = oldAlarm.toBuilder()
                .origin(new Location("", 0, 0))
                .build();
        oldAlarm.copyMutableFieldsTo(newAlarm);
        persistUpdatedAlarm(newAlarm, false);
        bindClearFrom(newAlarm);
    }

    @OnClick(R.id.to)
    void onToClicked() {
        ((MainActivity) getActivity()).setLocationPicker(new MainActivity.LocationPicker() {
            @Override
            public void onLocationPicked(Place place) {
                toText.setText(place.getAddress());
                setDuration();
                final Alarm oldAlarm = getAlarm();
                Alarm newAlarm = oldAlarm.toBuilder()
                        .destination(new Location(String.valueOf(place.getAddress()), place.getLatLng().latitude, place.getLatLng().longitude))
                        .build();
                oldAlarm.copyMutableFieldsTo(newAlarm);
                persistUpdatedAlarm(newAlarm, false);
            }
        });
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            getActivity().startActivityForResult(builder.build(getActivity()), MainActivity.REQUEST_CODE_PICK_LOCATION);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.b_clear_to)
    void onClearTo() {
        toText.setText("");
        duration.setText("");
        final Alarm oldAlarm = getAlarm();
        Alarm newAlarm = oldAlarm.toBuilder()
                .destination(new Location("", 0, 0))
                .build();
        oldAlarm.copyMutableFieldsTo(newAlarm);
        persistUpdatedAlarm(newAlarm, false);
        bindClearTo(newAlarm);
    }

    @OnClick(R.id.duration)
    void onDurationClicked() {
    }

    private void setDuration() {
        Log.d(TAG, "set duration");
        DistanceHandler asyncTask = new DistanceHandler(getAlarm(), new DistanceHandler.AsyncResponse() {
            @Override
            public void processFinish(long delaySecs) {
                int testseconds = 4000;
                final Alarm oldAlarm = getAlarm();
                int delayMinutes = (int) TimeUnit.SECONDS.toMinutes(testseconds);
                int delayHours = delayMinutes / 60;
                delayMinutes = delayMinutes % 60;

                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                try {
                    d = sdf.parse(oldAlarm.hour() + ":" + oldAlarm.minutes());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //TODO remove all debug logs for production
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                Log.d("ExpandedAlarmViewHolder","calendar time before new set: "+cal.getTime().toString());
                Log.d("ExpandedAlarmViewHolder","hours to be subtracted: "+delayHours);
                Log.d("ExpandedAlarmViewHolder","minutes to be subtracted: "+delayMinutes);

                if (delayHours == 0){
                    cal.add(Calendar.MINUTE, -Math.abs(delayMinutes));
                }else{
                    cal.add(Calendar.HOUR_OF_DAY,-Math.abs(delayHours));
                    cal.add(Calendar.MINUTE,-Math.abs(delayMinutes));
                }


                Log.d("ExpandedAlarmViewHolder","calendar time after set: "+cal.getTime().toString());
                Log.d("ExpandedAlarmViewHolder","setting new hours to: "+cal.get(Calendar.HOUR_OF_DAY));
                Log.d("ExpandedAlarmViewHolder","setting new minutes to: "+cal.get(Calendar.MINUTE));

                Alarm newAlarm = oldAlarm.toBuilder()
                        .minutes(cal.get(Calendar.MINUTE))
                        .hour(cal.get(Calendar.HOUR_OF_DAY))
                        .build();
                oldAlarm.copyMutableFieldsTo(newAlarm);
                persistUpdatedAlarm(newAlarm, false);
                //#############################
                duration.setText(String.valueOf(delayMinutes) + "delay");
            }
        });

        if (!fromText.getText().equals("") && !toText.getText().equals("")) {
            try {
                asyncTask.execute(fromText.getText().toString(), toText.getText().toString());
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    @OnClick({R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6})
    void onDayToggled(ToggleButton view) {
        final Alarm oldAlarm = getAlarm();
        Alarm newAlarm = oldAlarm.toBuilder().build();
        oldAlarm.copyMutableFieldsTo(newAlarm);
        // ---------------------------------------------------------------------------------
        // TOneverDO: precede copyMutableFieldsTo()
        int position = ((ViewGroup) view.getParent()).indexOfChild(view);
        int weekDayAtPosition = DaysOfWeek.getInstance(getContext()).weekDayAt(position);
        Log.d(TAG, "Day toggle #" + position + " checked changed. This is weekday #"
                + weekDayAtPosition + " relative to a week starting on Sunday");
        newAlarm.setRecurring(weekDayAtPosition, view.isChecked());
        // ---------------------------------------------------------------------------------
        persistUpdatedAlarm(newAlarm, true);
    }

    private void bindDays(Alarm alarm) {
        for (int i = 0; i < mDays.length; i++) {
            mDays[i].setTextColor(mDayToggleColors);
            int weekDay = DaysOfWeek.getInstance(getContext()).weekDayAt(i);
            String label = DaysOfWeek.getLabel(weekDay);
            mDays[i].setTextOn(label);
            mDays[i].setTextOff(label);
            mDays[i].setChecked(alarm.isRecurring(weekDay));
        }
    }

    private void bindFrom(Alarm alarm) {
        fromText.setText(alarm.origin().getAdress().equals("") ? "Mein Standort" : alarm.origin().getAdress());
    }

    private void bindClearFrom(Alarm alarm) {
        clearFromButton.setEnabled(!alarm.origin().getAdress().equals(""));
    }

    private void bindTo(Alarm alarm) {
        toText.setText(alarm.destination().getAdress());
        setDuration();
    }

    private void bindClearTo(Alarm alarm) {
        clearToButton.setEnabled(!alarm.destination().getAdress().equals(""));
    }

    private void bindRingtone() {
        int iconTint = Utils.getTextColorFromThemeAttr(getContext(), R.attr.themedIconTint);

        Drawable ringtoneIcon = mRingtone.getCompoundDrawablesRelative()[0/*start*/];
        ringtoneIcon = DrawableCompat.wrap(ringtoneIcon.mutate());
        DrawableCompat.setTint(ringtoneIcon, iconTint);
        mRingtone.setCompoundDrawablesRelativeWithIntrinsicBounds(ringtoneIcon, null, null, null);

        String title = RingtoneManager.getRingtone(getContext(),
                getSelectedRingtoneUri()).getTitle(getContext());
        mRingtone.setText(title);
    }

    private void bindVibrate(boolean vibrates) {
        Utils.setTintList(mVibrate, mVibrate.getDrawable(), mVibrateColors);
        mVibrate.setChecked(vibrates);
    }

    private Uri getSelectedRingtoneUri() {
        // If showing an item for "Default" (@see EXTRA_RINGTONE_SHOW_DEFAULT), this can be one
        // of DEFAULT_RINGTONE_URI, DEFAULT_NOTIFICATION_URI, or DEFAULT_ALARM_ALERT_URI to have the
        // "Default" item checked.
        //
        // Otherwise, use RingtoneManager.getActualDefaultRingtoneUri() to get the "actual sound URI".
        //
        // Do not use RingtoneManager.getDefaultUri(), because that just returns one of
        // DEFAULT_RINGTONE_URI, DEFAULT_NOTIFICATION_URI, or DEFAULT_ALARM_ALERT_URI
        // depending on the type requested (i.e. what the docs calls "symbolic URI
        // which will resolved to the actual sound when played").
        String ringtone = getAlarm().ringtone();
        return ringtone.isEmpty() ?
                RingtoneManager.getActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_ALARM)
                : Uri.parse(ringtone);
    }

    private String makeTag(@IdRes int viewId) {
        return FragmentTagUtils.makeTag(ExpandedAlarmViewHolder.class, viewId, getItemId());
    }
}
