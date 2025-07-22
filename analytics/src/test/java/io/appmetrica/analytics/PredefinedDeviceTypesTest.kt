package io.appmetrica.analytics

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.lang.reflect.Modifier

class PredefinedDeviceTypesTest : CommonTest() {

    @Test
    fun allValuesContainsAllPredefinedTypes() {
        val definedTypes = PredefinedDeviceTypes::class.java.declaredFields
            .filter {
                Modifier.isPublic(it.modifiers) &&
                    Modifier.isStatic(it.modifiers) &&
                    Modifier.isFinal(it.modifiers) &&
                    it.type == String::class.java
            }
            .map { it.get(null) as String }

        assertThat(definedTypes)
            .containsExactlyInAnyOrderElementsOf(PredefinedDeviceTypes.ALL_VALUES)
    }
}
