# [AppMetrica SDK](https://appmetrica.io)

AppMetrica is a free real-time ad tracking and mobile app analytics solution. AppMetrica covers the three key features for discovering your app's performance — ad tracking, usage analytics and crash analytics.
Detailed information and instructions for integration are available in the [documentation](https://appmetrica.io/docs/).

Classes in packages `${MODULE_NAMESPACE}.internal` are intended for interaction between modules and are not part of the public API. Also changing these classes does not correspond to [semver](https://semver.org/).

## Builds

### Assemble

`./gradlew :assembleRelease`

### Publish to MavenLocal

`./gradlew :publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :testReleaseUnitTest :generateReleaseJacocoReport`

### Code style

`./gradlew lint ktlint checkstyle`

### Check AAR API

`./gradlew :aarCheck`

### Regenerate AAR API dump

`./gradlew :aarDump`

## Modules

### Optional modules

The modules described below are optional and can be forcibly disabled from the AppMetrica SDK if necessary.
To do this, add the following code to the `app/build.gradle.kts` file:
```kotlin
configurations.configureEach {
    exclude(group = "io.appmetrica.analytics", module = "analytics-{module_name}")
}
```

- **ad-revenue** - includes all Ad-Revenue modules of AppMetrica SDK.
- **ad-revenue-admob-v23** - adds a handler for Ad-Revenue events from `com.google.android.gms:play-services-ads`.
- **ad-revenue-applovin-v12** - adds a handler for Ad-Revenue events from `com.applovin:applovin-sdk`.
- **ad-revenue-fyber-v3** - adds a handler for Ad-Revenue events from `com.fyber:fairbid-sdk`.
- **ad-revenue-ironsource-v7** - allows AppMetrica SDK to collect Ad-Revenue events from `com.ironsource.sdk:mediationsdk`.
- **apphud** - adds integration with `com.apphud:ApphudSDK-Android`.
- **appsetid** - allows AppMetrica SDK to collect App Set IDs.
- **identifiers** - allows AppMetrica SDK to collect ADV IDs.
- **location** - allows AppMetrica SDK to collect location.
- **ndkcrashes** - allows AppMetrica SDK to handle native crashes on Android.
- **screenshot** - allows AppMetrica SDK to collect screenshot taken events.

### Modules with optional dependencies

The modules described below are not optional, but they do require external dependencies to function.
You can find the necessary dependencies in the modules' README files.

- **billing-v6** - wrapper for `com.android.billingclient:billing`.
- **gpllibrary** - wrapper for `com.google.android.gms:play-services-location`.

### Module dependencies

You can find list of module dependencies with supported versions in the [dependencies_versions.yaml](dependencies_versions.yaml) file.
