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

package com.haayhappen.clockplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.haayhappen.clockplus.about.AboutActivity;
import com.haayhappen.clockplus.alarms.ui.AlarmsFragment;
import com.haayhappen.clockplus.intro.IntroActivity;
import com.haayhappen.clockplus.settings.SettingsActivity;

import butterknife.Bind;

public class MainActivity extends BaseActivity {
    public static final int PAGE_ALARMS = 0;
    public static final int REQUEST_THEME_CHANGE = 5;
    public static final String EXTRA_SHOW_PAGE = "com.philliphsu.clock2.extra.SHOW_PAGE";
    public static final int REQUEST_CODE_PICK_LOCATION = 121;
    private static final String TAG = "MainActivity";
    @Bind(R.id.fab)
    FloatingActionButton mFab;
    private AlarmsFragment alarmsFragment;
    private Drawable mAddItemDrawable;
    private SharedPreferences prefs = null;

    public void setLocationPicker(LocationPicker locationPicker) {
        this.locationPicker = locationPicker;
    }

    public interface LocationPicker {
        void onLocationPicked(Place place);
    }

    private LocationPicker locationPicker;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmsFragment = new AlarmsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, alarmsFragment).commit();

        // TODO: @OnCLick instead.
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarmsFragment.onFabClick();
            }
        });

        mAddItemDrawable = ContextCompat.getDrawable(this, R.drawable.ic_add_24dp);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        //TODO delete when published
        startActivity(new Intent(this, IntroActivity.class));
        //Intro
        prefs = getSharedPreferences("com.haayhappen.clockplus", MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        //TODO activate when published
        //if (prefs.getBoolean("firstrun", true)) {
            //startActivity(new Intent(this, IntroActivity.class));
          //  prefs.edit().putBoolean("firstrun", false).commit();
        //}
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_LOCATION) {
            if (data != null) {
                locationPicker.onLocationPicked(PlacePicker.getPlace(this, data));
            }
        }


        if (resultCode != RESULT_OK)
            return;


        // If we get here, either this Activity OR one of its hosted Fragments
        // started a requested Activity for a result. The latter case may seem
        // strange; the Fragment is the one starting the requested Activity, so why
        // does the result end up in its host Activity? Shouldn't it end up in
        // Fragment#onActivityResult()? Actually, the Fragment's host Activity gets the
        // first shot at handling the result, before delegating it to the Fragment
        // in Fragment#onActivityResult().
        //
        // There are subtle points to keep in mind when it is actually the Fragment
        // that should handle the result, NOT this Activity. You MUST start
        // the requested Activity with Fragment#startActivityForResult(), NOT
        // Activity#startActivityForResult(). The former calls
        // FragmentActivity#startActivityFromFragment() to implement its behavior.
        // Among other things (not relevant to the discussion),
        // FragmentActivity#startActivityFromFragment() sets internal bit flags
        // that are necessary to achieve the described behavior (that this Activity
        // should delegate the result to the Fragment). Finally, you MUST call
        // through to the super implementation of Activity#onActivityResult(),
        // i.e. FragmentActivity#onActivityResult(). It is this method where
        // the aforementioned internal bit flags will be read to determine
        // which of this Activity's hosted Fragments started the requested
        // Activity.
        //
        // If you are not careful with these points and instead mistakenly call
        // Activity#startActivityForResult(), THEN YOU WILL ONLY BE ABLE TO
        // HANDLE THE REQUEST HERE; the super implementation of onActivityResult()
        // will not delegate the result to the Fragment, because the requisite
        // internal bit flags are not set with Activity#startActivityForResult().
        //
        // Further reading:
        // http://stackoverflow.com/q/6147884/5055032
        // http://stackoverflow.com/a/24303360/5055032
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_THEME_CHANGE:
                if (data != null && data.getBooleanExtra(SettingsActivity.EXTRA_THEME_CHANGED, false)) {
                    recreate();
                }
                break;
        }
    }


    @Override
    protected int layoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected int menuResId() {
        return R.menu.menu_main;
    }

    @Override
    protected boolean isDisplayHomeUpEnabled() {
        return false;
    }

    @Override
    protected boolean isDisplayShowTitleEnabled() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_THEME_CHANGE);
            return true;
        }
        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }
        if (id == R.id.action_intro) {
            startActivity(new Intent(this, IntroActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
