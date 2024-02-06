package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.impl.component.session.BackgroundSessionFactory
import io.appmetrica.analytics.impl.component.session.ForegroundSessionFactory
import io.appmetrica.analytics.impl.component.session.SessionStorageImpl
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

internal class ComponentMigrationToV113Test : CommonTest() {

    private val componentPreferences: PreferencesComponentDbStorage = mock()

    private val componentUnit: ComponentUnit = mock {
        on { componentPreferences } doReturn componentPreferences
    }

    private val backgroundSleepStart = 200500L
    private val foregroundSleepStart = 100500L
    private val backgroundLastEventOffset = 300500L
    private val foregroundLastEventOffset = 400500L

    @get:Rule
    val sessionStorageImplMockedConstructionRule =
        MockedConstructionRule(SessionStorageImpl::class.java) { mock, context ->
            val arguments = context.arguments()
            if (arguments.containsAll(listOf(componentPreferences, BackgroundSessionFactory.SESSION_TAG))) {
                whenever(mock.sleepStart).thenReturn(backgroundSleepStart)
                whenever(mock.lastEventOffset).thenReturn(backgroundLastEventOffset)
            } else if (arguments.containsAll(listOf(componentPreferences, ForegroundSessionFactory.SESSION_TAG))) {
                whenever(mock.sleepStart).thenReturn(foregroundSleepStart)
                whenever(mock.lastEventOffset).thenReturn(foregroundLastEventOffset)
            }
        }

    private val migration: ComponentMigrationToV113 by setUp { ComponentMigrationToV113(componentUnit) }

    @Test
    fun sessionStorages() {
        migration.run(1)
        assertThat(sessionStorageImplMockedConstructionRule.constructionMock.constructed()).hasSize(2)
        assertThat(sessionStorageImplMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(
                componentPreferences, BackgroundSessionFactory.SESSION_TAG,
                componentPreferences, ForegroundSessionFactory.SESSION_TAG)
    }

    @Test
    fun `checkMigration if should not migrate`() {
        migration.run(100500)
        assertThat(sessionStorageImplMockedConstructionRule.constructionMock.constructed()).isEmpty()
    }

    @Test
    fun checkMigration() {
        migration.run(1)

        val backgroundSessionStorage = sessionStorageImplMockedConstructionRule.constructionMock.constructed().first()
        val foregroundSessionStorage = sessionStorageImplMockedConstructionRule.constructionMock.constructed()[1]

        verify(backgroundSessionStorage).putSleepStart(TimeUnit.SECONDS.toMillis(backgroundSleepStart))
        verify(backgroundSessionStorage).putLastEventOffset(TimeUnit.SECONDS.toMillis(backgroundLastEventOffset))
        verify(foregroundSessionStorage).putSleepStart(TimeUnit.SECONDS.toMillis(foregroundSleepStart))
        verify(foregroundSessionStorage).putLastEventOffset(TimeUnit.SECONDS.toMillis(foregroundLastEventOffset))
    }
}
