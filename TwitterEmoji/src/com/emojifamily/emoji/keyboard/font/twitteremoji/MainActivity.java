package com.emojifamily.emoji.keyboard.font.twitteremoji;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Button mShuffleButton;
    private Activity mActivity;
    private MainViewClickListener mMainViewClickListener = new MainViewClickListener();
    private AnimatorSet mAnimatorSet;
    private boolean mShuffleClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.main);
        mShuffleButton = (Button) findViewById(R.id.get_more_button);
        mShuffleButton.setOnClickListener(mMainViewClickListener);
    }

    @Override
    protected void onResume(){
        super.onResume();
        startShuffleAnimation();
    }

    private void startShuffleAnimation() {
        if (!mShuffleClicked) {
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(
                    ObjectAnimator.ofFloat(mShuffleButton, "rotation", 0, 10, -10, 6, -6, 3, -3, 0)
            );
            mAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mAnimatorSet.start();
                }
            });
            mAnimatorSet.setDuration(1500);
            mAnimatorSet.setStartDelay(500);
            mAnimatorSet.start();
        } else {
            cancelShuffleAnimation();
        }
    }

    private void cancelShuffleAnimation() {
        if (mAnimatorSet != null) {
            mAnimatorSet.removeAllListeners();
            mAnimatorSet.end();
            mAnimatorSet.cancel();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelShuffleAnimation();
        //finish();
    }

    private class MainViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.get_more_button:
                    Intent promotion = new Intent(mActivity, ShuffleLoadingActivity.class);
                    mActivity.startActivity(promotion);
                    mShuffleClicked = true;
                    cancelShuffleAnimation();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}


