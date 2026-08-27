package io.appmetrica.analytics.impl.component.session

import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.concurrent.TimeUnit

internal class SessionFromPastFactoryTest : CommonTest() {

    private val sessionFromPastId = 42L
    private val creationTimestamp = 1700000000000L
    private val creationElapsedRealtime = 100000L
    private val sessionRequestParams: SessionRequestParams = mock()

    private val componentPreferences: PreferencesComponentDbStorage = mock()
    private val dbHelper: DatabaseHelper = mock()
    private val reportRequestConfig: ReportRequestConfig = mock()
    private val componentUnit: ComponentUnit = mock {
        on { componentPreferences } doReturn componentPreferences
        on { dbHelper } doReturn dbHelper
        on { freshReportRequestConfig } doReturn reportRequestConfig
    }
    private val sessionIDProvider: SessionIDProvider = mock {
        on { getNextSessionId() } doReturn sessionFromPastId
    }

    @get:Rule
    val sessionStorageRule = constructionRule<SessionStorageImpl> { mock ->
        on { putSessionId(sessionFromPastId) } doReturn mock
        on { putSleepStart(creationElapsedRealtime) } doReturn mock
        on { putCreationTime(creationElapsedRealtime) } doReturn mock
        on { putCreationCurrentTimeMillis(creationTimestamp) } doReturn mock
        on { putReportId(SessionDefaults.INITIAL_REPORT_ID) } doReturn mock
        on { putAliveReportNeeded(false) } doReturn mock
    }
    private val sessionStorage: SessionStorageImpl by sessionStorageRule

    private val arguments = SessionArguments(creationElapsedRealtime, creationTimestamp, sessionRequestParams)

    private lateinit var factory: SessionFromPastFactory

    @Before
    fun setUp() {
        factory = SessionFromPastFactory(componentUnit, sessionIDProvider)
    }

    @Test
    fun `storage constructed with component preferences and background tag`() {
        assertThat(sessionStorageRule.argumentInterceptor.flatArguments())
            .containsExactly(componentPreferences, BackgroundSessionFactory.SESSION_TAG)
    }

    @Test
    fun `load returns null`() {
        assertThat(factory.load()).isNull()
    }

    @Test
    fun `create - returns session with background type and correct timeout`() {
        val session = factory.create(arguments)
        assertThat(session.type).isEqualTo(SessionType.BACKGROUND)
        assertThat(session.timeoutSeconds).isEqualTo(BackgroundSessionFactory.SESSION_TIMEOUT_SEC)
    }

    @Test
    fun `create - calls db and writes storage in correct order`() {
        factory.create(arguments)

        verify(sessionIDProvider).getNextSessionId()
        verify(dbHelper).newSessionFromPast(
            sessionFromPastId,
            SessionType.BACKGROUND,
            TimeUnit.MILLISECONDS.toSeconds(creationTimestamp),
            sessionRequestParams
        )

        verify(sessionStorage).putSessionId(sessionFromPastId)
        verify(sessionStorage).putSleepStart(creationElapsedRealtime)
        verify(sessionStorage).putCreationTime(creationElapsedRealtime)
        verify(sessionStorage).putCreationCurrentTimeMillis(creationTimestamp)
        verify(sessionStorage).putReportId(SessionDefaults.INITIAL_REPORT_ID)
        verify(sessionStorage).putAliveReportNeeded(false)
        verify(sessionStorage).apply()
    }
}
