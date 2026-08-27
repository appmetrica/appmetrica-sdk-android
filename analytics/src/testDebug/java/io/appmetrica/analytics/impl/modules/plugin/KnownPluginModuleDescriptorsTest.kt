package io.appmetrica.analytics.impl.modules.plugin

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class KnownPluginModuleDescriptorsTest : CommonTest() {

    @Test
    fun allContainsExpectedModuleNames() {
        assertThat(KnownPluginModuleDescriptors.ALL.map { it.moduleName }).containsExactly(
            "plugin:Flutter:AppMetrica",
            "plugin:React:AppMetrica",
            "plugin:React:Varioqub",
            "plugin:Unity:AdRevenueAdapter",
            "plugin:Unity:AppMetrica",
        )
    }
}
