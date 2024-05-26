package io.appmetrica.analytics.gradle.protobuf

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import java.io.File
import java.util.Locale

class ProtobufPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create<ProtobufExtension>("protobuf")
        extension.protoPath.convention("protobuf")

        val outDir = project.layout.buildDirectory.dir("generated")

        project.tasks.register<Exec>("generateProtocol") {
            group = "protobuf"
            description = "Generate protobuf protocol"

            doFirst {
                outDir.get().asFile.mkdirs()
            }

            workingDir(project.layout.projectDirectory)
            commandLine(
                "protoc",
                *extension.protoConfigs.get().map {
                    "${extension.protoPath.get()}/${it.srcPath.get()}"
                }.toTypedArray(),
                "--proto_path=${extension.protoPath.get()}",
                "--javanano_out=${outDir.get().asFile.canonicalPath}"
            )

            doLast {
                val destDir = project.layout.projectDirectory.dir("src/main/java")
                    .dir(extension.packageName.get().replace(".", "/"))
                project.delete(destDir)

                extension.protoConfigs.get().forEach { config ->
                    val srcPath = File(config.srcPath.get())
                    val packageName = mergePackageName(extension.packageName.get(), (srcPath.parent
                        ?: "").replace("/", "."))
                    val javaFileName = "${srcPath.name.take(srcPath.name.lastIndexOf(".")).capitalize(Locale.ROOT)}.java"

                    val generatedFile = outDir.get().file("nano/${javaFileName}").asFile
                    val metricaFile = destDir.dir(srcPath.parent ?: ".").file(javaFileName).asFile

                    metricaFile.parentFile.mkdirs()
                    metricaFile.writeText(
                        generatedFile.readText()
                            .replace("com.google.protobuf.nano.", "io.appmetrica.analytics.protobuf.nano.")
                            .replace("package nano;", "package ${packageName};")
                            .replace(" nano.", " ")
                    )
                }
            }
        }

        project.dependencies {
            add("implementation", project.findProject(":proto") ?: "io.appmetrica.analytics:proto")
        }
    }

    private fun mergePackageName(vararg names: String?): String {
        return names.filter { !it.isNullOrEmpty() }.joinToString(".")
    }
}
