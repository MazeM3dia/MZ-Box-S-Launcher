# Xiaomi TV Box S launcher (MZ Box S Home)

A lightweight Android home-screen launcher for the Xiaomi TV Box S 2nd Gen, started on 2026-10-28 as a copy of
MZ TX6 Home (../tx6-launcher, commit c47c297, now paused). Comments that mention the TX6 are from that origin.
The cleanyourtv copy in ../cleanyourtv-main and the plan in ../Xiaomi tv box s project.md are REFERENCE ONLY:
read them, borrow ideas, don't edit them.

## Target device
- Xiaomi TV Box S 2nd Gen, model MiTV-AFKR0, codename "jaws"; Google TV, Android 11 (API 30)
- Amlogic S905X4 (4x Cortex-A55, 2 GHz), Mali-G31 MP2; 2 GB RAM, 8 GB eMMC (keep the APK small)
- Known builds: RTT0.211222.001 737 and 772 (check which one this unit runs)
- Wi-Fi ac, Bluetooth 5.2, 1x USB 2.0, HDMI 2.0 out. No tuner or HDMI in, so no Inputs/Live TV feature
- Remote: Bluetooth "Xiaomi RC" (Vendor_2717_Product_32b9.kl) with mic; shortcut keys Netflix 193 (BUTTON_6),
  YouTube 190 (BUTTON_3), Prime Video 194 (BUTTON_7), Xiaomi TV+ 192 (BUTTON_5), Apps 284 (ALL_APPS, grabbed by
  Android before any app window). They go dead once the stock launcher is disabled, unless the launcher maps them
- Stock launcher: com.google.android.apps.tv.launcherx (HOME priority 2); other HOME claimants:
  com.google.android.tungsten.setupwraith/.RecoveryActivity (priority 1), Settings FallbackHome
- Box notes: ../cleanyourtv-main/examples/xiaomi-mi-box-s-2nd-gen/debloat.txt (read the header comments)

## Code
- Package `com.mazem3dia.mzboxshome`. Plain Java, Android framework only: no AndroidX, no app dependencies
  (JUnit is test-only)
- `MainActivity`: Home screen (clock, date, network label, Customize and Settings buttons, Favourites row,
  Apps grid), tile menu (Move, favourite, Hide, App info), and the Customize panel's pages
- `CustomizePanel`: the Customize side panel (built while open, removed on close). Look (theme, wallpaper, apps per
  row, button labels, animations), Home (open on start, hidden apps, reset order, default home),
  Waktu solat (show on Home, zone, open the add-on), Debug (demo apps). Every change applies at once, no
  recreate. Left/Right change a value, OK opens or picks, Back goes up then closes
- `PrayerHeader` + `Prayers`: with the Waktu Solat add-on (addons/waktu-solat), the header shows the Hijri date
  above the Malay date and the next prayer under it. Read through the add-on's provider
  (content://com.mazem3dia.waktusolat), so the launcher keeps no INTERNET permission
- Home key: back from an app focuses that app's tile; pressed on Home, back to the top. Open on start opens
  its app once per boot (Settings.Global.BOOT_COUNT, else boot time in minutes)
- `AppLoader`: finds LAUNCHER and LEANBACK_LAUNCHER apps; `AppEntry`: one tile
- `Arrangement`: order and move logic on package names, unit-tested in `app/src/test`
- `HomePrefs`: owner's choices in SharedPreferences; `Palette`: 5 colour themes, see-through pills and the
  favourites band, all drawn in code (no images, no blur)
- Look (after the original launcher's default theme): greeting, thin clock, divider, spaced capital date;
  hero line with the focused app's name; favourites in a band without names; names under the focused grid
  tile only; radial glow in the focused app's colour (`GlowColor`, picked from a 32x18 sample of its artwork
  at load; Customize can switch it off); Button labels off gives round icon buttons
- Type: Inter Display (Light, Regular, Bold) in res/font, trimmed to Latin with fonttools pyftsubset
  (about 51 KB each; licence FONT-LICENSE-OFL.txt). Every text uses `Font.Display` (clock, hero),
  `Font.Text` or `Font.Bold` in res/values/themes.xml; the clocks use tabular digits (`tnum`).
  Originals stay outside the repo in ../inbox/inter
- `Capacity`: RAM and storage in use under the header buttons, "[icon] 0.7GB/2.0GB · [icon] 0.2GB/5.8GB",
  read by the launcher itself (ActivityManager, StatFs). Clock minute tick; orange above 85 % RAM in use;
  Customize > Home > Memory and storage
- Icons: Lucide (ISC) outlines, stroke 2, converted from the owner's SVGs (ram, storage, customize, settings,
  wifi, ethernet); Waktu Solat uses the same set
- `Wallpaper`: Customize > Look > Wallpaper, its own page: one background (HomePrefs.background()): None
  (theme colours), App colour (the GlowColor glow, formerly its own switch) or one of five built-in pictures in
  res/drawable-nodpi (the owner's Pixabay picks, CREDITS.md) with Dim 0-80 %. Pictures are decoded on the loader
  in RGB_565 at screen size (~4 MB), dropped in onTrimMemory. Custom: "Choose a picture" lists MediaStore images
  (internal storage, scanned USB sticks), and Wallpaper.importPicture copies the pick upright and screen-sized
  to files/custom_wallpaper.jpg (Wallpaper.CUSTOM), so starts need neither the permission nor the original
- `NetworkStatus`: Wi-Fi/Ethernet label, orange "No internet" (not VALIDATED), red "Offline"; listens only
  between onStart and onStop
- `ClockDream`: screensaver (Settings > Screen saver > MZ Box S Clock)
- `RemoteButtons` + `RemoteKeysService`: the Xiaomi RC's shortcut buttons (Netflix 193, YouTube 190, Prime Video
  194, Xiaomi TV+ 192, Apps 284) open what Customize > Remote buttons picks per button: an app, the Apps grid
  (`MainActivity.EXTRA_SHOW_APPS`) or nothing. Defaults: the app printed on the button, Apps = Apps grid, TV+ =
  nothing. Ported from cleanyourtv's box-tested InputKeyService/app_keys: a keys-only accessibility service (no
  events, no window content), off until turned on in Settings > Accessibility (MZ Box S Remote buttons) or adb.
  A button whose app is missing passes through. Prime Video starts out in Hidden apps (firmware re-enables it)
