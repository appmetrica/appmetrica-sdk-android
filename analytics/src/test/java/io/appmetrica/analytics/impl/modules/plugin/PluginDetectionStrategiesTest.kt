package io.appmetrica.analytics.impl.modules.plugin

import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn

internal class PluginDetectionStrategiesTest : CommonTest() {

    private val presentClass = "com.example.PresentClass"
    private val absentClass = "com.example.AbsentClass"

    @get:Rule
    val reflectionUtilsRule = staticRule<ReflectionUtils> {
        on { ReflectionUtils.detectClassExists(presentClass) } doReturn true
        on { ReflectionUtils.detectClassExists(absentClass) } doReturn false
    }

    @Test
    fun byClassReturnsTrueWhenClassExists() {
        assertThat(PluginDetectionStrategies.byClass(presentClass).isPresent()).isTrue()
    }

    @Test
    fun byClassReturnsFalseWhenClassAbsent() {
        assertThat(PluginDetectionStrategies.byClass(absentClass).isPresent()).isFalse()
    }

    @Test
    fun byClassReturnsTrueWhenAtLeastOneClassExists() {
        assertThat(PluginDetectionStrategies.byClass(absentClass, presentClass).isPresent()).isTrue()
    }

    @Test
    fun byClassReturnsFalseWhenAllClassesAbsent() {
        assertThat(PluginDetectionStrategies.byClass(absentClass, absentClass).isPresent()).isFalse()
    }
}
