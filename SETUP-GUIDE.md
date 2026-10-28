# Xiaomi TV Box S 2nd Gen: step-by-step setup

From a wiped box to MZ Box S Home as the Home screen, with the bloat switched off. Every step says what it
needs, and the stages are in order: finish one before starting the next.

**Needs:**

| Tag | What |
|---|---|
| **Box** | The Xiaomi box, its remote and the TV |
| **PC** | The Windows PC with Android Studio (adb lives in `%LOCALAPPDATA%\Android\Sdk\platform-tools`). It is on an Ethernet cable to the router; the box is on Wi-Fi from the same router (see 2.1) |
| **Phone** | Your phone (LocalSend, to send APKs without adb) |
| **Claude** | **Claude Code** on that PC (see below). Claude asks before every command that changes the box |

**Which Claude to use**

| Claude | Use it for this setup? |
|---|---|
| **Claude Code in VS Code** (the Claude panel you use now) | **Yes, this one.** It runs on the PC, so it can run adb, reach the box on your home network, build the APKs and edit this repo. Open the folder `C:\Users\muham\Desktop\tx6-launcher-custom` (the workspace, not a subfolder): its project notes and memory are tied to that folder |
| Claude Code in the Claude desktop app (Code tab) or in a terminal | Also works, same abilities, as long as it is opened on that same folder. Pick one and stick to it |
| Claude desktop app chat, claude.ai in a browser, Claude phone app | **No** for the steps marked Claude: they run in the cloud, can't reach the box or run adb, and don't see this repo. Fine for reading this guide or asking a general question away from the PC |

**Safety rules for every stage**

- Apps are *disabled*, never uninstalled. `adb shell pm enable <package>` undoes any one of them.
- The box always keeps a working Home screen.
- Keep the real remote with you: if Wi-Fi drops, the phone remote and adb drop with it.

Sources: the cleanyourtv list tested on this model (builds 737 and 772,
`../cleanyourtv-main/examples/xiaomi-mi-box-s-2nd-gen/debloat.txt`) and its `docs/TROUBLESHOOTING.md`.

---

## Stage 0: Before you go home (no box needed)

| # | Step | Needs |
|---|---|---|
| 0.1 | Android 11 TV emulator: **done** (`android_11_tv`: Google TV, API 30, 1920x1080, density 320). Start it from Android Studio's Device Manager when Claude needs it | PC |
| 0.2 | **Done**: MZ Box S Home and the add-ons checked on the emulator. The real shortcut buttons can only be tested on the box (the emulator keyboard has none, and `adb shell input keyevent` skips the helper) | Claude, PC |
| 0.3 | **Done** (28 Oct): emulator fixes in, APKs ready in `../test-builds/xiaomi-box-s/` | Claude |
| 0.4 | Set up the debloat tools once, in PowerShell in `xiaomi-tv-box-s-launcher`: `python -m venv tools\venv` then `tools\venv\Scripts\python.exe -m pip install -r tools\requirements.txt` (the tools come from cleanyourtv, MIT). Claude's safety check blocks this download, so you run it, or allow it when Claude asks | PC (you) |
| 0.5 | Read through [debloat/debloat.txt](debloat/debloat.txt) and comment out anything you want to keep (put `#` in front) | PC |

## Stage 1: Box settings, no PC needed

| # | Step | Needs |
|---|---|---|
| 1.1 | **Re-join the Wi-Fi.** The network you entered during first setup belongs to the setup wizard, and Android deletes it when the wizard is turned off (Stage 6). Settings > Network & Internet > your network > **Forget network**, then pick it again and enter the password | Box |
| 1.2 | **Usage & diagnostics off**: Settings > Privacy > Usage & diagnostics. Do it now: this page comes from the setup wizard and may be gone after Stage 6 | Box |
| 1.3 | **Ads**: Settings > Privacy > Ads > turn on *Opt out of Ads Personalization* (or delete the advertising ID) | Box |
| 1.4 | **Scanning always available off**: Settings > Network & Internet | Box |
| 1.5 | **Developer options**: Settings > System > About > press OK on **Android TV OS build** 7 times | Box |
| 1.6 | **USB debugging on**: Settings > System > Developer options > USB debugging. On this box adb then answers over the network on port 5555 | Box |
| 1.7 | **Note the box's IP**: Settings > Network & Internet > your network. A fixed address in the router saves trouble later | Box |
| 1.8 | Write down which preinstalled apps you already removed (tell Claude, it marks them in the list) | Box |

