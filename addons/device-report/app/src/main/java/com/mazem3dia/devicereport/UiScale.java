package com.mazem3dia.devicereport;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

/**
 * Lays this add-on out at one scale on every box (the same class as in MZ Box S Home): the screen is DESIGN_HEIGHT_DP tall, whatever density the
 * firmware reports. The layout was tuned on the TX6 (1920x1080 at 213 dpi, 1442x811 dp). Google TV boxes such
 * as the Xiaomi TV Box S report 320 dpi for the same 1080p screen (960x540 dp), which made every text and tile
 * half as big again and wrapped the date. Only this app's windows change; Android's own screens keep theirs.
 */
final class UiScale {
    /** The TX6's screen height in dp, which every dimension in res/values was sized for. */
    static final int DESIGN_HEIGHT_DP = 811;

    private UiScale() { }

    /** The override for this screen, or null when the firmware's density already matches (the TX6). */
    static Configuration override(Context context) {
        // The resources' metrics, not DisplayManager: an activity calls this from attachBaseContext, before
        // its own context works. A TV app window is the whole screen, so these are the screen's pixels.
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int shortSide = Math.min(metrics.widthPixels, metrics.heightPixels);
        int longSide = Math.max(metrics.widthPixels, metrics.heightPixels);
        int dpi = Math.round(shortSide * 160f / DESIGN_HEIGHT_DP);
        if (shortSide <= 0 || Math.abs(dpi - metrics.densityDpi) <= 2) return null;
        Configuration config = new Configuration();
        config.densityDpi = dpi;
        config.screenWidthDp = Math.round(longSide * 160f / dpi);
        config.screenHeightDp = Math.round(shortSide * 160f / dpi);
        config.smallestScreenWidthDp = config.screenHeightDp;
        return config;
    }

    /** {@code base} with the TX6 scale, for an activity's attachBaseContext. */
    static Context wrap(Context base) {
        Configuration config = override(base);
        return config == null ? base : base.createConfigurationContext(config);
    }
}
