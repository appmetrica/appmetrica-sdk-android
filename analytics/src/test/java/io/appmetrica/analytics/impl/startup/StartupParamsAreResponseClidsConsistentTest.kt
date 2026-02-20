package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.impl.utils.StartupUtils
import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@RunWith(Parameterized::class)
internal class StartupParamsAreResponseClidsConsistentTest(
    inputResponseClids: MutableMap<String, String>?,
    inputClientClids: MutableMap<String, String>?,
    private val expectedResult: Boolean
) : CommonTest() {
    private val responseClids = IdentifiersResult(
        JsonHelper.clidsToString(inputResponseClids),
        IdentifierStatus.OK,
        null
    )
    private val clientClids: String = StartupUtils.encodeClids(inputClientClids)

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule

    private val uuidResult = IdentifiersResult("test-uuid", IdentifierStatus.OK, null)

    private val storage: PreferencesClientDbStorage = mock {
        on { getClientClids(anyOrNull()) } doReturn clientClids
        on { responseClidsResult } doReturn responseClids
        on { customSdkHosts } doReturn IdentifiersResult(null, IdentifierStatus.UNKNOWN, null)
        on { getFeatures() } doReturn FeaturesInternal(null, IdentifierStatus.UNKNOWN, null)
        on { uuidResult } doReturn uuidResult
    }

    private lateinit var startupParams: StartupParams

    @Before
    fun setUp() {
        StartupParamsTestUtils.mockPreferencesClientDbStoragePutResponses(storage)
        startupParams = StartupParams(context, storage)
    }

    @Test
    fun areResponseClidsConsistent() {
        assertThat(startupParams.areResponseClidsConsistent()).isEqualTo(expectedResult)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(
            name = "[{index}]Return {2} for responseClids = \"{0}\" and " +
                "clientClids = \"{1}\""
        )
        fun data() = listOf(
            arrayOf(null, null, true),
            arrayOf(null, mutableMapOf<Any?, Any?>(), true),
            arrayOf(null, StartupParamsTestUtils.CLIDS_MAP_2, true),
            arrayOf(mutableMapOf<Any?, Any?>(), null, true),
            arrayOf(mutableMapOf<Any?, Any?>(), mutableMapOf<Any?, Any?>(), true),
            arrayOf(mutableMapOf<Any?, Any?>(), StartupParamsTestUtils.CLIDS_MAP_2, false),
            arrayOf(StartupParamsTestUtils.CLIDS_MAP_1, null, true),
            arrayOf(StartupParamsTestUtils.CLIDS_MAP_1, mutableMapOf<Any?, Any?>(), true),
            arrayOf(StartupParamsTestUtils.CLIDS_MAP_1, StartupParamsTestUtils.CLIDS_MAP_2, true)
        )
    }
}
