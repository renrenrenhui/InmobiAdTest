package com.emojifamily.emoji.keyboard.font.twitteremoji;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mopub.nativeads.MoPubNativeAdLoader;
import com.mopub.nativeads.NativeResponse;

import java.lang.ref.WeakReference;

public class ShuffleLoadingActivity extends Activity {
    private static final String TAG = ShuffleLoadingActivity.class.getSimpleName();

    private ImageView mLoadingView;
    private LinearLayout mNativeAdContainer;
    private Button mRefreshBtn;
    private Button mCloseBtn;

    private AnimationDrawable mLoadingAnimation;

    private static final int MSG_TO_DELAY = 1;

    private static final int NATIVE_AD_CACHE_LIMIT = 1;

    private ShuffleHandler mHandler = new ShuffleHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shuffle_loading_layout);

        mLoadingView = (ImageView) findViewById(R.id.loading);
        mLoadingView.setImageResource(R.drawable.shuffle_loading);
        mLoadingAnimation = (AnimationDrawable) mLoadingView.getDrawable();

        mNativeAdContainer = (LinearLayout) findViewById(R.id.native_ad_container);

        mRefreshBtn = (Button) findViewById(R.id.refresh);
        mRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNativeAd();
            }
        });

        mCloseBtn = (Button) findViewById(R.id.close);
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        MoPubNativeAdLoader.init(this, NATIVE_AD_CACHE_LIMIT);
        MoPubNativeAdLoader.setNativeAdsListener(new MoPubNativeAdsListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleNativeAd();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void handleNativeAd() {
        NativeResponse response = MoPubNativeAdLoader.getResponse();
        if (response != null) {
            showNativeAdUI(response);
        } else {
            showLoadingUI();
            MoPubNativeAdLoader.loadAds("fbc38362c0c14f139f641aadb0f76d16", false);
            mHandler.sendEmptyMessageDelayed(MSG_TO_DELAY, 15000);
        }
    }

    private void showNativeAdUI(@NonNull NativeResponse response) {
        final ImageView coverImage = (ImageView) findViewById(R.id.ad_image);
        coverImage.setImageDrawable(null);
        final ImageView iconImage = (ImageView) findViewById(R.id.ad_icon);
        iconImage.setImageDrawable(null);
        final TextView titleTxt = (TextView) findViewById(R.id.ad_title);
        final TextView descTxt = (TextView) findViewById(R.id.ad_desc);
        final TextView actionTxt = (TextView) findViewById(R.id.call_to_action);

        titleTxt.setText(response.getTitle());
        descTxt.setText(response.getText());
        actionTxt.setText(response.getCallToAction());
        try {
            Glide.with(this)
                    .load(response.getMainImageUrl())
                    .into(coverImage);
            Glide.with(this)
                    .load(response.getIconImageUrl())
                    .into(iconImage);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        MoPubNativeAdLoader.prepare(mNativeAdContainer, response);
        mNativeAdContainer.setVisibility(View.VISIBLE);
        mCloseBtn.setVisibility(View.VISIBLE);
        mRefreshBtn.setVisibility(View.VISIBLE);
        mLoadingView.setVisibility(View.INVISIBLE);
        
        mHandler.removeMessages(MSG_TO_DELAY);
    }

    private void showLoadingUI() {
        mNativeAdContainer.setVisibility(View.INVISIBLE);
        mRefreshBtn.setVisibility(View.INVISIBLE);
        mLoadingView.setVisibility(View.VISIBLE);
        mLoadingAnimation.start();
        mCloseBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        MoPubNativeAdLoader.destroy();
        super.onDestroy();
    }

    private static class ShuffleHandler extends Handler {
        private final WeakReference<ShuffleLoadingActivity> mActivity;

        public ShuffleHandler(ShuffleLoadingActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ShuffleLoadingActivity activity = mActivity.get();
            if (activity != null) {
                if (msg.what == MSG_TO_DELAY) {
                    if (!MoPubNativeAdLoader.isAdsLoaded()) {
                        Toast.makeText(activity, R.string.net_unavailable, Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }
                }
            }
        }
    }

    private class MoPubNativeAdsListener implements MoPubNativeAdLoader.NativeAdsListener {

        @Override
        public void onAdsLoaded() {
            if (mNativeAdContainer != null && mNativeAdContainer.getVisibility() != View.VISIBLE) {
                handleNativeAd();
            }
        }
    }
}

