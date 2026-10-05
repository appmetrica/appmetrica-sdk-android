package io.appmetrica.analytics.gradle

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.DefaultConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AppMetricaModulePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.apply<AppMetricaCommonModulePlugin>()

        project.configure<LibraryExtension> {
            compileSdkVersion(Constants.Android.sdkVersion)
            buildToolsVersion(Constants.Android.buildToolsVersion)

            defaultConfig {
                this as DefaultConfig
                minSdkVersion(Constants.Android.minSdkVersion)
                targetSdkVersion(Constants.Android.sdkVersion)
                versionName = Constants.Library.versionName
                versionCode = Constants.Library.versionCode
            }
        }
    }
}
