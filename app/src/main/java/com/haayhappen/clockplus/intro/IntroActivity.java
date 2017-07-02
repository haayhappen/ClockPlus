package com.haayhappen.clockplus.intro;

import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.haayhappen.clockplus.R;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.animations.IViewTranslation;

/**
 * Created by Fynn on 21.02.2017.
 */

public class IntroActivity extends MaterialIntroActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableLastSlideAlphaExitTransition(true);

        getBackButtonTranslationWrapper()
                .setEnterTranslation(new IViewTranslation() {
                    @Override
                    public void translate(View view, @FloatRange(from = 0, to = 1.0) float percentage) {
                        view.setAlpha(percentage);
                    }
                });

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.intro_slides_background)
                        .buttonsColor(R.color.intro_slides_buttons)
                       .image(R.drawable.intro_slides_2)
                        .title("Easy Setup!")
                        .description("Add as many alarms as you want.")
                        .build());
        /*,
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMessage("We provide solutions to make you love your work");
                    }
                }, "Set up Alarm"));
*/
        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.intro_slides_background)
                .buttonsColor(R.color.intro_slides_buttons)
                .image(R.drawable.intro_slides_1)
                .title("Add Routes!")
                .description("Tell the alarm where you want to go.")
                .build());


/*
       addSlide(new CustomSlide());

       addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.intro_slides_background)
                        .buttonsColor(R.color.intro_slides_buttons)
                        .possiblePermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_SMS})
                        //.neededPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
                        //.image(R.drawable.img_equipment)
                        .title("We provide best tools")
                        .description("ever")
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMessage("Try us!");
                    }
                }, "Tools"));
*/
        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.intro_slides_background)
                .buttonsColor(R.color.intro_slides_buttons)
                .image(R.drawable.intro_slides_3)
                .title("You sleep!")
                .description("We watch the traffic and wake you up earlier if there is a traffic jam.")
                .build());
    }

    @Override
    public void onFinish() {
        super.onFinish();
        Toast.makeText(this, "Awesome App!", Toast.LENGTH_SHORT).show();
    }
}
