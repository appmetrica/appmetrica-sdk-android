plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-common-logger")
    name.set("AppMetrica SDK Common Logger")
}

android {
    namespace = "io.appmetrica.analytics.logger.common"

    sourceSets {
        getByName("debug") {
            java.srcDir("src/impl/java")
        }
        getByName("snapshot") {
            java.srcDir("src/impl/java")
        }
        getByName("testDebug") {
            java.srcDir("src/testImpl/java")
        }
        getByName("testSnapshot") {
            java.srcDir("src/testImpl/java")
        }
    }
}
