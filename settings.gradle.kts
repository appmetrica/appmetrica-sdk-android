if (file("internal.settings.gradle.kts").exists()) {
    apply(from = "internal.settings.gradle.kts")
} else {
    apply(from = "public.settings.gradle.kts")
}

rootProject.name = "appmetrica-sdk"

// build scripts
includeBuild("build-logic") {
    name = "appmetrica-sdk-build-logic"
}

// modules
include("ad-revenue")
include("ad-revenue-admob-v23")
include("ad-revenue-applovin-v12")
include("ad-revenue-fyber-v3")
include("ad-revenue-ironsource-v7")
include("analytics")
include("apphud")
include("appsetid")
include("billing-interface")
include("billing-v6")
include("billing-v8")
include("common-logger")
include("core-api")
include("core-utils")
include("gpllibrary")
include("identifiers")
include("location")
include("location-api")
include("logger")
include("modules-api")
include("ndkcrashes-api")
include("network")
include("network-tasks")
include("proto")
include("remote-permissions")
include("reporter-extension")
include("screenshot")

// native crashes
include("ndkcrashes")

// tests modules
includeBuild("common_assertions")
include("test-utils")
