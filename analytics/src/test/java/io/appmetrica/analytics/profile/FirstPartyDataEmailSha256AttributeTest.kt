package io.appmetrica.analytics.profile

import io.appmetrica.analytics.impl.profile.CommonSavingStrategy
import io.appmetrica.analytics.impl.profile.StringSetUpdatePatcher
import io.appmetrica.analytics.impl.profile.fpd.Sha256Converter
import io.appmetrica.analytics.impl.utils.limitation.StringTrimmer
import io.appmetrica.analytics.impl.utils.validation.DummyValidator
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class FirstPartyDataEmailSha256AttributeTest : CommonTest() {

    private val values = listOf(
        "value_1",
        "value_2"
    )
    private val convertedValues = listOf(
        "convertedValue_1",
        "convertedValue_2"
    )

    private val hashConverter: Sha256Converter = mock {
        on { convert(values) } doReturn convertedValues
    }

    @get:Rule
    val commonSavingStrategyRule = constructionRule<CommonSavingStrategy>()

    @get:Rule
    val stringSetUpdatePatcherRule = constructionRule<StringSetUpdatePatcher>()

    @get:Rule
    val stringTrimmerRule = constructionRule<StringTrimmer>()

    @get:Rule
    val dummyValidatorRule = constructionRule<DummyValidator<String>>()

    private val attribute by setUp {
        FirstPartyDataEmailSha256Attribute(
            hashConverter,
        )
    }

    @Test
    fun withEmailValues() {
        val result = attribute.withEmailValues(values)

        verify(hashConverter).convert(values)

        assertThat(result).isNotNull
        assertThat(commonSavingStrategyRule.constructionMock.constructed()).hasSize(1)
        assertThat(stringSetUpdatePatcherRule.constructionMock.constructed()).hasSize(1)

        val patcherArgs = stringSetUpdatePatcherRule.argumentInterceptor.flatArguments()
        assertThat(patcherArgs[0]).isEqualTo("appmetrica_1pd_email_sha256")
        assertThat(patcherArgs[1]).isEqualTo(convertedValues)
        assertThat(patcherArgs[2]).isEqualTo(10)
        assertThat(patcherArgs[3]).isInstanceOf(StringTrimmer::class.java)
        assertThat(patcherArgs[4]).isInstanceOf(DummyValidator::class.java)
        assertThat(patcherArgs[5]).isEqualTo(commonSavingStrategyRule.constructionMock.constructed().first())
    }

    @Test
    fun withVarargValues() {
        val result = attribute.withEmailValues(*values.toTypedArray())

        verify(hashConverter).convert(values)

        assertThat(result).isNotNull
        assertThat(commonSavingStrategyRule.constructionMock.constructed()).hasSize(1)
        assertThat(stringSetUpdatePatcherRule.constructionMock.constructed()).hasSize(1)

        val patcherArgs = stringSetUpdatePatcherRule.argumentInterceptor.flatArguments()
        assertThat(patcherArgs[0]).isEqualTo("appmetrica_1pd_email_sha256")
        assertThat(patcherArgs[1]).isEqualTo(convertedValues)
        assertThat(patcherArgs[2]).isEqualTo(10)
        assertThat(patcherArgs[3]).isInstanceOf(StringTrimmer::class.java)
        assertThat(patcherArgs[4]).isInstanceOf(DummyValidator::class.java)
        assertThat(patcherArgs[5]).isEqualTo(commonSavingStrategyRule.constructionMock.constructed().first())
    }

    @Test
    fun withEmptyValues() {
        val emptyValues = emptyList<String>()
        val emptyConvertedValues = emptyList<String>()
        whenever(hashConverter.convert(emptyValues)).thenReturn(emptyConvertedValues)

        val result = attribute.withEmailValues(emptyValues)

        verify(hashConverter).convert(emptyValues)
        assertThat(result).isNotNull

        val patcherArgs = stringSetUpdatePatcherRule.argumentInterceptor.flatArguments()
        assertThat(patcherArgs[1]).isEqualTo(emptyConvertedValues)
    }

    @Test
    fun withSingleValue() {
        val singleValue = listOf("single@email.com")
        val singleConverted = listOf("converted_single")
        whenever(hashConverter.convert(singleValue)).thenReturn(singleConverted)

        val result = attribute.withEmailValues(singleValue)

        verify(hashConverter).convert(singleValue)
        assertThat(result).isNotNull

        val patcherArgs = stringSetUpdatePatcherRule.argumentInterceptor.flatArguments()
        assertThat(patcherArgs[1]).isEqualTo(singleConverted)
    }

    @Test
    fun withMaximumValues() {
        val maxValues = (1..10).map { "email$it@test.com" }
        val maxConverted = (1..10).map { "converted$it" }
        whenever(hashConverter.convert(maxValues)).thenReturn(maxConverted)

        val result = attribute.withEmailValues(maxValues)

        verify(hashConverter).convert(maxValues)
        assertThat(result).isNotNull

        val patcherArgs = stringSetUpdatePatcherRule.argumentInterceptor.flatArguments()
        assertThat(patcherArgs[1]).isEqualTo(maxConverted)
        assertThat(patcherArgs[2]).isEqualTo(10) // verify limit is 10
    }

    @Test
    fun withMoreThanMaximumValues() {
        val manyValues = (1..15).map { "email$it@test.com" }
        val manyConverted = (1..15).map { "converted$it" }
        whenever(hashConverter.convert(manyValues)).thenReturn(manyConverted)

        val result = attribute.withEmailValues(manyValues)

        verify(hashConverter).convert(manyValues)
        assertThat(result).isNotNull

        // Verify that limit is still 10, StringSetUpdatePatcher will handle limiting
        val patcherArgs = stringSetUpdatePatcherRule.argumentInterceptor.flatArguments()
        assertThat(patcherArgs[2]).isEqualTo(10)
    }

    @Test
    fun verifyAttributeKey() {
        attribute.withEmailValues(values)

        val patcherArgs = stringSetUpdatePatcherRule.argumentInterceptor.flatArguments()
        assertThat(patcherArgs[0]).isEqualTo("appmetrica_1pd_email_sha256")
    }

    @Test
    fun returnsUserProfileUpdate() {
        val result = attribute.withEmailValues(values)

        assertThat(result).isInstanceOf(UserProfileUpdate::class.java)
    }

    @Test
    fun hashConverterIsCalledWithOriginalValues() {
        val originalValues = listOf("test1@email.com", "test2@email.com", "test3@email.com")
        val converted = listOf("hash1", "hash2", "hash3")
        whenever(hashConverter.convert(originalValues)).thenReturn(converted)

        attribute.withEmailValues(originalValues)

        verify(hashConverter).convert(originalValues)

        val patcherArgs = stringSetUpdatePatcherRule.argumentInterceptor.flatArguments()
        assertThat(patcherArgs[1]).isEqualTo(converted)
    }

    @Test
    fun varargConvertedToIterable() {
        val val1 = "email1@test.com"
        val val2 = "email2@test.com"
        val val3 = "email3@test.com"

        val expectedList = listOf(val1, val2, val3)
        val converted = listOf("hash1", "hash2", "hash3")
        whenever(hashConverter.convert(expectedList)).thenReturn(converted)

        attribute.withEmailValues(val1, val2, val3)

        verify(hashConverter).convert(expectedList)
    }
}
