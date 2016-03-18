package com.mopub.nativeads;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import net.pubnative.library.PubnativeContract;
import net.pubnative.library.model.NativeAdModel;
import net.pubnative.library.request.AdRequest;
import net.pubnative.library.request.AdRequestListener;

import org.droidparts.persist.serializer.JSONSerializer;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class PubNativeAdapter extends CustomEventNative
{
    private static final String PUBNATIVE_AD_CACHE_KEY     = "net.pubnative.ad_cache";
    private static final String PUBNATIVE_APP_TOKEN_KEY    = "net.pubnative.app_token";
    private static final String PUBNATIVE_TIMESTAMP_KEY    = "net.pubnative.timestamp";
    private static final long   PUBNATIVE_CACHE_TIME       = 600000;
    private static final String PUBNATIVE_CACHE_SIZE_VALUE = "10";
    private static final String SERVER_APP_TOKEN_KEY       = "app_token";
    private Context                       context;
    private CustomEventNativeListener     callbackListener;
    private JSONSerializer<NativeAdModel> jsonSerializer;
    private boolean mCache = true;

    @Override
    protected void loadNativeAd(Context context, CustomEventNativeListener customEventNativeListener, Map<String, Object> localExtras, Map<String, String> serverExtras, boolean cache)
    {
        if (jsonSerializer == null)
        {
            jsonSerializer = new JSONSerializer<NativeAdModel>(NativeAdModel.class, context);
        }
        callbackListener = customEventNativeListener;
        mCache = cache;
        this.context = context;
        if (this.context != null)
        {

            SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String currentAppToken = defaultPreferences.getString(PUBNATIVE_APP_TOKEN_KEY, null);
            String serverAppToken = serverExtras.get(SERVER_APP_TOKEN_KEY);
            if (currentAppToken != null && serverAppToken != null && serverAppToken.equals(currentAppToken))
            {
                long currentTimestamp = System.currentTimeMillis();
                long cachedTimestamp = defaultPreferences.getLong(PUBNATIVE_TIMESTAMP_KEY, 0);

                long timeDifference = currentTimestamp - cachedTimestamp;

                if (timeDifference > PUBNATIVE_CACHE_TIME)
                {
                    this.cacheAds();
                }
                else
                {
                    this.serveAd();
                }
            }
            else
            {
                defaultPreferences.edit().putString(PUBNATIVE_APP_TOKEN_KEY, serverAppToken).commit();
                this.cacheAds();
            }
        }
        else
        {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
        }
    }

    private NativeAdModel getCachedAd()
    {
        NativeAdModel result = null;
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        String jsonArrayString = defaultPreferences.getString(PUBNATIVE_AD_CACHE_KEY, null);
        ArrayList<NativeAdModel> ads = null;
        try
        {
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            ads = jsonSerializer.deserializeAll(jsonArray);
        }
        catch (Exception e)
        {
        }
        if (ads != null && ads.size() > 0)
        {
            result = ads.get(0);
            ads.remove(0);
            setCachedAds(ads);
        }
        return result;
    }

    private void setCachedAds(ArrayList<NativeAdModel> ads)
    {
        try
        {
            JSONArray jsonArray = jsonSerializer.serializeAll(ads);
            String jsonArrayString = jsonArray.toString();
            PreferenceManager.getDefaultSharedPreferences(this.context).edit().putString(PUBNATIVE_AD_CACHE_KEY, jsonArrayString).commit();
            Log.d("PubNativeAdapter", "CACHE: Updated - " + ads.size() + " ads remaining");
        }
        catch (Exception e)
        {
            callbackListener.onNativeAdFailed(NativeErrorCode.INVALID_JSON);
        }
    }

    private void serveAd()
    {
        NativeAdModel cachedAd = this.getCachedAd();
        if (cachedAd != null)
        {
            NativeAdModelWrapper w = new NativeAdModelWrapper(context, cachedAd);
            w.fillIn();
            w.cacheImages(callbackListener);
        }
        else
        {
            this.cacheAds();
        }
    }

    private void cacheAds()
    {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        String currentAppToken = defaultPreferences.getString(PUBNATIVE_APP_TOKEN_KEY, null);

        AdRequest req = new AdRequest(this.context);// currentAppToken, APIEndpoint.NATIVE);
        req.setParameter(PubnativeContract.Request.APP_TOKEN, currentAppToken);
        req.setParameter(PubnativeContract.Request.AD_COUNT, PUBNATIVE_CACHE_SIZE_VALUE);

        // TODO: Configure your ad requirements here 
        //        req.setParam("icon_size", "YOUR_SIZE_HERE");
        //        req.setParam("banner_size", "YOUR_SIZE_HERE");

        req.start(AdRequest.Endpoint.NATIVE, new AdRequestListener()
        {
            @Override
            public void onAdRequestStarted(AdRequest request)
            {
                // Do nothing
            }

            @Override
            public void onAdRequestFinished(AdRequest request, ArrayList<? extends NativeAdModel> ads)
            {
                if (ads.size() > 0)
                {
                    PreferenceManager.getDefaultSharedPreferences(PubNativeAdapter.this.context).edit().putLong(PUBNATIVE_TIMESTAMP_KEY, System.currentTimeMillis()).commit();

                    setCachedAds((ArrayList<NativeAdModel>) ads);
                    serveAd();
                }
                else
                {
                    callbackListener.onNativeAdFailed(NativeErrorCode.NETWORK_NO_FILL);
                }
            }

            @Override
            public void onAdRequestFailed(AdRequest request, Exception ex)
            {
                callbackListener.onNativeAdFailed(NativeErrorCode.CONNECTION_ERROR);
            }
        });
    }

    private class NativeAdModelWrapper extends BaseForwardingNativeAd
    {
        private final NativeAdModel ad;
        private Context context;

        public NativeAdModelWrapper(Context context, NativeAdModel ad)
        {
            this.context = context;
            this.ad = ad;
        }

        void fillIn()
        {
            setTitle(ad.title);
            setText(ad.description);
            setIconImageUrl(ad.iconUrl);
            setMainImageUrl(ad.bannerUrl);
            setStarRating((double) ad.getStoreRating());
            setCallToAction(ad.ctaText);
            addImpressionTracker(ad.getBeaconURL(PubnativeContract.Response.NativeAd.Beacon.TYPE_IMPRESSION));
        }

        void cacheImages(final CustomEventNativeListener l) {
            //
            if (mCache) {
                String[] imgUrls = new String[]{ad.iconUrl, ad.bannerUrl};
                ImageListener il = new ImageListener() {
                    @Override
                    public void onImagesCached() {
                        l.onNativeAdLoaded(NativeAdModelWrapper.this);
                    }

                    @Override
                    public void onImagesFailedToCache(NativeErrorCode errorCode) {
                        l.onNativeAdFailed(errorCode);
                    }
                };
                preCacheImages(this.context, Arrays.asList(imgUrls), il);
            } else {
                l.onNativeAdLoaded(NativeAdModelWrapper.this);
            }
        }


        @Override
        public void handleClick(View view)
        {
            ad.open(this.context);
        }
    }
}
