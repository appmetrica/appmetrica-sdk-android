package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.AdvIdentifiersResult
import io.appmetrica.analytics.StartupParamsItem
import io.appmetrica.analytics.StartupParamsItemStatus
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class AdvIdentifiersFromStartupParamsItemConverterTest : CommonTest() {

    private val gaid = "gaid"
    private val hoaid = "hoaid"
    private val yandex = "yandex"
    private val gaidError = "gaid error"
    private val hoaidError = "hoaid error"
    private val yandexError = "yandex error"

    private val gaidStartupParamsItem = mock<StartupParamsItem> {
        on { id } doReturn gaid
        on { status } doReturn StartupParamsItemStatus.OK
        on { errorDetails } doReturn gaidError
    }

    private val hoaidStartupParamsItem = mock<StartupParamsItem> {
        on { id } doReturn hoaid
        on { status } doReturn StartupParamsItemStatus.OK
        on { errorDetails } doReturn hoaidError
    }

    private val yandexStartupParamsItem = mock<StartupParamsItem> {
        on { id } doReturn yandex
        on { status } doReturn StartupParamsItemStatus.OK
        on { errorDetails } doReturn yandexError
    }

    private lateinit var converter: AdvIdentifiersFromStartupParamsItemConverter

    @Before
    fun setUp() {
        converter = AdvIdentifiersFromStartupParamsItemConverter()
    }

    @Test
    fun `convert for null`() {
        assertThat(converter.convert(null, null, null))
            .usingRecursiveComparison()
            .isEqualTo(
                AdvIdentifiersResult(
                    AdvIdentifiersResult.AdvId(
                        null,
                        AdvIdentifiersResult.Details.INTERNAL_ERROR,
                        null
                    ),
                    AdvIdentifiersResult.AdvId(
                        null,
                        AdvIdentifiersResult.Details.INTERNAL_ERROR,
                        null
                    ),
                    AdvIdentifiersResult.AdvId(
                        null,
                        AdvIdentifiersResult.Details.INTERNAL_ERROR,
                        null
                    )
                )
            )
    }

    @Test
    fun `convert for filled`() {
        assertThat(converter.convert(gaidStartupParamsItem, hoaidStartupParamsItem, yandexStartupParamsItem))
            .usingRecursiveComparison()
            .isEqualTo(
                AdvIdentifiersResult(
                    AdvIdentifiersResult.AdvId(
                        gaid,
                        AdvIdentifiersResult.Details.OK,
                        gaidError
                    ),
                    AdvIdentifiersResult.AdvId(
                        hoaid,
                        AdvIdentifiersResult.Details.OK,
                        hoaidError
                    ),
                    AdvIdentifiersResult.AdvId(
                        yandex,
                        AdvIdentifiersResult.Details.OK,
                        yandexError
                    )
                )
            )
    }

    @Test
    fun `convert for google only`() {
        assertThat(converter.convert(gaidStartupParamsItem, null, null))
            .usingRecursiveComparison()
            .isEqualTo(
                AdvIdentifiersResult(
                    AdvIdentifiersResult.AdvId(
                        gaid,
                        AdvIdentifiersResult.Details.OK,
                        gaidError
                    ),
                    AdvIdentifiersResult.AdvId(
                        null,
                        AdvIdentifiersResult.Details.INTERNAL_ERROR,
                        null
                    ),
                    AdvIdentifiersResult.AdvId(
                        null,
                        AdvIdentifiersResult.Details.INTERNAL_ERROR,
                        null
                    )
                )
            )
    }

    @Test
    fun `convert for hoaid only`() {
        assertThat(converter.convert(null, hoaidStartupParamsItem, null))
            .usingRecursiveComparison()
            .isEqualTo(
                AdvIdentifiersResult(
                    AdvIdentifiersResult.AdvId(
                        null,
                        AdvIdentifiersResult.Details.INTERNAL_ERROR,
                        null
                    ),
                    AdvIdentifiersResult.AdvId(
                        hoaid,
                        AdvIdentifiersResult.Details.OK,
                        hoaidError
                    ),
                    AdvIdentifiersResult.AdvId(
                        null,
                        AdvIdentifiersResult.Details.INTERNAL_ERROR,
                        null
                    )
                )
            )
    }

    @Test
    fun `convert for yandex only`() {
        assertThat(converter.convert(null, null, yandexStartupParamsItem))
            .usingRecursiveComparison()
            .isEqualTo(
                AdvIdentifiersResult(
                    AdvIdentifiersResult.AdvId(
                        null,
                        AdvIdentifiersResult.Details.INTERNAL_ERROR,
                        null
                    ),
                    AdvIdentifiersResult.AdvId(
                        null,
                        AdvIdentifiersResult.Details.INTERNAL_ERROR,
                        null
                    ),
                    AdvIdentifiersResult.AdvId(
                        yandex,
                        AdvIdentifiersResult.Details.OK,
                        yandexError
                    )
                )
            )
    }
}
