package com.mazem3dia.mzboxshome;

import android.accessibilityservice.AccessibilityService;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

/**
 * Brings back the remote's shortcut buttons (RemoteButtons) from any app. Keys only: it asks for no accessibility
 * events (res/xml/remote_keys_service.xml), reads no screen content and lets every other key through untouched.
 * A mapped button opens its target on release; a button with nothing to open passes through. Runs only while the
 * owner has it turned on in Settings > Accessibility, the same way cleanyourtv's InputKeyService was tested on
 * this box.
 */
public class RemoteKeysService extends AccessibilityService {

    private HomePrefs prefs;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        prefs = new HomePrefs(this);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int key = event.getKeyCode();
        if (prefs == null || !RemoteButtons.isButton(key)) return false;
        // Read on each press: Customize changes it in this same process, so a pick works at once.
        Intent intent = RemoteButtons.intent(this, prefs.remoteTarget(key));
        if (intent == null) return false;
        if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled() && event.getRepeatCount() == 0) {
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException | SecurityException ignored) {
            }
        }
        return true;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }
}
