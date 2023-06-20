# AppMetrica NDK Crashes

**Optional module**.

This library adds ability to “AppMetrica SDK” to handle native crashes on Android.
It is based on [Crashpad](https://chromium.googlesource.com/crashpad/crashpad/+/refs/heads/main) library and redistribute it in binary form.
All changes in Crashpad code are indicated by the notice “//change for AppMetrica”.
You can find Crashpad license [here](https://chromium.googlesource.com/crashpad/crashpad/+/refs/heads/main/LICENSE)

Maven: `io.appmetrica.analytics:analytics-ndk-crashes:${VERSION}`.

## Prerequisites

- cmake version 3.10.2
- NDK version 21.1.6352462
- [Crashpad Prerequisites](https://chromium.googlesource.com/crashpad/crashpad/+/HEAD/doc/developing.md#prerequisites)

## Builds

### Assemble

`./gradlew :ndkcrashes:assembleRelease -Pndkcrashes.native.enabled=true`

### Publish to MavenLocal

`./gradlew :ndkcrashes:publishReleasePublicationToMavenLocal -Pndkcrashes.native.enabled=true`
