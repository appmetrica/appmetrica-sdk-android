package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.IdentifiersResult
import io.appmetrica.analytics.StartupParamsItemStatus
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.whenever

class StartupParamItemAdapterTest : CommonTest() {

    @get:Rule
    val startupParamItemStatusAdapterMockedConstructionRule =
        MockedConstructionRule(StartupParamItemStatusAdapter::class.java)

    private val identifierStatus = IdentifierStatus.FEATURE_DISABLED
    private val startupParamsItemStatus = StartupParamsItemStatus.FEATURE_DISABLED

    private lateinit var startupParamItemStatusAdapter: StartupParamItemStatusAdapter
    private lateinit var startupParamItemAdapter: StartupParamItemAdapter

    @Before
    fun setUp() {
        startupParamItemAdapter = StartupParamItemAdapter()

        assertThat(startupParamItemStatusAdapterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(startupParamItemStatusAdapterMockedConstructionRule.argumentInterceptor.flatArguments()).isEmpty()
        startupParamItemStatusAdapter =
            startupParamItemStatusAdapterMockedConstructionRule.constructionMock.constructed().first()

        whenever(startupParamItemStatusAdapter.adapt(identifierStatus)).thenReturn(startupParamsItemStatus)
    }

    @Test
    fun adapt() {
        val input = IdentifiersResult("Some value", identifierStatus, "Some error")
        ObjectPropertyAssertions(startupParamItemAdapter.adapt(input))
            .checkField("id", input.id)
            .checkField("status", startupParamsItemStatus)
            .checkField("errorDetails", input.errorExplanation)
            .checkAll()
    }

    @Test
    fun `adapt with nulls`() {
        val input = IdentifiersResult(null, identifierStatus, null)
        ObjectPropertyAssertions(startupParamItemAdapter.adapt(input))
            .checkFieldIsNull("id")
            .checkField("status", startupParamsItemStatus)
            .checkFieldIsNull("errorDetails")
            .checkAll()
    }
}
