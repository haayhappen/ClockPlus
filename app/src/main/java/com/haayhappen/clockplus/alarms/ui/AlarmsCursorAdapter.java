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

package com.haayhappen.clockplus.alarms.ui;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.haayhappen.clockplus.alarms.Alarm;
import com.haayhappen.clockplus.alarms.data.AlarmCursor;
import com.haayhappen.clockplus.alarms.misc.AlarmController;
import com.haayhappen.clockplus.list.BaseCursorAdapter;
import com.haayhappen.clockplus.list.OnListItemInteractionListener;

/**
 * Created by Phillip Hsu on 6/29/2016.
 */
public class AlarmsCursorAdapter extends BaseCursorAdapter<Alarm, BaseAlarmViewHolder, AlarmCursor> {
    private static final String TAG = "AlarmsCursorAdapter";
    private static final int VIEW_TYPE_COLLAPSED = 0;
    private static final int VIEW_TYPE_EXPANDED = 1;

    private final AlarmController mAlarmController;

    // TOneverDO: initial value >= 0
    private int mExpandedPosition = RecyclerView.NO_POSITION;
    private long mExpandedId = RecyclerView.NO_ID;

    public AlarmsCursorAdapter(OnListItemInteractionListener<Alarm> listener,
                               AlarmController alarmController) {
        super(listener);
        mAlarmController = alarmController;
    }

    @Override
    protected BaseAlarmViewHolder onCreateViewHolder(ViewGroup parent, OnListItemInteractionListener<Alarm> listener, int viewType) {
        if (viewType == VIEW_TYPE_COLLAPSED)
            return new CollapsedAlarmViewHolder(parent, listener, mAlarmController);
        return new ExpandedAlarmViewHolder(parent, listener, mAlarmController);
    }

    @Override
    public int getItemViewType(int position) {
        final long stableId = getItemId(position);
        return stableId != RecyclerView.NO_ID && stableId == mExpandedId
//                position == mExpandedPosition
                ? VIEW_TYPE_EXPANDED : VIEW_TYPE_COLLAPSED;
    }

//    // TODO
//    public void saveInstance(Bundle outState) {
//        outState.putLong(KEY_EXPANDED_ID, mExpandedId);
//    }

    public boolean expand(int position) {
        if (position == RecyclerView.NO_POSITION)
            return false;
        final long stableId = getItemId(position);
        if (stableId == RecyclerView.NO_ID || mExpandedId == stableId)
            return false;
        mExpandedId = stableId;
        // If we can call this, the item is in view, so we don't need to scroll to it?
//        mScrollHandler.smoothScrollTo(position);
        if (mExpandedPosition >= 0) {
            // Collapse this position first. getItemViewType() will be called
            // in onCreateViewHolder() to verify which ViewHolder to create
            // for the position.
            notifyItemChanged(mExpandedPosition);
        }
        mExpandedPosition = position;
        notifyItemChanged(position);
        return true;

        // This would be my alternative solution. But we're keeping Google's
        // because the stable ID *could* hold up better for orientation changes
        // than the position? I.e. when saving instance state we save the id.
//        int oldExpandedPosition = mExpandedPosition;
//        mExpandedPosition = position;
//        if (oldExpandedPosition >= 0) {
//            notifyItemChanged(oldExpandedPosition);
//        }
//        notifyItemChanged(mExpandedPosition);
    }

    public void collapse(int position) {
        mExpandedId = RecyclerView.NO_ID;
        mExpandedPosition = RecyclerView.NO_POSITION;
        notifyItemChanged(position);
    }

    public int getExpandedPosition() {
        return mExpandedPosition;
    }
}
