package io.appmetrica.analytics.gradle

import org.gradle.api.Project

private const val DEFAULT_BUILD_NUMBER = 65535
private val BUILD_NUMBER = System.getenv("BUILD_NUMBER") ?: DEFAULT_BUILD_NUMBER.toString()

object Constants {
    const val robolectricSdk = 34 // after change run task `updateRobolectricSdk`

    object Android {
        const val buildToolsVersion = "35.0.0"
        const val sdkVersion = 35
        const val minSdkVersion = 21
        const val minSdkVersionNative = 21
    }

    object Library {
        const val versionName = "7.14.1"
        val versionCode = BUILD_NUMBER.toInt()
        val buildNumber = BUILD_NUMBER
        //Also need to update api level at https://nda.ya.ru/t/rFFwGoT66NsxJm and https://nda.ya.ru/t/4_qxHC-N6NsxL4
        const val libraryApiLevel = 116
        const val group = "io.appmetrica.analytics"
    }

    object NdkCrashes {
        const val versionName = "3.2.0"
        const val versionCode = 7
    }
}

object Deps {
    const val androidX = "1.0.0"
    const val appSetIdVersion = "16.1.0"
    const val gmsLocationVersion = "21.0.1"
    const val referrerVersion = "2.2"
}

object Hosts {
    const val defaultStartupHost = "https://startup.mobile.yandex.net/"
}

val Project.isCIBuild: Boolean
    get() = project.hasProperty("sandbox")
