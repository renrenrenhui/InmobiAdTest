package com.emojifamily.emoji.keyboard.font.twitteremoji;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import com.inmobi.commons.InMobi;

public class CuteEmojiApplication extends MultiDexApplication{
    @Override
    public void onCreate() {
        super.onCreate();
        InMobi.setLogLevel(InMobi.LOG_LEVEL.DEBUG);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
