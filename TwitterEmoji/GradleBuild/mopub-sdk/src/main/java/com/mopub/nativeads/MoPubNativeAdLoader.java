package com.mopub.nativeads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

public class MoPubNativeAdLoader {
    private static Context mContext;
    private static NativeAdSource mNativeAdSource;
    private static ImpressionTracker mImpressionTracker;

    private static boolean mIsNativeAdsLoaded = false;

    private static final int CACHE_LIMIT_DEFAULT = 1;

    private static NativeAdsListener mListener;

    public interface NativeAdsListener {
        void onAdsLoaded();
    }

    private static NativeAdSource.AdSourceListener mAdSourceListener = new NativeAdSource.AdSourceListener() {
        @Override
        public void onAdsAvailable() {
            mIsNativeAdsLoaded = true;
            if (mListener != null) {
                mListener.onAdsLoaded();
            }
        }
    };

    public static void init(Context context) {
        init(context, CACHE_LIMIT_DEFAULT);
    }

    public static void init(Context context, int cacheLimit) {
        mContext = context;
        mNativeAdSource = new NativeAdSource(cacheLimit);
        mImpressionTracker = new ImpressionTracker(context);
    }

    public static void loadAds(String unitId, boolean cache) {
        mIsNativeAdsLoaded = false;
        mNativeAdSource.setAdSourceListener(mAdSourceListener);
        mNativeAdSource.loadAds(mContext, unitId, null, cache);
    }

    public static NativeResponse getResponse() {
        if (!mIsNativeAdsLoaded) {
            return null;
        }
        return mNativeAdSource.dequeueAd();
    }

    public static void prepare(View view, @NonNull NativeResponse response) {
        if (!response.isOverridingImpressionTracker()) {
            mImpressionTracker.addView(view, response);
        }
        response.prepare(view);
    }

    public static boolean isAdsLoaded() {
        return mIsNativeAdsLoaded;
    }

    public static void destroy() {
        if (mNativeAdSource != null) {
            mNativeAdSource.clear();
        }

        if (mImpressionTracker != null) {
            mImpressionTracker.destroy();
        }
    }

    public static void setNativeAdsListener(NativeAdsListener listener) {
        mListener = listener;
    }

}
