package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.IdentifiersResult
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.startup.FeatureUtils.featureToIdentifierResultInternal
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FeaturesUtilsTest : CommonTest() {

    private val status = IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE
    private val errorExplanation = "some error"

    @Test
    fun featureToIdentifierResultInternalTrue() {
        ObjectPropertyAssertions(true.featureToIdentifierResultInternal(status, errorExplanation))
            .checkField("id", "true")
            .checkField("status", status)
            .checkField("errorExplanation", errorExplanation)
            .checkAll()
    }

    @Test
    fun featureToIdentifierResultInternalFalse() {
        ObjectPropertyAssertions(false.featureToIdentifierResultInternal(status, errorExplanation))
            .checkField("id", "false")
            .checkField("status", status)
            .checkField("errorExplanation", errorExplanation)
            .checkAll()
    }

    @Test
    fun identifierResultToFeatureNullResult() {
        assertThat(FeatureUtils.identifierResultToFeature(null)).isNull()
    }

    @Test
    fun identifierResultToFeatureNullId() {
        assertThat(FeatureUtils.identifierResultToFeature(
            IdentifiersResult(
                null,
                status,
                errorExplanation
            )
        )).isNull()
    }

    @Test
    fun identifierResultToFeatureEmptyId() {
        assertThat(FeatureUtils.identifierResultToFeature(
            IdentifiersResult(
                "",
                status,
                errorExplanation
            )
        )).isNull()
    }

    @Test
    fun identifierResultToFeatureUnknownId() {
        assertThat(FeatureUtils.identifierResultToFeature(
            IdentifiersResult(
                "unknown",
                status,
                errorExplanation
            )
        )).isNull()
    }

    @Test
    fun identifierResultToFeatureTrueId() {
        assertThat(FeatureUtils.identifierResultToFeature(
            IdentifiersResult(
                "true",
                status,
                errorExplanation
            )
        )).isTrue
    }

    @Test
    fun identifierResultToFeatureFalseId() {
        assertThat(FeatureUtils.identifierResultToFeature(
            IdentifiersResult(
                "false",
                status,
                errorExplanation
            )
        )).isFalse
    }
}
