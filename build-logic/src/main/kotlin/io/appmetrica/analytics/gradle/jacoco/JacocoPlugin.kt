package io.appmetrica.analytics.gradle.jacoco

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.LibraryVariant
import groovy.namespace.QName
import groovy.util.Node
import groovy.util.XmlParser
import io.appmetrica.analytics.gradle.teamcity.TeamCityExtension
import io.appmetrica.analytics.gradle.teamcity.TeamCityPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Locale

class JacocoPlugin : Plugin<Project> {

    companion object {
        const val JACOCO_TASK_GROUP = "jacoco"
    }

    override fun apply(project: Project) {
        project.apply<JacocoPlugin>() // id("jacoco")
        project.apply<TeamCityPlugin>() // id("appmetrica-teamcity")

        val extension = project.extensions.create<JacocoSettingsExtension>("jacocoSettings")

        val jacocoReportDir = project.layout.buildDirectory.dir("jacoco")

        project.configure<JacocoPluginExtension> {
            toolVersion = "0.8.7" // https://github.com/jacoco/jacoco/releases
            setReportsDir(jacocoReportDir.map { it.asFile })
        }

        project.plugins.withId("com.android.library") {
            project.the<LibraryExtension>().libraryVariants.configureEach {
                project.registerJacocoTaskForVariant(extension, this, jacocoReportDir)
            }
        }

        project.tasks.withType<Test> {
            reports {
                junitXml.isEnabled = true
            }
            configure<JacocoTaskExtension> {
                // without this coverage does not work
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
    }

    fun getValueFromCounter(parsedXmlReport: Node, type: String, attribute: String): Int {
        val counter = parsedXmlReport
            .getAt(QName("counter"))
            .find { counter -> (counter as Node).attribute("type") == type } as Node?
        val value = counter?.attribute(attribute)?.toString() ?: "0"

        return value.toInt()
    }

    fun Project.printInTeamcityAndReturnCoverage(parsedXmlReport: Node, type: String): String {
        val covered = getValueFromCounter(parsedXmlReport, type, "covered")
        val missed = getValueFromCounter(parsedXmlReport, type, "missed")
        val coverage = 100.0 * covered / (covered + missed)

        the<TeamCityExtension>().buildStatisticValue("CodeCoverageAbs${type[0]}Covered", covered)
        the<TeamCityExtension>().buildStatisticValue("CodeCoverageAbs${type[0]}Total", covered + missed)

        return String.format("%.1f", coverage).replace(",", ".")
    }

    fun Project.registerJacocoTaskForVariant(
        extension: JacocoSettingsExtension,
        variant: LibraryVariant,
        jacocoReportDir: Provider<Directory>
    ) {
        val jacocoVariantReportDir = jacocoReportDir.map { it.dir(variant.name) }

        val jacocoVariantTask = tasks.register<JacocoReport>("generate${variant.name.capitalize(Locale.ROOT)}JacocoReport") {
            val testTask = tasks.named<Test>("test${variant.name.capitalize(Locale.ROOT)}UnitTest").get()
            val kotlinTask = tasks.named<KotlinCompile>("compile${variant.name.capitalize(Locale.ROOT)}Kotlin").get()

            group = JACOCO_TASK_GROUP
            description = "Generate Jacoco coverage reports after running tests for ${variant.name}"

            // jacocoRootReport doesn"t work if some subprojects don"t have any tests at all
            // because this causes the onlyIf of JacocoReport to be false.
            onlyIf { true }

            reports {
                xml.required.set(true)
                xml.setDestination(jacocoVariantReportDir.map { it.file("${name}.xml").asFile })
                html.required.set(true)
                html.setDestination(jacocoVariantReportDir.map { it.dir("html").asFile })
            }

            classDirectories.from(fileTree(variant.javaCompileProvider.get().destinationDirectory.get()) {
                exclude(extension.exclude.get())
            })
            classDirectories.from(fileTree(kotlinTask.destinationDir) {
                exclude(extension.exclude.get())
            })

            sourceDirectories.from(variant.sourceSets.map { it.javaDirectories })
            executionData.from(testTask.the<JacocoTaskExtension>().destinationFile)

            dependsOn(testTask)
        }

        val printCoverageTask = tasks.register("print${variant.name.capitalize(Locale.ROOT)}CoverageInTeamcity") {
            group = JACOCO_TASK_GROUP
            description = "Prints coverage to teamcity for ${variant.name}"

            onlyIf { jacocoVariantTask.get().reports.xml.destination.exists() }

            doLast {
                val xmlParser = XmlParser()
                xmlParser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
                xmlParser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
                val parsedXmlReport = xmlParser.parse(jacocoVariantTask.get().reports.xml.destination)

                val classesCoverage = printInTeamcityAndReturnCoverage(parsedXmlReport, "CLASS")
                val methodsCoverage = printInTeamcityAndReturnCoverage(parsedXmlReport, "METHOD")
                val linesCoverage = printInTeamcityAndReturnCoverage(parsedXmlReport, "LINE")

                project.the<TeamCityExtension>().appendToBuildStatus("Coverage: ${classesCoverage}%/${methodsCoverage}%/${linesCoverage}%")
            }
        }

        jacocoVariantTask.configure { finalizedBy(printCoverageTask) }
    }
}
