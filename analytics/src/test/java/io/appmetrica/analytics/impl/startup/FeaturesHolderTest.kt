package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.IdentifiersResult
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FeaturesHolderTest : CommonTest() {

    private val featuresHolder = FeaturesHolder()

    @Test
    fun getFeatures() {
        assertThat(featuresHolder.features).isEqualToComparingFieldByField(FeaturesInternal())
        val features = FeaturesInternal(true, IdentifierStatus.OK, null)
        featuresHolder.features = features
        assertThat(featuresHolder.features).isSameAs(features)
        val newFeatures = FeaturesInternal(false, IdentifierStatus.OK, null)
        featuresHolder.features = newFeatures
        assertThat(featuresHolder.features).isSameAs(newFeatures)
    }

    @Test
    fun getFeature() {
        val unknownKey = "unknown key"
        assertThat(featuresHolder.getFeature(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED)).isNull()
        assertThat(featuresHolder.getFeature(unknownKey)).isNull()
        featuresHolder.features = FeaturesInternal(true, IdentifierStatus.OK, "error")
        assertThat(featuresHolder.getFeature(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED))
            .isEqualToComparingFieldByField(
                IdentifiersResult(
                    "true",
                    IdentifierStatus.OK,
                    "error"
                )
            )
        assertThat(featuresHolder.getFeature(unknownKey)).isNull()
    }

    @Test
    fun putToMapNoIdentifier() {
        val map = mutableMapOf("key" to IdentifiersResult(
            "id",
            IdentifierStatus.NO_STARTUP,
            "error"
        )
        )
        featuresHolder.putToMap(listOf(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED), map)
        assertThat(map).containsExactlyInAnyOrderEntriesOf(
            mapOf("key" to IdentifiersResult(
                "id",
                IdentifierStatus.NO_STARTUP,
                "error"
            )
            )
        )
    }

    @Test
    fun putToMapHasIdentifier() {
        val map = mutableMapOf("key" to IdentifiersResult(
            "id",
            IdentifierStatus.NO_STARTUP,
            "error"
        )
        )
        featuresHolder.features= FeaturesInternal(false, IdentifierStatus.OK, "some error")
        featuresHolder.putToMap(listOf(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED), map)
        assertThat(map).containsExactlyInAnyOrderEntriesOf(mapOf(
            "key" to IdentifiersResult(
                "id",
                IdentifierStatus.NO_STARTUP,
                "error"
            ),
            Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED to IdentifiersResult(
                "false",
                IdentifierStatus.OK,
                "some error"
            ),
        ))
    }

    @Test
    fun putToMapIgnoresUnknownKey() {
        val map = mutableMapOf("key" to IdentifiersResult(
            "id",
            IdentifierStatus.NO_STARTUP,
            "error"
        )
        )
        featuresHolder.features= FeaturesInternal(false, IdentifierStatus.OK, "some error")
        featuresHolder.putToMap(listOf(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED, "unknown key"), map)
        assertThat(map).containsExactlyInAnyOrderEntriesOf(mapOf(
            "key" to IdentifiersResult(
                "id",
                IdentifierStatus.NO_STARTUP,
                "error"
            ),
            Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED to IdentifiersResult(
                "false",
                IdentifierStatus.OK,
                "some error"
            ),
        ))
    }
}
