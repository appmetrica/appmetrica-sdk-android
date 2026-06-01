package io.appmetrica.analytics.impl.modules.plugin

import io.appmetrica.analytics.impl.modules.ModuleStatus
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class PluginModuleStatusDetectorTest : CommonTest() {

    private val presentStrategy: PluginDetectionStrategy = mock { on { isPresent() } doReturn true }
    private val absentStrategy: PluginDetectionStrategy = mock { on { isPresent() } doReturn false }

    private val descriptors = listOf(
        PluginModuleDescriptor("plugin:Unity:AppMetrica", presentStrategy),
        PluginModuleDescriptor("plugin:React:AppMetrica", absentStrategy),
    )

    private val detector = PluginModuleStatusDetector(descriptors)

    @Test
    fun detectMapsDescriptorsToModuleStatuses() {
        assertThat(detector.detect()).containsExactly(
            ModuleStatus("plugin:Unity:AppMetrica", true),
            ModuleStatus("plugin:React:AppMetrica", false),
        )
    }

    @Test
    fun detectReturnsEmptyListForEmptyDescriptors() {
        assertThat(PluginModuleStatusDetector(emptyList()).detect()).isEmpty()
    }
}
