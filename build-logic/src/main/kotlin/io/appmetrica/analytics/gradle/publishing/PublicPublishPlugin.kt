package io.appmetrica.analytics.gradle.publishing

import io.appmetrica.gradle.publishing.MavenCentralPublishExtension
import io.appmetrica.gradle.publishing.MavenCentralPublishPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType

class PublicPublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.apply<MavenCentralPublishPlugin>()

        val ids = project.objects.setProperty(String::class.java)

        project.afterEvaluate {
            project.subprojects
                .filter { it.name != "ndkcrashes" }
                .forEach { subproject ->
                    subproject.plugins.withType<PublishingPlugin> {
                        subproject.extensions.findByType<PublishingInfoExtension>()
                            ?.also { extension ->
                                ids.add(extension.baseArtifactId)
                            }
                    }
                }
            project.configure<MavenCentralPublishExtension> {
                publishToMavenLocalTask.set(project.tasks.named("publishReleasePublicationToMavenLocal"))
                artifactIds.set(ids)
            }
        }
    }
}
