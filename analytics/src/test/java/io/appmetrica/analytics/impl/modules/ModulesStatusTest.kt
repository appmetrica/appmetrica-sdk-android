package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ModulesStatusTest : CommonTest() {

    @Test
    fun toJson() {
        val modulesStatus = ModulesStatus(
            listOf(
                ModuleStatus("module1", true),
                ModuleStatus("module2", false)
            ),
            112431243L
        )

        val json = modulesStatus.toJson()
        val newModulesStatus = ModulesStatus.fromJson(json)

        assertThat(newModulesStatus).usingRecursiveComparison().isEqualTo(modulesStatus)
    }
}