## Stage 2: Connect the PC

| # | Step | Needs |
|---|---|---|
| 2.1 | **Same network, cable and Wi-Fi.** The PC on Ethernet and the box on Wi-Fi can talk as long as both go through the same router. Check: in PowerShell, `ipconfig` shows the PC's IPv4 address under the *Ethernet adapter*; it and the box's IP (1.7) should start the same, e.g. `192.168.1.20` and `192.168.1.45`. The box must **not** be on a *guest* Wi-Fi (guest networks block devices from reaching each other) | PC, Box |
| 2.2 | Reach test: `Test-NetConnection <box-ip> -Port 5555` says `TcpTestSucceeded : True` | Claude |
| 2.3 | `adb connect <box-ip>:5555` | Claude |
| 2.4 | On the TV, the *Allow USB debugging?* prompt: tick **Always allow from this computer**, OK | Box |
| 2.5 | `adb devices` shows the box as `device` (not `unauthorized`) | Claude |

## Stage 3: Read-only check (changes nothing)

Claude runs these and tells you what it finds before anything changes:

| # | Check | Command | Needs |
|---|---|---|---|
| 3.1 | Model, Android version and build (737 or 772?) | `adb shell getprop ro.build.fingerprint` | Claude |
| 3.1b | Screen size and density, so the emulator matches the box | `adb shell wm size`, `adb shell wm density` | Claude |
| 3.2 | Who owns the saved Wi-Fi: must **not** say setupwraith (else redo 1.1) | `adb shell dumpsys wifi \| findstr cname=` | Claude |
| 3.3 | On build 772: an old hidden network from first setup. Forget it if it is there | `adb shell cmd wifi list-networks`, then `cmd wifi forget-network <id>` | Claude, with your OK |
| 3.4 | Which apps answer Home and which one has it | `adb shell cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME` | Claude |
| 3.5 | Which packages from the list are on the box | `adb shell pm list packages` | Claude |

## Stage 4: Back up

