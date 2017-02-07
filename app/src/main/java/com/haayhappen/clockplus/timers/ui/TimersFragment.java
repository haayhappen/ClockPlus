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

package com.haayhappen.clockplus.timers.ui;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.haayhappen.clockplus.timers.EditTimerActivity;
import com.haayhappen.clockplus.timers.data.AsyncTimersTableUpdateHandler;
import com.haayhappen.clockplus.R;
import com.haayhappen.clockplus.list.RecyclerViewFragment;
import com.haayhappen.clockplus.timers.Timer;
import com.haayhappen.clockplus.timers.data.TimerCursor;
import com.haayhappen.clockplus.timers.data.TimersListCursorLoader;

import static butterknife.ButterKnife.findById;
import static com.haayhappen.clockplus.util.ConfigurationUtils.getOrientation;

public class TimersFragment extends RecyclerViewFragment<Timer, TimerViewHolder, TimerCursor, TimersCursorAdapter> {
    // TODO: Different number of columns for different display densities, instead of landscape.
    // Use smallest width qualifiers. I can imagine 3 or 4 columns for a large enough tablet in landscape.
    private static final int LANDSCAPE_LAYOUT_COLUMNS = 2;

    public static final int REQUEST_CREATE_TIMER = 0;
    public static final String EXTRA_SCROLL_TO_TIMER_ID = "com.philliphsu.clock2.timers.extra.SCROLL_TO_TIMER_ID";

    private AsyncTimersTableUpdateHandler mAsyncTimersTableUpdateHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAsyncTimersTableUpdateHandler = new AsyncTimersTableUpdateHandler(getActivity(), this);

        long scrollToStableId = getActivity().getIntent().getLongExtra(EXTRA_SCROLL_TO_TIMER_ID, -1);
        if (scrollToStableId != -1) {
            setScrollToStableId(scrollToStableId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        final Resources r = getResources();
        if (getOrientation(r) == Configuration.ORIENTATION_LANDSCAPE) {
            RecyclerView list = findById(view, R.id.list);
            int cardViewMargin = r.getDimensionPixelSize(R.dimen.cardview_margin);
            list.setPaddingRelative(cardViewMargin/*start*/, cardViewMargin/*top*/, 0, list.getPaddingBottom());
        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null)
            return;
        // TODO: From EditTimerActivity, pass back the Timer as a parcelable and
        // retrieve it here directly.
        int hour = data.getIntExtra(EditTimerActivity.EXTRA_HOUR, -1);
        int minute = data.getIntExtra(EditTimerActivity.EXTRA_MINUTE, -1);
        int second = data.getIntExtra(EditTimerActivity.EXTRA_SECOND, -1);
        String label = data.getStringExtra(EditTimerActivity.EXTRA_LABEL);
        boolean startTimer = data.getBooleanExtra(EditTimerActivity.EXTRA_START_TIMER, false);
        // TODO: Timer's group?

        Timer t = Timer.createWithLabel(hour, minute, second, label);
        if (startTimer) {
            t.start();
        }
        mAsyncTimersTableUpdateHandler.asyncInsert(t);
    }

    @Override
    public void onFabClick() {
        Intent intent = new Intent(getActivity(), EditTimerActivity.class);
        startActivityForResult(intent, REQUEST_CREATE_TIMER);
    }

    @Override
    protected TimersCursorAdapter onCreateAdapter() {
        // Create a new adapter. This is called before we can initialize mAsyncTimersTableUpdateHandler,
        // so right now it is null. However, after super.onCreate() returns, it is initialized, and
        // the reference variable will be pointing to an actual object. This assignment "propagates"
        // to all references to mAsyncTimersTableUpdateHandler.
        return new TimersCursorAdapter(this, mAsyncTimersTableUpdateHandler);
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        switch (getOrientation(getResources())) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return new GridLayoutManager(getActivity(), LANDSCAPE_LAYOUT_COLUMNS);
            default:
                return super.getLayoutManager();
        }
    }

    @Override
    protected int emptyMessage() {
        return R.string.empty_timers_container;
    }

    @Override
    protected int emptyIcon() {
        return R.drawable.ic_timer_96dp;
    }

    @Override
    public Loader<TimerCursor> onCreateLoader(int id, Bundle args) {
        return new TimersListCursorLoader(getActivity());
    }

    @Override
    public void onListItemClick(Timer item, int position) {

    }

    @Override
    public void onListItemDeleted(Timer item) {

    }

    @Override
    public void onListItemUpdate(Timer item, int position) {

    }

    @Override
    protected void onScrolledToStableId(long id, int position) {

    }
}
