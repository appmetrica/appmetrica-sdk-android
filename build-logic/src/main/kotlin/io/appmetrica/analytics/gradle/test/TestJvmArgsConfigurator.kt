package io.appmetrica.analytics.gradle.test

import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.kotlin.dsl.KotlinClosure2
import org.gradle.kotlin.dsl.closureOf

object TestJvmArgsConfigurator {

    fun configureBaseJvmArgs(test: Test) {
        // about arguments https://docs.oracle.com/javase/9/tools/java.htm
        test.jvmArgs("-noverify", "-Xmx4g", "-XX:MaxMetaspaceSize=512m")
        // https://nda.ya.ru/t/81uL3Dxs6Njj8v
        test.jvmArgs("-Djdk.attach.allowAttachSelf=true")
        // need for fix https://nda.ya.ru/t/PGGDmRNa6Njj8w
        test.jvmArgs("-XX:CompileCommand=exclude,android/database/sqlite/SQLiteSession*.*")
        test.systemProperty("robolectric.logging.enabled", "true")

        test.beforeTest(
            closureOf<TestDescriptor> {
                test.logger.lifecycle("< $this started.")
            }
        )
        test.afterTest(
            KotlinClosure2<TestDescriptor, TestResult, Unit>({ descriptor, result ->
                val duration = result.endTime - result.startTime
                test.logger.lifecycle("< $descriptor finished in ${duration}ms.")
            })
        )
    }
}
