import com.android.build.gradle.internal.dsl.BuildType
import io.appmetrica.analytics.gradle.Constants
import io.appmetrica.analytics.gradle.Deps
import io.appmetrica.analytics.gradle.Hosts
import io.appmetrica.analytics.gradle.publishing.PublishingInfoFlavorExtension
import io.appmetrica.gradle.aarcheck.agp.aarCheck
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("appmetrica-module")
    id("appmetrica-proto")
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

    lint {
        // set to true to turn off analysis progress reporting by lint
        quiet = false
        // if true, stop the gradle build if errors are found
        abortOnError = true
        // if true, only report errors
        ignoreWarnings = false
        // if true, check all issues, including those that are off by default
        checkAllWarnings = false
        // if true, treat all warnings as errors
        warningsAsErrors = true
        ignoreTestSources = true
        // if true, generate an XML report for use by for example Jenkins
        xmlReport = true
        xmlOutput = file("${project.buildDir}/reports/lint/lint-results.xml")
        // if true, generate an HTML report (with issue explanations, sourcecode, etc)
        htmlReport = true
        htmlOutput = file("${project.buildDir}/reports/lint/lint.html")
        lintConfig = file("$projectDir/lint.xml")
        checkDependencies = false

        disable += "GradleDependency"
        disable += "LongLogTag"
    }
}

protobuf {
    packageName.set("io.appmetrica.analytics.impl.protobuf")
    protoFile(srcPath = "backend/adRevenue.proto", years = "2022")
    protoFile(srcPath = "backend/crashAndroid.proto", years = "2019")
    protoFile(srcPath = "backend/ecommerce.proto", years = "2020")
    protoFile(srcPath = "backend/eventProto.proto", years = "2012-2017")
    protoFile(srcPath = "backend/eventStart.proto", years = "2019")
    protoFile(srcPath = "backend/referrer.proto", years = "2018")
    protoFile(srcPath = "backend/revenue.proto", years = "2018")
    protoFile(srcPath = "backend/externalAttribution.proto", years = "2023")
    protoFile(srcPath = "backend/userprofile.proto", years = "2018")

    protoFile(srcPath = "client/appPermissionsStateProtobuf.proto", years = "2019")
    protoFile(srcPath = "client/clidsInfoProto.proto", years = "2022")
    protoFile(srcPath = "client/dbProto.proto", years = "2023")
    protoFile(srcPath = "client/eventExtrasProto.proto", years = "2022")
    protoFile(srcPath = "client/eventhashes.proto", years = "2018")
    protoFile(srcPath = "client/preloadInfoProto.proto", years = "2020")
    protoFile(srcPath = "client/referrerInfoClient.proto", years = "2018")
    protoFile(srcPath = "client/satelliteClidsInfoProto.proto", years = "2020")
    protoFile(srcPath = "client/sessionExtrasProtobuf.proto", years = "2023")
    protoFile(srcPath = "client/startupStateProtobuf.proto", years = "2018")
    protoFile(srcPath = "client/legacyStartupStateProtobuf.proto", years = "2023")
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
    api("com.android.installreferrer:installreferrer:${Deps.referrerVersion}")

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
    testImplementation("com.google.android.gms:play-services-location:${Deps.gmsLocationVersion}")
}