- Tile width comes from the screen width and `res/values/integers.xml` columns, so any density fills the row
- Tile menu opens on OK *release* after a hold (first key repeat), or on Menu: a dialog shown while a key is
  still down never gets key focus on Android 9. It is a platform Dialog (style Dialog: see-through, no min width)
  holding a themed card (tile_menu.xml, Palette.menu(), rows from panel_row.xml) so it matches Customize
- Permissions: ACCESS_NETWORK_STATE (read-only network state), and read photos (READ_EXTERNAL_STORAGE up to
  API 32, READ_MEDIA_IMAGES after) asked only by Customize > Wallpaper > Choose a picture. Never INTERNET: network features live in
  add-ons (separate apps), which the launcher reads through their providers

## Testing
- Remote buttons: `settings put secure enabled_accessibility_services
  com.mazem3dia.mzboxshome/.RemoteKeysService` and `settings put secure accessibility_enabled 1` (overwrites
  any other enabled service: read the old value first and put it back). Then `input keyevent 193` (Netflix),
  190, 194, 192, 284. Undo: `settings delete secure enabled_accessibility_services`
- Unit tests: `./gradlew testReleaseUnitTest`
- Emulator: no API 30 TV image installed yet; `Television_1080p` or `old_android_tv` (API 28 TV). The box
  runs a 1920x1080 UI (density to confirm on the box); reset any density change with `adb shell wm density reset`. Hold OK: `adb shell input keyevent --longpress DPAD_CENTER`
- Screensaver: `settings put secure screensaver_components com.mazem3dia.mzboxshome/.ClockDream`, then
  `am start -n com.android.systemui/.Somnambulator`; put the old value back afterwards
- Home on the emulator: Google's TV launcher declares HOME priority 2, so `cmd package set-home-activity` is
  ignored. To test as Home: `pm disable-user --user 0 com.google.android.tvlauncher`; undo with
  `pm enable com.google.android.tvlauncher`
- Many apps: the debug build (what Android Studio runs) has Customize > Demo apps, 40 fake tiles from
  `src/debug/.../DemoApps.java`; `src/release` has an empty twin, so the release APK carries none of it
- No internet: point `captive_portal_http(s)_url` (settings global) at `127.0.0.1:9`, toggle Wi-Fi; then
  `settings delete` both and toggle again

## Build rules
- minSdk 26, targetSdk 34, compileSdk 35 (installed)
- Gradle 8.11.1 + AGP 8.7.3 on JDK 21 (`~/.jdks/jbr-21.0.11`). Android Studio's bundled JDK 25 is too new
  for Gradle 8.11: in Android Studio set Settings > Build Tools > Gradle > Gradle JDK to jbr-21
- Command line, from this folder:
  `JAVA_HOME=~/.jdks/jbr-21.0.11 ./gradlew --no-daemon testReleaseUnitTest assembleRelease lintRelease`
- Lint must stay at zero errors. No INTERNET permission, no background services (except the owner-enabled,
  keys-only RemoteKeysService and the ClockDream screensaver), no blur/video, minimal deps

## Device rules
- Ask before running any command that changes the device
- Disable, never uninstall; always keep a working Home app
- HOME priority: Android only offers the highest-priority Home apps. Ours is 0; launcherx declares 2, so it wins
  over any default and hides the picker. To make ours Home, launcherx has to be disabled, and then setupwraith
  too (else Home goes to its RecoveryActivity and `cmd package set-home-activity` says Success but does
  nothing). Disable setupwraith only after our launcher is installed; re-enable both before a factory reset
- Right after disabling setupwraith, Wi-Fi drops for about 20 s and asks for the password once. On build 772,
  also forget the hidden first-setup network (`cmd wifi list-networks`, `cmd wifi forget-network <id>`)
- Prime Video (com.amazon.amazonvideo.livingroom) gets re-enabled by firmware/Play after reboots
- Way back: `pm enable com.google.android.apps.tv.launcherx` (and setupwraith), then pick it as Home

## Box setup and debloat
- SETUP-GUIDE.md: the owner's step-by-step (stages 0-7, who does what: Box / PC / Phone / Claude)
- debloat/debloat.txt: Tier 1 tested on this box, Tier 2 sibling-tested (commented), Tier 3 Home switch
  (commented: done by hand in guide stage 6, setupwraith before launcherx)
- tools/: tv.py, disable.ps1, restore.ps1, recovery.py, check.py copied unchanged from cleanyourtv (MIT,
  tools/LICENSE-cleanyourtv.txt). They need tools/venv (`python -m venv tools/venv`, pip install -r
  tools/requirements.txt); undo records go to ~/.cleanyourtv/

## Useful references
- ../cleanyourtv-main/launcher/app/src/main/AndroidManifest.xml, MainActivity.java, GridNavigation.java
- ../cleanyourtv-main/examples/xiaomi-mi-box-s-2nd-gen/ (this box), ../cleanyourtv-main/launcher (app_keys remap)
- ../cleanyourtv-main/docs/TROUBLESHOOTING.md
