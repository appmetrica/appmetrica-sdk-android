package io.appmetrica.analytics.impl.profile

import io.appmetrica.analytics.impl.utils.limitation.Trimmer
import io.appmetrica.analytics.impl.utils.validation.Validator
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class StringSetUpdatePatcherTest : CommonTest() {

    private val keyPrefix = "some_key_prefix"
    private val valuesCountLimit = 3
    private val valueTrimmer: Trimmer<String> = mock()
    private val keyValidator: Validator<String> = mock()
    private val saver: BaseSavingStrategy = mock()

    @get:Rule
    val stringUpdatePatcherRule = constructionRule<StringUpdatePatcher>()

    @Test
    fun constructorMakesListOfPatchers() {
        val values = (0 until valuesCountLimit).map { "value_$it" }.toList()
        val patcher = StringSetUpdatePatcher(
            keyPrefix,
            values,
            valuesCountLimit,
            valueTrimmer,
            keyValidator,
            saver
        )
        assertThat(stringUpdatePatcherRule.constructionMock.constructed())
            .hasSize(values.size)
        assertThat(stringUpdatePatcherRule.argumentInterceptor.arguments).containsExactlyElementsOf(
            values.mapIndexed { index, value ->
                listOf("${keyPrefix}_$index", value, valueTrimmer, keyValidator, saver)
            }
        )
    }

    @Test
    fun constructorLimitsValuesCount() {
        val values = (0 until valuesCountLimit + 1).map { "value_$it" }.toList()
        val patcher = StringSetUpdatePatcher(
            keyPrefix,
            values,
            valuesCountLimit,
            valueTrimmer,
            keyValidator,
            saver
        )
        assertThat(stringUpdatePatcherRule.constructionMock.constructed())
            .hasSize(valuesCountLimit)
    }

    @Test
    fun constructorRemovesDuplicates() {
        val values = (0 until valuesCountLimit).map { "value" }.toList()
        val patcher = StringSetUpdatePatcher(
            keyPrefix,
            values,
            valuesCountLimit,
            valueTrimmer,
            keyValidator,
            saver
        )
        assertThat(stringUpdatePatcherRule.constructionMock.constructed())
            .hasSize(1)
    }

    @Test
    fun applyCallsApplyOnPatchers() {
        val values = (0 until valuesCountLimit).map { "value_$it" }.toList()
        val patcher = StringSetUpdatePatcher(
            keyPrefix,
            values,
            valuesCountLimit,
            valueTrimmer,
            keyValidator,
            saver
        )
        val userProfileStorage: UserProfileStorage = mock()
        patcher.apply(userProfileStorage)
        stringUpdatePatcherRule.constructionMock.constructed().forEach {
            verify(it).apply(userProfileStorage)
        }
    }

    @Test
    fun setPublicLoggerCallsSetPublicLoggerOnPatchers() {
        val values = (0 until valuesCountLimit).map { "value_$it" }.toList()
        val patcher = StringSetUpdatePatcher(
            keyPrefix,
            values,
            valuesCountLimit,
            valueTrimmer,
            keyValidator,
            saver
        )
        val publicLogger: PublicLogger = mock()
        patcher.setPublicLogger(publicLogger)
        stringUpdatePatcherRule.constructionMock.constructed().forEach {
            verify(it).setPublicLogger(publicLogger)
        }
    }
}
