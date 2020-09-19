package com.mark.imagecropanimation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mark.imagecropanimation.crop.CropAnimationView;
import com.mark.imagecropanimation.crop.FixedCropImageView;
import com.mark.imagecropanimation.crop.SimpleTransitionListener;

public class MainActivity extends AppCompatActivity {


    FixedCropImageView fixedCropImageView;
    CropAnimationView cropAnimationView;

    Scene sceneStart;
    Scene sceneEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewGroup sceneRoot = findViewById(R.id.scene_root);

        View sceneStartLayout = LayoutInflater.from(this).inflate(R.layout.scene_start, null);
        View sceneEndLayout = LayoutInflater.from(this).inflate(R.layout.scene_end, null);

        fixedCropImageView = sceneStartLayout.findViewById(R.id.crop_image_view);
        cropAnimationView = sceneEndLayout.findViewById(R.id.crop_image_view);

        sceneStart = new Scene(sceneRoot, sceneStartLayout);
        sceneEnd = new Scene(sceneRoot, sceneEndLayout);

        Glide.with(fixedCropImageView).asBitmap().load(R.drawable.test_03).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                fixedCropImageView.setImageToCrop(bitmap);
                cropAnimationView.setStartMatrix(
                        bitmap,
                        fixedCropImageView.getWidth(),
                        fixedCropImageView.getHeight()
                );

            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });

        TransitionManager.go(sceneStart);
    }

    public void onCropClick(View view) {

        cropAnimationView.setEndMatrix(
                fixedCropImageView.getCropPointsInView(),
                fixedCropImageView.getCorrectBitmapInViewBound());

        final Bitmap crop = fixedCropImageView.crop();

        TransitionSet transitionSet = new TransitionSet();

        transitionSet.addListener(new SimpleTransitionListener() {

            @Override
            public void onTransitionStart(android.transition.Transition transition) {
                cropAnimationView.startCropAnimation();
            }
        });

        cropAnimationView.setAnimatorListenerAdapter(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //cropAnimationView.setEndBitmap(crop,fixedCropImageView.getWidth(),fixedCropImageView.getHeight());
            }
        });


        TransitionManager.go(sceneEnd, transitionSet);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }
}