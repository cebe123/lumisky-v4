# Google Play Release

Last checked: 2026-04-25

## Build Requirements

- Google Play requires new apps and updates to target Android 15 / API 35 or higher. Lumisky targets API 36.
- New Google Play apps are published as Android App Bundles (`.aab`).
- Release uploads must be signed with an upload key. Keep `keystore.properties` and keystore files local.

Official references:

- Target API level: https://developer.android.com/google/play/requirements/target-sdk
- Android App Bundles: https://developer.android.com/guide/app-bundle
- Package visibility / `QUERY_ALL_PACKAGES`: https://support.google.com/googleplay/android-developer/answer/10158779
- Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
- User data and privacy policy: https://support.google.com/googleplay/android-developer/answer/10144311

## Release Identity

`lumisky.applicationId` is set in `gradle.properties`:

```properties
lumisky.applicationId=com.lumisky.wallpaper
lumisky.versionCode=1
lumisky.versionName=1.0.0
```

The application ID is permanent after the first Play upload. Change it before the first upload if `com.lumisky.wallpaper` is not the final package name.

Increment `lumisky.versionCode` for every Play upload. `lumisky.versionName` is user-facing.

## Signing

Create an upload key locally:

```powershell
keytool -genkeypair -v -keystore lumisky-upload.jks -alias lumisky-upload -keyalg RSA -keysize 4096 -validity 10000
```

Then copy `keystore.properties.template` to `keystore.properties` and fill:

```properties
storeFile=lumisky-upload.jks
storePassword=...
keyAlias=lumisky-upload
keyPassword=...
```

You can also provide the same values through Gradle properties:

```powershell
.\gradlew :app:bundleRelease `
  -Plumisky.release.storeFile=lumisky-upload.jks `
  -Plumisky.release.storePassword=... `
  -Plumisky.release.keyAlias=lumisky-upload `
  -Plumisky.release.keyPassword=...
```

Or with environment variables:

```powershell
$env:LUMISKY_RELEASE_STORE_FILE="lumisky-upload.jks"
$env:LUMISKY_RELEASE_STORE_PASSWORD="..."
$env:LUMISKY_RELEASE_KEY_ALIAS="lumisky-upload"
$env:LUMISKY_RELEASE_KEY_PASSWORD="..."
```

## Commands

Local validation:

```powershell
.\gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

Build the Play artifact:

```powershell
.\gradlew :app:bundleRelease
```

The upload file is:

```text
app/build/outputs/bundle/release/app-release.aab
```

## Play Console Checklist

- Use `app-release.aab` for the release track.
- Complete Data safety. The app requests approximate and precise location and uses network access for daylight/location-related behavior.
- Add a real privacy policy URL in Play Console and inside the app/store materials before submission.
- Confirm content rating, target audience, ads status, and app category.
- Keep `QUERY_ALL_PACKAGES` out of the manifest unless the app has a policy-approved core use case.
