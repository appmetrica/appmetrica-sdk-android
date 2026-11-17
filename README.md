# [AppMetrica SDK](https://appmetrica.io)

AppMetrica is a free real-time ad tracking and mobile app analytics solution. AppMetrica covers the three key features for discovering your app's performance — ad tracking, usage analytics and crash analytics.
Detailed information and instructions for integration are available in the [documentation](https://appmetrica.io/docs/).

Classes in packages `${MODULE_NAMESPACE}.internal` are intended for interaction between modules and are not part of the public API. Also changing these classes does not correspond to [semver](https://semver.org/).

## Requirements

- JDK 17

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

Below is the list of modules that can be disabled when integrating AppMetrica SDK.
Modules that are not listed here must not be disabled.

To disable a module, put its Maven artifact id into `module` below:
```kotlin
configurations.configureEach {
    exclude(group = "io.appmetrica.analytics", module = "analytics-{module_name}")
}
```

Modules marked *not pulled by default* are not transitive dependencies of `analytics`.
Add them explicitly if needed:
```kotlin
implementation("io.appmetrica.analytics:analytics-{module_name}:${VERSION}")
```

- **ad-revenue** - includes all Ad-Revenue modules of AppMetrica SDK.
- **ad-revenue-admob-v23** - adds a handler for Ad-Revenue events from `com.google.android.gms:play-services-ads`.
- **ad-revenue-applovin-v12** - adds a handler for Ad-Revenue events from `com.applovin:applovin-sdk`.
- **ad-revenue-applovin-v12-auto** - adds auto-collection of Ad-Revenue events from `com.applovin:applovin-sdk`.
- **ad-revenue-fyber-v3** - adds a handler for Ad-Revenue events from `com.fyber:fairbid-sdk`.
- **ad-revenue-ironsource-v7** - allows AppMetrica SDK to collect Ad-Revenue events from `com.ironsource.sdk:mediationsdk` (7.1.0–8.12.0) and `com.unity3d.ads-mediation:mediation-sdk` (7.9.0–8.12.0).
- **ad-revenue-ironsource-v9** - allows AppMetrica SDK to collect Ad-Revenue events from `com.unity3d.ads-mediation:mediation-sdk` (9.0.0+).
- **ad-revenue-other** - allows AppMetrica SDK to collect Ad-Revenue events from other libraries and make experiments.
- **apphud-v2** - adds integration with `com.apphud:ApphudSDK-Android` (2.6.0–2.9.7). *Not pulled by default.*
- **apphud-v3** - adds integration with `com.apphud:ApphudSDK-Android` (3.0.0+). *Not pulled by default.*
- **billing** - aggregates the functionality of the in-app purchase collection.
- **certificates** - optional module providing an additional TLS trust path for SDK network requests. Does not perform Certificate Transparency checks.
- **identifiers** - allows AppMetrica SDK to collect ADV IDs.
- **location** - allows AppMetrica SDK to collect location.
- **ndkcrashes** (`analytics-ndk-crashes`) - allows AppMetrica SDK to handle native crashes on Android. *Not pulled by default.*
- **product-flow** - allows AppMetrica SDK to collect Product Flow events.
- **remote-permissions** - remote permissions functionality.
- **reporter-extension** - reporter extension module.
- **screenshot** - allows AppMetrica SDK to collect screenshot taken events.

### External dependencies used when present

AppMetrica SDK does not pull the libraries below as transitive dependencies.
If the host app adds them, the corresponding AppMetrica modules can use them
(via reflection / optional adapters). Without them the SDK keeps working;
related features are simply unavailable.

- `com.android.billingclient:billing` - Google Play Billing (in-app auto-collection via `billing` / `billing-v8`)
- `com.google.android.gms:play-services-location` - fused location (`location` / `gpllibrary`)
- `com.google.android.gms:play-services-ads` - AdMob Ad Revenue (`ad-revenue-admob-v23`)
- `com.applovin:applovin-sdk` - AppLovin Ad Revenue (`ad-revenue-applovin-v12`, `ad-revenue-applovin-v12-auto`)
- `com.fyber:fairbid-sdk` - Fyber Ad Revenue (`ad-revenue-fyber-v3`)
- `com.unity3d.ads-mediation:mediation-sdk` - IronSource Ad Revenue (`ad-revenue-ironsource-v7`, `ad-revenue-ironsource-v9`)
- `com.facebook.android:audience-network-sdk` - Facebook / other Ad Revenue (`ad-revenue-other`)

Supported versions are listed in [dependencies_versions.yaml](dependencies_versions.yaml).
