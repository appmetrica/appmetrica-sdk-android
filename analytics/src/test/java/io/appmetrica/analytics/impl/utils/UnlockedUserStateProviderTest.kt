package io.appmetrica.analytics.impl.utils

import android.os.Build
import android.os.UserManager
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

internal class UnlockedUserStateProviderTest : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule

    @get:Rule
    val androidUtilsRule = staticRule<AndroidUtils> {
        on { AndroidUtils.isApiAchieved(Build.VERSION_CODES.N) }.thenReturn(true)
    }
    private val userManager: UserManager = mock()
    private lateinit var unlockedUserStateProvider: UnlockedUserStateProvider

    @Before
    fun setUp() {
        whenever(context.getSystemService(UserManager::class.java)).thenReturn(userManager)
        unlockedUserStateProvider = UnlockedUserStateProvider()
    }

    @Test
    fun isUserUnlockedForTrue() {
        whenever(userManager.isUserUnlocked).thenReturn(true)
        assertThat(unlockedUserStateProvider.isUserUnlocked(context)).isTrue()
    }

    @Test
    fun isUserUnlockedForFalse() {
        whenever(userManager.isUserUnlocked).thenReturn(false)
        assertThat(unlockedUserStateProvider.isUserUnlocked(context)).isFalse()
    }

    @Test
    fun isUserUnlockedForMissingService() {
        whenever(context.getSystemService(UserManager::class.java)).thenReturn(null)
        assertThat(unlockedUserStateProvider.isUserUnlocked(context)).isTrue()
    }

    @Test
    fun isUserUnlockedIfThrowException() {
        whenever(unlockedUserStateProvider.isUserUnlocked(context))
            .thenThrow(RuntimeException("Some throwable"))
        assertThat(unlockedUserStateProvider.isUserUnlocked(context)).isTrue()
    }

    @Test
    fun isUserUnlockedPreN() {
        whenever(AndroidUtils.isApiAchieved(Build.VERSION_CODES.N)).thenReturn(false)
        Assertions.assertThat(unlockedUserStateProvider.isUserUnlocked(context)).isTrue()
        verifyNoMoreInteractions(context, userManager)
    }
}
