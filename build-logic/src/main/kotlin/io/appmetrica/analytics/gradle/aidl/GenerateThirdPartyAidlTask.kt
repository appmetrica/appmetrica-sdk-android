package io.appmetrica.analytics.gradle.aidl

import com.android.build.api.variant.Aidl
import com.android.build.api.variant.AndroidComponents
import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.the
import java.io.File
import javax.inject.Inject
import kotlin.io.path.createTempDirectory

abstract class GenerateThirdPartyAidlTask @Inject constructor(
    objectFactory: ObjectFactory,
) : DefaultTask() {

    @get:InputFiles
    val source: ConfigurableFileCollection = objectFactory.fileCollection()
        .from(project.layout.projectDirectory.dir("aidl"))

    @get:InputDirectory
    val outputDir: DirectoryProperty = objectFactory.directoryProperty()
        .convention(project.layout.projectDirectory.dir("src/main/java"))

    init {
        group = "aidl"
        description = "Generates Java from third-party AIDL sources in aidl/ and places them into the main source tree."
    }

    @TaskAction
    @Suppress("UnstableApiUsage")
    fun generate() {
        val aidlGenerator = AidlGenerator(project.the<AndroidComponents>().sdkComponents.aidl.get())
        val sourceDirs = source.asFileTree.filter { it.name == "config.yaml" }.map { it.parentFile }
        for (sourceDir in sourceDirs) {
            logger.lifecycle("Generating AIDL from ${sourceDir.relativeTo(project.projectDir)}")
            aidlGenerator.generate(sourceDir, outputDir.get().asFile)
        }
    }

    @Suppress("UnstableApiUsage")
    private inner class AidlGenerator(aidl: Aidl) {
        private val aidlExe = aidl.executable.get().asFile
        private val framework = aidl.framework.get().asFile

        @Suppress("NewApi")
        fun generate(sourceDir: File, outputDir: File): Collection<File> {
            val config = ThirdPartyAidlConfig.read(sourceDir.resolve("config.yaml"))
            val tempSrcDir = createTempDirectory().toFile()
            val tempOutDir = createTempDirectory().toFile()
            tempOutDir.mkdirs()

            for (aidlFile in sourceDir.getFilesByExtensionRecursively("aidl")) {
                val packageName = aidlFile.getPackage()
                aidlFile.copyTo(tempSrcDir.resolve(packageName.replace('.', '/')).resolve(aidlFile.name))
            }

            val cmd = listOf(
                aidlExe.absolutePath,
                "--lang=java",
                "-p${framework.absolutePath}",
                "-I${tempSrcDir.absolutePath}",
                "-o${tempOutDir.absolutePath}",
            ) + tempSrcDir.getFilesByExtensionRecursively("aidl").map { it.absolutePath }

            logger.lifecycle("Running: ${cmd.joinToString(" ")}")
            val process = ProcessBuilder(cmd).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            check(exitCode == 0) { "aidl failed (exit $exitCode):\n$output" }

            return tempOutDir.getFilesByExtensionRecursively("java").map { generatedFile ->
                val curPackage = generatedFile.getPackage()
                val realPackage = config.targetPackage + curPackage.removePrefix(config.originalPackage)
                val destFile = outputDir.resolve(realPackage.replace('.', '/')).resolve(generatedFile.name)
                generatedFile.copyTo(destFile, overwrite = true)
                postProcess(destFile, config, sourceDir)
                logger.lifecycle("Generated: ${destFile.relativeTo(project.projectDir)}")
                destFile
            }
        }

        private fun postProcess(generatedFile: File, config: ThirdPartyAidlConfig, sourceDir: File) {
            var result = generatedFile.readText()

            // Strip the aidl compiler's own header.
            result = result.replace(Regex("^/\\*.*?\\*/\\n", RegexOption.DOT_MATCHES_ALL), "")

            // Rewrite the Java package declaration (but NOT the DESCRIPTOR string literal).
            result = result.replace("package ${config.originalPackage};", "package ${config.targetPackage};")

            // Rewrite fully-qualified type references outside string literals.
            // Negative lookbehind on '"' preserves the DESCRIPTOR value.
            result = result.replace(
                Regex("(?<!\")(${Regex.escape(config.originalPackage)})(?=\\.)"),
                config.targetPackage,
            )

            if (!result.endsWith("\n")) result += "\n"

            val header = buildString {
                appendLine("// @formatter:off")
                appendLine("//CHECKSTYLE:OFF")
                appendLine("/*")
                appendLine(" * This file is generated from third-party AIDL sources.")
                appendLine(" * DO NOT EDIT MANUALLY.")
                appendLine(" *")
                appendLine(" * Source AIDL dir: ${sourceDir.relativeTo(project.projectDir)}")
                appendLine(" * Original package (used as Binder DESCRIPTOR): ${config.originalPackage}")
                appendLine(" *")
                appendLine(" * To regenerate, run:")
                appendLine(" *   ./gradlew ${project.path}:generateThirdPartyAidl")
                appendLine(" */")
            }
            val footer = buildString {
                appendLine("//CHECKSTYLE:ON")
            }

            generatedFile.writeText(header + result + footer)
        }

        private fun File.getPackage(): String {
            return useLines { lines -> lines.first { it.startsWith("package") }.split(" ")[1].removeSuffix(";") }
        }

        private fun File.getFilesByExtensionRecursively(extension: String): List<File> {
            return walkTopDown().filter { it.extension == extension }.toList()
        }
    }
}
