package io.appmetrica.analytics.gradle.publishing

import io.appmetrica.gradle.publishing.MavenCentralPublishExtension
import io.appmetrica.gradle.publishing.MavenCentralPublishPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType

class NdkCrashesPublicPublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.apply<MavenCentralPublishPlugin>()

        project.afterEvaluate {
            project.plugins.withType<PublishingPlugin> {
                project.configure<MavenCentralPublishExtension> {
                    publishToMavenLocalTask.set(project.tasks.named("publishReleasePublicationToMavenLocal"))
                    artifactIds.set(
                        listOf(
                            project.extensions.findByType<PublishingInfoExtension>()!!
                                .baseArtifactId
                                .get()
                        )
                    )
                }
            }
        }
    }
}