| # | Step | Needs |
|---|---|---|
| 4.1 | Save every package and every disabled package to files in `../test-builds/box-backup/` | Claude |
| 4.2 | The debloat script (Stage 5) also writes its own undo record in `%USERPROFILE%\.cleanyourtv\` before its first change | automatic |

## Stage 5: Debloat, Tier 1 (tested on this exact box)

| # | Step | Needs |
|---|---|---|
| 5.1 | Run the list: `powershell -ExecutionPolicy Bypass -File tools\disable.ps1 -TvHost <box-ip> debloat\debloat.txt` (needs 0.4). It refuses core Android packages and never takes away the app that owns Home; the Home-switch lines are commented out, so Home stays with Google TV for now | Claude, with your OK |
| 5.1b | The tools may connect with a key of their own: if the TV asks *Allow USB debugging?* again, tick Always allow and OK | Box |
| 5.2 | Look at the TV: Home, Wi-Fi, YouTube and the remote still fine | Box |
| 5.3 | Restart the box once, check again | Box |

Turned off by Tier 1: Xiaomi and Google tracking, the Xiaomi ad rows and PatchWall, partner app
auto-installs, the Xiaomi update service, Prime Video, Play Movies, Play Games, the print spooler.
The update service being off means no firmware updates: turn it back on (`pm enable
com.xiaomi.mitv.updateservice`) when you want to check for one.

## Stage 5b: Debloat, Tier 2 (optional, tested on similar boxes only)

| # | Step | Needs |
|---|---|---|
| 5b.1 | Pick one from Tier 2 in debloat.txt and remove its `#` | PC |
| 5b.2 | Before `dreamx` / `backdrop` (Google's screensaver): Settings > System > Ambient mode / Screen saver > **MZ Box S Clock** | Box |
| 5b.3 | Run the list again (same command as 5.1). Use the box for a day before the next one | Claude |

## Stage 6: MZ Box S Home and the Home switch

| # | Step | Needs |
|---|---|---|
| 6.1 | Install the ready-made APKs from `..\test-builds\xiaomi-box-s\` (MZ-Box-S-Home-0.1, Waktu-Solat-2.1, Device-Report-1.1, Capacity-Stats-1.1; its README.txt has the first-time checks): Claude runs `adb install -r <apk>` for each. Without adb: install LocalSend on the box from Play Store, send the APKs from the PC or phone, open them on the box and allow installing from LocalSend | Claude, or Phone + Box |
| 6.2 | Use it from the apps list for a while. Google TV keeps Home until it is turned off, so there is no "Always" choice yet | Box |
| 6.3 | **Remote buttons**: Settings > System > Accessibility, in the services list > **MZ Box S Remote buttons** > on (or Claude turns it on over adb). Then Customize > Remote buttons: pick what each button opens | Box (or Claude) |
| 6.4 | Check again that the Wi-Fi is not owned by setupwraith (3.2) | Claude |
| 6.5 | Turn off the setup wizard **first**: `adb shell pm disable-user --user 0 com.google.android.tungsten.setupwraith`. Wi-Fi may drop for ~20 s and ask for the password once: enter it with the remote. adb drops with it: Claude runs `adb connect <box-ip>:5555` again once the box is back on Wi-Fi | Claude, Box |
| 6.6 | Then Google TV's Home: `adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx` | Claude |
| 6.7 | Press Home on the remote: MZ Box S Home should come up | Box |
| 6.8 | Claude checks what really has the screen: `adb shell dumpsys activity activities \| findstr mFocusedApp`. **If it says `RecoveryActivity`:** turn Google TV's Home back on at once (`pm enable com.google.android.apps.tv.launcherx`) and **do not restart the box** (a Xiaomi TV wiped itself after a restart on that screen) | Claude |
| 6.9 | Restart the box: MZ Box S Home comes up by itself, Wi-Fi reconnects | Box |
| 6.10 | **Casting**: cast YouTube from your phone, then again after a restart. This model's testers say it works; on Chromecast with Google TV it broke ("Untrusted device") without the setup wizard. If it fails, tell Claude | Phone, Box |

## Stage 7: Live with it (a few days)

| Check | Needs |
|---|---|
| Home key, Back, remote shortcut buttons, voice search (Assistant) | Box |
| Wi-Fi after a restart and after a power cut | Box |
| Prime Video coming back on by itself after a restart (it is hidden on Home anyway) | Box |
| Speed and memory; anything slow or odd: note it for Claude | Box |
| Then turn **USB debugging off** (Settings > System > Developer options) so port 5555 closes | Box |

## Way back

| What | Command | Needs |
|---|---|---|
| One app back | `adb shell pm enable <package>` | Claude |
| Everything in the list back, from the undo record | `powershell -ExecutionPolicy Bypass -File tools\restore.ps1 -TvHost <box-ip> debloat\debloat.txt -Stock com.google.android.apps.tv.launcherx` | Claude |
| Google TV's Home back | `pm enable com.google.android.apps.tv.launcherx` and `pm enable com.google.android.tungsten.setupwraith`, then press Home | Claude |
| **Before a factory reset** | Put everything back first (restore above), so the setup wizard can run | Claude |

## After a firmware update

An update can turn listed apps back on and give Home back to Google TV. Turn USB debugging on, then Claude
compares the box with the list: `$env:TV_HOST='<box-ip>'; tools\venv\Scripts\python.exe -I tools\check.py
debloat\debloat.txt`; `--fix` after the list puts it back. Then Stage 6 again if Home moved.
