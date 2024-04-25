package io.appmetrica.analytics.impl.startup

import android.text.TextUtils
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.impl.startup.CollectingFlags.CollectingFlagsBuilder
import io.appmetrica.analytics.impl.utils.ServerTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.argThat
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.ParameterizedRobolectricTestRunner


@RunWith(ParameterizedRobolectricTestRunner::class)
internal class StartupUnitClidsFromStartupTest(
    description: String,
    private val storedClids: String?,
    private val clidsFromStartup: String?,
    private val expectedClids: String?
) : StartupUnitBaseTest() {

    companion object {
        private const val STORED_VALID_CLIDS = "clid0:0"
        private const val VALID_CLIDS_FROM_STARTUP = "clid0:1"

        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> =
            listOf(
                arrayOf("Startup and stored clids are null", null, null, null),
                arrayOf("Startup clids are null and stored clids are empty", "", null, null),
                arrayOf(
                    "Startup clids are null and stored clids are valid",
                    STORED_VALID_CLIDS,
                    null,
                    STORED_VALID_CLIDS
                ),
                arrayOf("Startup clids are empty and stored clids are null", null, "", null),
                arrayOf("Startup clids are empty and stored clids are empty", "", "", null),
                arrayOf(
                    "Startup clids are empty and stored clids are valid",
                    STORED_VALID_CLIDS,
                    "",
                    STORED_VALID_CLIDS
                ),
                arrayOf(
                    "Startup clids are valid and stored clids are null",
                    null,
                    VALID_CLIDS_FROM_STARTUP,
                    VALID_CLIDS_FROM_STARTUP
                ),
                arrayOf(
                    "Startup clids are valid and stored clids are empty",
                    "",
                    VALID_CLIDS_FROM_STARTUP,
                    VALID_CLIDS_FROM_STARTUP
                ),
                arrayOf(
                    "Startup clids are valid and stored clids are valid",
                    STORED_VALID_CLIDS,
                    VALID_CLIDS_FROM_STARTUP,
                    VALID_CLIDS_FROM_STARTUP
                )
            )

    }

    @Before
    fun setUp() {
        ServerTime.getInstance().init(mock<PreferencesServiceDbStorage>(), timeProvider)
    }

    @Test
    fun checkClids() {
        whenever(startupUnitComponents.startupConfigurationHolder.startupState)
            .thenReturn(
                StartupState.Builder(CollectingFlagsBuilder().build()).withEncodedClidsFromResponse(storedClids).build()
            )
        startupUnit.init()
        clearInvocations(startupStateStorage)
        whenever(startupResult.encodedClids).thenReturn(clidsFromStartup)
        startupUnit.onRequestComplete(startupResult, startupRequestConfig, HashMap())
        verify(startupStateStorage, Mockito.times(1)).save(
            argThat { argument ->
                if (argument == null) {
                    false
                } else {
                    TextUtils.equals(expectedClids, argument.encodedClidsFromResponse)
                }
            }
        )
    }
}
