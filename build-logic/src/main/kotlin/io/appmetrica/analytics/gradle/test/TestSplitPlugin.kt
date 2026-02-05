package io.appmetrica.analytics.gradle.test

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

/**
 * Gradle plugin that automatically splits unit tests into two groups: Robolectric tests and standard JUnit tests.
 *
 * This plugin is designed for modules with a large number of tests that include both Robolectric and standard
 * JUnit tests. For modules with only one type of tests or a small test suite, the overhead of splitting may
 * not provide significant benefits.
 *
 * This plugin creates its own independent testSplit extension for configuration. The testSettings extension
 * handles general test settings like maxParallelForks.
 *
 * ## Purpose
 *
 * Robolectric tests require different JVM settings than standard JUnit tests:
 * - More memory: Robolectric shadows consume significant heap space
 * - Frequent JVM forks: Prevents memory leaks from accumulating
 * - G1GC: Better garbage collection for large heap sizes
 *
 * Running all tests together with the same settings leads to:
 * - Memory issues and OOM errors
 * - Slower execution due to conservative settings
 * - Unpredictable test failures
 *
 * This plugin solves these problems by:
 * 1. Automatically detecting Robolectric tests using ASM bytecode analysis
 * 2. Creating separate test tasks with optimized JVM settings for each group
 * 3. Merging results transparently - IDE and CI see unified test reports
 * 4. Combining JaCoCo coverage data from both groups
 *
 * ## How It Works
 *
 * The plugin uses ASM (Java bytecode manipulation library) to scan compiled test classes and detect
 * Robolectric tests by looking for @RunWith(RobolectricTestRunner::class) or
 * @RunWith(ParameterizedRobolectricTestRunner::class) annotations.
 *
 * For each unit test task (e.g., testProdReleaseUnitTest), the plugin creates:
 * - testProdReleaseUnitTestRobolectric - runs only Robolectric tests
 * - testProdReleaseUnitTestStandard - runs only standard JUnit tests
 * - testProdReleaseUnitTestReport - generates aggregated HTML report
 * - testProdReleaseUnitTestMerge - merges XML results and validates success
 *
 * The original test task is disabled and depends on the merge task, making the split transparent.
 *
 * ## Default Settings
 *
 * When split is enabled, tests run with these settings:
 *
 * Robolectric tests:
 * - forkEvery = 10 (restart JVM every 10 test methods)
 * - -Xmx6g (6GB heap memory)
 * - -XX:+UseG1GC -XX:MaxGCPauseMillis=100 (G1 garbage collector)
 * - JaCoCo: separate execution file (testXXXRobolectric.exec)
 *
 * Standard tests:
 * - forkEvery = 1000 (restart JVM every 1000 test methods)
 * - -Xmx4g (4GB heap memory from base config)
 * - Default GC
 * - JaCoCo: separate execution file (testXXXStandard.exec)
 *
 * ## Configuration
 *
 * The plugin is disabled by default. Enable it per-module in build.gradle.kts:
 *
 * Example - Enable with defaults:
 *   testSettings {
 *       maxParallelForks.set(8)  // General test setting
 *   }
 *   testSplit {
 *       enabled.set(true)  // Enable test splitting
 *   }
 *
 * Example - Enable with custom settings:
 *   testSplit {
 *       enabled.set(true)
 *       robolectricForkEvery.set(5)
 *       robolectricMemory.set("8g")
 *       standardForkEvery.set(2000)
 *   }
 *
 * Example - Disable (default):
 *   testSplit {
 *       enabled.set(false)
 *   }
 */

class TestSplitPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val splitExtension = project.extensions.create("testSplit", TestSplitExtension::class.java)

        project.afterEvaluate {
            if (!splitExtension.enabled.get()) return@afterEvaluate

            project.tasks.withType<Test>()
                .filter { it.name.contains("UnitTest") }
                .forEach { originalTest ->
                    createSplitTasks(project, originalTest, splitExtension)
                }
        }
    }

    private fun createSplitTasks(
        project: Project,
        originalTest: Test,
        extension: TestSplitExtension
    ) {
        val originalTestName = originalTest.name

        val userTestFilters = detectUserTestFilters(project)

        val robolectricTask = createRobolectricTask(project, originalTest, extension)
        val standardTask = createStandardJUnitTask(project, originalTest, extension, robolectricTask)
        val reportTask = createReportTask(project, originalTest, robolectricTask, standardTask)
        val mergeTask = createMergeTask(project, originalTest, robolectricTask, standardTask, reportTask)

        originalTest.dependsOn(mergeTask)

        configureTestFilters(robolectricTask, userTestFilters, includeRobolectric = true)
        configureTestFilters(standardTask, userTestFilters, includeRobolectric = false)


        // Disable original test - all work done by split tasks
        originalTest.enabled = false

        configureJacocoReports(project, originalTestName, mergeTask)
    }

    private fun detectUserTestFilters(project: Project): List<String> {
        val filters = mutableListOf<String>()

        // Check for -Dtest.single system property
        project.gradle.startParameter.systemPropertiesArgs["test.single"]?.let {
            filters.add(it)
        }

        project.gradle.startParameter.taskRequests.forEach { taskRequest ->
            taskRequest.args.forEachIndexed { index, arg ->
                if (arg == "--tests" && index + 1 < taskRequest.args.size) {
                    filters.add(taskRequest.args[index + 1])
                }
            }
        }

        return filters
    }

    private fun configureTestFilters(
        testTask: TaskProvider<Test>,
        userFilters: List<String>,
        includeRobolectric: Boolean
    ) {
        testTask.configure {
            doFirst {
                val robolectricTests = RobolectricTestDetector().run {
                    testClassesDirs.files.flatMap { findRobolectricTests(it) }.toSet()
                }

                filter {
                    if (includeRobolectric) {
                        if (robolectricTests.isEmpty()) {
                            excludeTestsMatching("*")
                            return@filter
                        }
                        if (userFilters.isEmpty()) {
                            robolectricTests.forEach { includeTestsMatching(it) }
                        } else {
                            userFilters.forEach { includeTestsMatching(it) }
                            val allTests = getAllTestClasses()
                            (allTests - robolectricTests).forEach { excludeTestsMatching(it) }
                        }
                    } else {
                        userFilters.forEach { includeTestsMatching(it) }
                        robolectricTests.forEach { excludeTestsMatching(it) }
                    }
                }
            }
        }
    }

    private fun Test.getAllTestClasses(): Set<String> {
        val allTests = mutableSetOf<String>()
        testClassesDirs.files.forEach { dir ->
            if (dir.exists()) {
                dir.walk()
                    .filter { it.isFile && it.name.endsWith(".class") && !it.name.contains("$") }
                    .forEach { classFile ->
                        val relativePath = classFile.relativeTo(dir).path
                        val className = relativePath.removeSuffix(".class").replace("/", ".")
                        allTests.add(className)
                    }
            }
        }
        return allTests
    }

    private fun createRobolectricTask(
        project: Project,
        originalTest: Test,
        extension: TestSplitExtension
    ) = project.tasks.register("${originalTest.name}Robolectric", Test::class.java) {
        description = "Runs Robolectric tests from ${originalTest.name}"
        group = "verification"

        originalTest.dependsOn.forEach { dep ->
            dependsOn(dep)
        }

        classpath = originalTest.classpath
        testClassesDirs = originalTest.testClassesDirs
        forkEvery = extension.robolectricForkEvery.get().toLong()

        // Configure base JVM args (includes -Xmx4g)
        TestJvmArgsConfigurator.configureBaseJvmArgs(this)

        // Override memory setting for Robolectric (replace -Xmx4g with -Xmx6g)
        jvmArgs("-Xmx${extension.robolectricMemory.get()}")

        // GC settings for better performance with Robolectric
        jvmArgs("-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100")

        extensions.configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
            setDestinationFile(
                project.layout.buildDirectory
                    .file("jacoco/${originalTest.name}Robolectric.exec").get().asFile
            )
        }

        // Temporary results directory (only XML, HTML will be aggregated later)
        reports.junitXml.outputLocation.set(
            project.layout.buildDirectory.dir("test-results-robolectric/${originalTest.name}")
        )
        reports.html.required.set(false)

        // Don't fail build on test failures - let merge task handle it
        ignoreFailures = true

        filter.isFailOnNoMatchingTests = false
        outputs.upToDateWhen { false }
    }

    private fun createStandardJUnitTask(
        project: Project,
        originalTest: Test,
        extension: TestSplitExtension,
        robolectricTask: TaskProvider<Test>
    ) = project.tasks.register("${originalTest.name}Standard", Test::class.java) {
        description = "Runs standard (non-Robolectric) tests from ${originalTest.name}"
        group = "verification"

        // Run after Robolectric task to avoid JaCoCo agent race condition
        mustRunAfter(robolectricTask)

        // Depend on test compilation (same as original test)
        originalTest.dependsOn.forEach { dep ->
            dependsOn(dep)
        }

        classpath = originalTest.classpath
        testClassesDirs = originalTest.testClassesDirs
        forkEvery = extension.standardForkEvery.get().toLong()

        // Configure base JVM args (includes -Xmx4g)
        TestJvmArgsConfigurator.configureBaseJvmArgs(this)

        extensions.configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
            setDestinationFile(
                project.layout.buildDirectory
                    .file("jacoco/${originalTest.name}Standard.exec").get().asFile
            )
        }

        // Temporary results directory (only XML, HTML will be aggregated later)
        reports.junitXml.outputLocation.set(
            project.layout.buildDirectory.dir("test-results-standard/${originalTest.name}")
        )
        reports.html.required.set(false) // Disable HTML report for split task

        // Don't fail build on test failures - let merge task handle it
        ignoreFailures = true

        filter.isFailOnNoMatchingTests = false
        outputs.upToDateWhen { false }
    }

    private fun createReportTask(
        project: Project,
        originalTest: Test,
        robolectricTask: TaskProvider<Test>,
        standardTask: TaskProvider<Test>
    ) = project.tasks.register("${originalTest.name}Report", TestReport::class.java) {
        description = "Generates aggregated HTML report for ${originalTest.name}"
        group = "verification"

        destinationDirectory.set(originalTest.reports.html.outputLocation)
        reportOn(robolectricTask, standardTask)
    }

    private fun createMergeTask(
        project: Project,
        originalTest: Test,
        robolectricTask: TaskProvider<Test>,
        standardTask: TaskProvider<Test>,
        reportTask: TaskProvider<TestReport>
    ) = project.tasks.register("${originalTest.name}Merge") {
        description = "Merges test results from Robolectric and standard tests"
        group = "verification"

        dependsOn(robolectricTask, standardTask, reportTask)

        doLast {
            val originalResultsDir = originalTest.reports.junitXml.outputLocation.get().asFile

            originalResultsDir.deleteRecursively()
            originalResultsDir.mkdirs()

            val robolectricResultsDir = robolectricTask.get().reports.junitXml.outputLocation.get().asFile
            val standardResultsDir = standardTask.get().reports.junitXml.outputLocation.get().asFile

            var robolectricFiles = 0
            var robolectricTestMethods = 0
            var standardFiles = 0
            var standardTestMethods = 0

            if (robolectricResultsDir.exists()) {
                robolectricResultsDir.listFiles()?.forEach { file ->
                    file.copyTo(originalResultsDir.resolve(file.name), overwrite = true)
                    robolectricFiles++
                    robolectricTestMethods += countTestsInXml(file)
                }
            }

            if (standardResultsDir.exists()) {
                standardResultsDir.listFiles()?.forEach { file ->
                    file.copyTo(originalResultsDir.resolve(file.name), overwrite = true)
                    standardFiles++
                    standardTestMethods += countTestsInXml(file)
                }
            }

            logger.lifecycle("")
            logger.lifecycle("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            logger.lifecycle("  Test Split Summary for ${originalTest.name}")
            logger.lifecycle("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            logger.lifecycle("  Robolectric tests: $robolectricFiles classes, $robolectricTestMethods methods")
            logger.lifecycle("  Standard tests:    $standardFiles classes, $standardTestMethods methods")
            logger.lifecycle(
                "  Total:             ${robolectricFiles + standardFiles} classes, " +
                    "${robolectricTestMethods + standardTestMethods} methods"
            )
            logger.lifecycle("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            logger.lifecycle("")

            val totalTestMethods = robolectricTestMethods + standardTestMethods
            if (totalTestMethods == 0) {
                throw org.gradle.api.GradleException(
                    "No tests found matching the filter. Both Robolectric and Standard test tasks found 0 tests."
                )
            }

            val robolectricFailed = robolectricTask.get().state.failure != null
            val standardFailed = standardTask.get().state.failure != null

            if (robolectricFailed || standardFailed) {
                throw org.gradle.api.GradleException(
                    "Tests failed. Robolectric: $robolectricFailed, Standard: $standardFailed"
                )
            }
        }
    }

    private fun configureJacocoReports(
        project: Project,
        originalTestName: String,
        mergeTask: TaskProvider<*>
    ) {
        project.afterEvaluate {
            val variantName = originalTestName.removePrefix("test").removeSuffix("UnitTest")
            project.tasks.withType<org.gradle.testing.jacoco.tasks.JacocoReport>().configureEach {
                val reportTaskName = name
                if (reportTaskName.equals("generate${variantName}JacocoReport", ignoreCase = true)) {
                    executionData.setFrom(
                        project.layout.buildDirectory.file("jacoco/${originalTestName}Robolectric.exec").get().asFile,
                        project.layout.buildDirectory.file("jacoco/${originalTestName}Standard.exec").get().asFile
                    )

                    dependsOn(mergeTask)
                }
            }
        }
    }

    private fun countTestsInXml(xmlFile: java.io.File): Int {
        return try {
            val text = xmlFile.readText()
            val testsPattern = """<testsuite[^>]*\s+tests="(\d+)"""".toRegex()
            val match = testsPattern.find(text)
            match?.groupValues?.get(1)?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
