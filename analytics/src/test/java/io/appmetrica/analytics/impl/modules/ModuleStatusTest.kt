package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ModuleStatusTest : CommonTest() {

    @Test
    fun toJsonObject() {
        val moduleStatus = ModuleStatus("module", true)
        val moduleStatusJson = moduleStatus.toJsonObject()

        val newModuleStatus = ModuleStatus.fromJsonObject(moduleStatusJson)

        assertThat(newModuleStatus).usingRecursiveComparison().isEqualTo(moduleStatus)
    }
}
