import com.android.build.gradle.internal.dsl.BuildType
import io.appmetrica.analytics.gradle.Constants
import io.appmetrica.analytics.gradle.Hosts
import io.appmetrica.analytics.gradle.publishing.PublishingInfoFlavorExtension
import io.appmetrica.gradle.aarcheck.agp.aarCheck
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("appmetrica-module")
    alias(appMetricaLibs.plugins.appMetricaProto)
}

publishingInfo {
    baseArtifactId.set("analytics")
    name.set("AppMetrica SDK")
}

android {
    namespace = "io.appmetrica.analytics"
    defaultConfig {
        // Metrica configuration
        buildConfigField("String[]", "DEFAULT_HOSTS", "{\"${Hosts.defaultStartupHost}\"}")
        buildConfigField("int", "API_LEVEL", "${Constants.Library.libraryApiLevel}")

        buildConfigField("String", "BUILD_DATE", "\"${SimpleDateFormat("dd.MM.yyyy").format(Date())}\"")
        buildConfigField("String", "BUILD_NUMBER", "\"${Constants.Library.buildNumber}\"")

        buildConfigField("String", "SDK_BUILD_FLAVOR", "\"public\"")
        buildConfigField("String", "SDK_BUILD_TYPE", "\"\"")

        buildConfigField("String", "SDK_DEPENDENCY", "\"\"")
        buildConfigField("boolean", "DEFAULT_LOCATION_COLLECTING", "false")
        buildConfigField("String", "SERVICE_COMPONENTS_INITIALIZER_CLASS_NAME", "\"\"")
        buildConfigField("String", "CLIENT_COMPONENTS_INITIALIZER_CLASS_NAME", "\"\"")
    }

    buildTypes {
        debug {
            this as BuildType
            isDebuggable = true
            isDefault = true

            buildConfigField("String", "VERSION_NAME", "\"${Constants.Library.versionName}\"")
            buildConfigField("boolean", "METRICA_DEBUG", "true")
            buildConfigField("String", "SDK_BUILD_TYPE", "\"\"")
        }
        release {
            this as BuildType
            isDebuggable = false

            buildConfigField("String", "VERSION_NAME", "\"${Constants.Library.versionName}\"")
            buildConfigField("boolean", "METRICA_DEBUG", "false")
            buildConfigField("String", "SDK_BUILD_TYPE", "\"\"")
        }
        named("snapshot") {
            this as BuildType
            isDebuggable = false

            buildConfigField("String", "VERSION_NAME", "\"${Constants.Library.versionName}-SNAPSHOT\"")
            buildConfigField("boolean", "METRICA_DEBUG", "true")
            buildConfigField("String", "SDK_BUILD_TYPE", "\"snapshot\"")
        }
    }

    flavorDimensionList.add("tier")
    productFlavors {
        create("prod") {
            dimension = "tier"
            isDefault = true

            buildConfigField("String", "SDK_DEPENDENCY", "\"source\"")
            aarCheck.enabled = true
        }
        create("perf") {
            dimension = "tier"

            buildConfigField("String", "SDK_DEPENDENCY", "\"source\"")

            configure<PublishingInfoFlavorExtension> {
                artifactIdSuffix.set("-perf")
            }
        }
        create("binaryProd") {
            dimension = "tier"

            buildConfigField("String", "SDK_DEPENDENCY", "\"binary\"")
        }
    }
}

jacocoSettings {
    exclude.addAll(
        "io/appmetrica/analytics/impl/protobuf/**",
    )
}

// Configure test execution (AppMetricaCommonModulePlugin)
testSettings {
    // Use more parallel forks due to huge amount of tests. Used in combination with large multislot.
    maxParallelForks.set(8)
}

// Enable test splitting for Robolectric and standard tests (TestSplitPlugin)
testSplit {
    enabled.set(true)
}

//
// +------------------------------------------------------------+
// | Dependencies                                               |
// +------------------------------------------------------------+
// ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||

dependencies {
    api(appMetricaLibs.installreferrer)

    implementation(project(":appsetid"))
    implementation(project(":core-api"))
    implementation(project(":core-utils"))
    implementation(project(":location-api"))
    implementation(project(":logger"))
    implementation(project(":modules-api"))
    implementation(project(":ndkcrashes-api"))
    implementation(project(":network-tasks"))

    runtimeOnly(project(":ad-revenue"))
    runtimeOnly(project(":billing"))
    runtimeOnly(project(":id-sync"))
    runtimeOnly(project(":identifiers"))
    runtimeOnly(project(":location"))
    runtimeOnly(project(":remote-permissions"))
    runtimeOnly(project(":reporter-extension"))
    runtimeOnly(project(":screenshot"))

    testImplementation(project(":identifiers"))
    testImplementation(appMetricaLibs.playServicesLocation)
}
