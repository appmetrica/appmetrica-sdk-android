package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.StartupParamsItemStatus
import io.appmetrica.analytics.StartupParamsItem
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

class FeaturesHolderTest : CommonTest() {

    private lateinit var featuresHolder: FeaturesHolder

    @get:Rule
    val startupParamsItemAdapterMockedConstructionRule = MockedConstructionRule(StartupParamItemAdapter::class.java)

    private lateinit var startupParamItemAdapter: StartupParamItemAdapter

    @Before
    fun setUp() {
        featuresHolder = FeaturesHolder()

        assertThat(startupParamsItemAdapterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(startupParamsItemAdapterMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        startupParamItemAdapter = startupParamsItemAdapterMockedConstructionRule.constructionMock.constructed().first()
    }

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
        val map = mutableMapOf("key" to StartupParamsItem(
            "id",
            StartupParamsItemStatus.NETWORK_ERROR,
            "error"
        )
        )
        featuresHolder.putToMap(listOf(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED), map)
        assertThat(map).containsExactlyInAnyOrderEntriesOf(map)
    }

    @Test
    fun putToMapHasIdentifier() {
        val firstEntry = "key" to StartupParamsItem(
            "id",
            StartupParamsItemStatus.NETWORK_ERROR,
            "error"
        )

        val libSslStartupParamsItem = StartupParamsItem("false", StartupParamsItemStatus.OK, "some error")
        val map = mutableMapOf(firstEntry)

        whenever(startupParamItemAdapter.adapt(eq(
            IdentifiersResult(
                "false",
                IdentifierStatus.OK,
                "some error"
            )
        )))
            .thenReturn(libSslStartupParamsItem)

        featuresHolder.features = FeaturesInternal(false, IdentifierStatus.OK, "some error")
        featuresHolder.putToMap(listOf(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED), map)
        assertThat(map).containsExactlyInAnyOrderEntriesOf(mapOf(
            firstEntry,
            Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED to libSslStartupParamsItem,
        ))
    }

    @Test
    fun putToMapIgnoresUnknownKey() {
        val initialEntry = "key" to StartupParamsItem(
            "id",
            StartupParamsItemStatus.NETWORK_ERROR,
            "error"
        )
        val libSslStartupParamsItem = StartupParamsItem("false", StartupParamsItemStatus.OK, "some error")
        val map = mutableMapOf(initialEntry)
        featuresHolder.features= FeaturesInternal(false, IdentifierStatus.OK, "some error")
        whenever(startupParamItemAdapter.adapt(eq(
            IdentifiersResult(
                "false",
                IdentifierStatus.OK,
                "some error"
            )
        )))
            .thenReturn(libSslStartupParamsItem)
        featuresHolder.putToMap(listOf(Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED, "unknown key"), map)
        assertThat(map).containsExactlyInAnyOrderEntriesOf(mapOf(
            initialEntry,
            Constants.StartupParamsCallbackKeys.FEATURE_LIB_SSL_ENABLED to libSslStartupParamsItem,
        ))
    }
}
