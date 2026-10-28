package com.mazem3dia.mzboxshome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;

/**
 * The Xiaomi RC's shortcut buttons, which only Google TV's launcher (launcherx) answered: once it is disabled they
 * do nothing. Keycodes from the box's Vendor_2717_Product_32b9.kl, tested by cleanyourtv contributors on builds 737
 * and 772 (examples/xiaomi-mi-box-s-2nd-gen). Each button opens what the owner picked in Customize > Remote buttons:
 * an app, the Apps grid of this launcher, or nothing. RemoteKeysService catches the keys; it has to be turned on
 * in Settings > Accessibility (or over adb), as Android hands these keys to no app window otherwise.
 */
final class RemoteButtons {
    static final int NETFLIX = 193;      // KEY_VIDEOPHONE -> BUTTON_6
    static final int YOUTUBE = 190;      // KEY_PRESENTATION -> BUTTON_3
    static final int PRIME_VIDEO = 194;  // KEY_PLAY -> BUTTON_7
    static final int XIAOMI_TV_PLUS = 192;   // KEY_WWW -> BUTTON_5
    static final int APPS = 284;         // KEY_CHAT -> ALL_APPS: Android grabs it before any app window

    /** The buttons in Customize order; names in R.array.remote_buttons. */
    static final int[] KEYS = {NETFLIX, YOUTUBE, PRIME_VIDEO, XIAOMI_TV_PLUS, APPS};

    /** Target: this launcher's Apps grid. */
    static final String APPS_GRID = ":apps";
    /** Target: the button does what Android does without the launcher (nothing on this box). */
    static final String NOTHING = "";

    static final String PRIME_PACKAGE = "com.amazon.amazonvideo.livingroom";

    private RemoteButtons() { }

    /** What a button opens until the owner picks something: the app printed on it. */
    static String defaultTarget(int key) {
        switch (key) {
            case NETFLIX: return "com.netflix.ninja";
            case YOUTUBE: return "com.google.android.youtube.tv";
            case PRIME_VIDEO: return PRIME_PACKAGE;
            case APPS: return APPS_GRID;
            default: return NOTHING;
        }
    }

    static boolean isButton(int key) {
        for (int k : KEYS) if (k == key) return true;
        return false;
    }

    /** RemoteKeysService is on in Settings > Accessibility. */
    static boolean serviceOn(Context context) {
        String on = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (on == null) return false;
        ComponentName service = new ComponentName(context, RemoteKeysService.class);
        for (String name : on.split(":")) {
            ComponentName c = ComponentName.unflattenFromString(name);
            if (service.equals(c)) return true;
        }
        return false;
    }

    /** The intent a target opens, or null: nothing chosen, or the app is gone or disabled. */
    static Intent intent(Context context, String target) {
        if (target.isEmpty()) return null;
        if (target.equals(APPS_GRID)) {
            return new Intent(context, MainActivity.class).putExtra(MainActivity.EXTRA_SHOW_APPS, true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLeanbackLaunchIntentForPackage(target);
        if (intent == null) intent = pm.getLaunchIntentForPackage(target);
        if (intent == null) return null;
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    }
}
