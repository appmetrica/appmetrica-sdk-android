package io.appmetrica.analytics.gradle.publishing

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.LibraryVariant
import io.appmetrica.analytics.gradle.isCIBuild
import io.appmetrica.gradle.repositories.sonatypeRepository
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import java.io.File
import java.util.Locale

class PublishingPlugin : Plugin<Project> {

    companion object {
        private const val EXTENSION_NAME = "publishingInfo"
    }

    private val flavorExtensions = mutableMapOf<String, PublishingInfoFlavorExtension>()
    private val buildTypeExtensions = mutableMapOf<String, PublishingInfoBuildTypeExtension>()

    override fun apply(project: Project) {
        project.apply<MavenPublishPlugin>() // id("maven-publish")
        project.apply<SigningPlugin>() // id("signing")

        if (!project.plugins.hasPlugin("com.android.library")) {
            throw GradleException("appmetrica-publish plugin requires the com.android.library plugin")
        }

        val artifactoryBuildType = project.property("artifactoryBuildType")

        val extension = project.createExtensions()
        project.configureSign()
        project.the<LibraryExtension>().libraryVariants.configureEach {
            project.configureJavadoc(this, extension)
        }

        project.configure<PublishingExtension> {
            sonatypeRepository("sonatypeRelease")

            publications {
                project.the<LibraryExtension>().libraryVariants.configureEach {
                    val variant = this
                    create<MavenPublication>(variant.name) {
                        (this as MavenPublicationInternal).isAlias = variant.buildType.name != artifactoryBuildType
                        from(project.components[variant.name])

                        groupId = project.group.toString()
                        artifactId = getArtifactIdFor(variant, extension)
                        version = variant.mergedFlavor.versionName + (variant.buildType.versionNameSuffix ?: "")

                        artifact(project.registerSourcesJarTask(variant, extension))
                        if (extension.withJavadoc.get()) {
                            artifact(project.registerJavadocTask(variant, extension))
                        }

                        pom {
                            name.set(extension.name)
                            description.set("AppMetrica SDK Module")
                            url.set("https://appmetrica.io/")

                            licenses {
                                license {
                                    name.set("MIT License")
                                    url.set("https://www.opensource.org/licenses/mit-license.php")
                                    distribution.set("repo")
                                }
                            }

                            developers {
                                developer {
                                    name.set("AppMetrica")
                                    url.set("https://appmetrica.io/")
                                }
                            }

                            scm {
                                connection.set("scm:git:https://github.com/appmetrica/appmetrica-sdk-android.git")
                                developerConnection.set("scm:git:https://github.com/appmetrica/appmetrica-sdk-android.git")
                                url.set("https://github.com/appmetrica/appmetrica-sdk-android")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Project.configureJavadoc(
        variant: LibraryVariant,
        extension: PublishingInfoExtension
    ) {
        val capitalVariantName = variant.name.capitalize(Locale.ROOT)
        val android = project.the<LibraryExtension>()

        project.tasks.register("prepare${capitalVariantName}Javadoc", Javadoc::class.java) {
            source = files(variant.sourceSets.flatMap { it.javaDirectories }).asFileTree
            exclude("**/impl/**")
            exclude("**/internal/**")

            classpath = files(variant.javaCompile.outputs.files) +
                files("${android.sdkDirectory.path}/platforms/${android.compileSdkVersion}/android.jar") +
                variant.getCompileClasspath(null)

            title = extension.name.get()
            options.encoding = "UTF-8"
            if (extension.checkAllJavadoc.get()) {
                (options as CoreJavadocOptions).addStringOption("Xwerror", "-quiet")
            }
        }
    }

    private fun Project.createExtensions(): PublishingInfoExtension {
        val extension = extensions.create<PublishingInfoExtension>(EXTENSION_NAME)
        extension.baseArtifactId.convention(project.name)
        extension.withJavadoc.convention(true)
        extension.checkAllJavadoc.convention(true)
        project.the<LibraryExtension>().productFlavors.configureEach {
            val ext = extensions.create<PublishingInfoFlavorExtension>(EXTENSION_NAME)
            ext.artifactIdSuffix.convention("")
            ext.withSources.convention(true)
            flavorExtensions[name] = ext
        }
        project.the<LibraryExtension>().buildTypes.configureEach {
            val ext = extensions.create<PublishingInfoBuildTypeExtension>(EXTENSION_NAME)
            ext.artifactIdSuffix.convention("")
            ext.withSources.convention(true)
            buildTypeExtensions[name] = ext
        }
        return extension
    }

    private fun Project.configureSign() {
        val signingKeyId = project.properties["signing.keyId"] ?: System.getenv("SIGNING_KEY_ID")
        val signingPassword = project.properties["signing.password"] ?: System.getenv("SIGNING_PASSWORD")
        val signingSecretKeyRingFile = project.properties["signing.secretKeyRingFile"]
            ?: System.getenv("SIGNING_SECRET_KEY_RING_FILE")
        project.extra["signing.keyId"] = signingKeyId
        project.extra["signing.password"] = signingPassword
        project.extra["signing.secretKeyRingFile"] = signingSecretKeyRingFile?.let { File(it.toString()) }
        configure<SigningExtension> {
            sign(the<PublishingExtension>().publications)
        }
        tasks.withType<Sign> {
            onlyIf { isCIBuild && signingKeyId != null && signingPassword != null && signingSecretKeyRingFile != null }
        }
    }

    private fun getArtifactIdFor(variant: LibraryVariant, extension: PublishingInfoExtension): String {
        var artifactId = extension.baseArtifactId.get()
        variant.productFlavors.forEach {
            artifactId += flavorExtensions.getValue(it.name).artifactIdSuffix.get()
        }
        artifactId += buildTypeExtensions.getValue(variant.buildType.name).artifactIdSuffix.get()
        return artifactId
    }

    private fun Project.getSourcesForVariant(variant: LibraryVariant): Iterable<Any> {
        val withSourcesForFlavors = variant.productFlavors.all {
            flavorExtensions.getValue(it.name).withSources.get()
        }
        val withSourcesForBuildType = buildTypeExtensions.getValue(variant.buildType.name).withSources.get()
        return if (withSourcesForFlavors && withSourcesForBuildType) {
            variant.sourceSets.map { it.javaDirectories }
        } else {
            zipTree(PublishingPlugin::class.java.getResource("/source_code.txt")!!.path.replace("!/source_code.txt", ""))
                .matching { include("source_code.txt") }
        }
    }

    private fun Project.registerSourcesJarTask(variant: LibraryVariant, extension: PublishingInfoExtension): TaskProvider<Jar> {
        return tasks.register<Jar>("generate${variant.name.capitalize(Locale.ROOT)}SourcesArtifact") {
            archiveClassifier.set("sources")
            archiveBaseName.set(getArtifactIdFor(variant, extension))
            destinationDirectory.set(layout.buildDirectory.dir("artifacts/sources"))
            from(getSourcesForVariant(variant))

            mustRunAfter(variant.assembleProvider)
        }
    }

    private fun Project.registerJavadocTask(variant: LibraryVariant, extension: PublishingInfoExtension): TaskProvider<Jar> {
        val capitalVariantName = variant.name.capitalize(Locale.ROOT)

        return tasks.register<Jar>("prepare${capitalVariantName}JavadocJar") {
            group = "javadoc"
            description = "Generates Javadoc jar for $capitalVariantName"

            archiveClassifier.set("javadoc")
            archiveBaseName.set(getArtifactIdFor(variant, extension))
            destinationDirectory.set(layout.buildDirectory.dir("artifacts/javadoc-jar/$capitalVariantName/"))
            from(project.tasks.named<Javadoc>("prepare${capitalVariantName}Javadoc").get().outputs)

            mustRunAfter(variant.assembleProvider)
        }
    }
}
