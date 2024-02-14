plugins {
    id("appmetrica-module")
}

publishingInfo {
    baseArtifactId.set("analytics-logger")
    name.set("AppMetrica SDK Logger")
}

android {
    namespace = "io.appmetrica.analytics.logger"
    lint {
        disable += "GradleDependency"
    }

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
