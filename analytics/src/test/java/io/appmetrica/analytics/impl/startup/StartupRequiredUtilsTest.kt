package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing

class StartupRequiredUtilsTest : CommonTest() {

    private val timeProvider = mock<TimeProvider>()
    private val clidsStateChecker = mock<ClidsStateChecker>()

    @Before
    fun setUp() {
        StartupRequiredUtils.timeProvider = timeProvider
        StartupRequiredUtils.clidsStateChecker = clidsStateChecker
    }

    @After
    fun tearDown() {
        StartupRequiredUtils.timeProvider = SystemTimeProvider()
        StartupRequiredUtils.clidsStateChecker = ClidsStateChecker()
    }

    @Test
    fun isOutdatedFlagIsTrueCurrentTimeIsGreater() {
        val startupObtainTime = 666777L
        val updateInterval = 100
        val startup = mock<StartupState> {
            on { outdated } doReturn true
            on { obtainTime } doReturn startupObtainTime
            on { startupUpdateConfig } doReturn StartupUpdateConfig(updateInterval)
        }
        stubbing(timeProvider) {
            on { currentTimeSeconds() } doReturn startupObtainTime + updateInterval + 1
        }
        assertThat(StartupRequiredUtils.isOutdated(startup)).isTrue
    }

    @Test
    fun isOutdatedFlagIsTrueCurrentTimeIsLess() {
        val startupObtainTime = 666777L
        val updateInterval = 200
        val startup = mock<StartupState> {
            on { outdated } doReturn true
            on { obtainTime } doReturn startupObtainTime
            on { startupUpdateConfig } doReturn StartupUpdateConfig(updateInterval)
        }
        stubbing(timeProvider) {
            on { currentTimeSeconds() } doReturn startupObtainTime + updateInterval - 1
        }
        assertThat(StartupRequiredUtils.isOutdated(startup)).isTrue
    }

    @Test
    fun isOutdatedFlagIsFalseCurrentTimeIsLess() {
        val startupObtainTime = 666777L
        val updateInterval = 150
        val startup = mock<StartupState> {
            on { outdated } doReturn false
            on { obtainTime } doReturn startupObtainTime
            on { startupUpdateConfig } doReturn StartupUpdateConfig(updateInterval)
        }
        stubbing(timeProvider) {
            on { currentTimeSeconds() } doReturn startupObtainTime + updateInterval - 1
        }
        assertThat(StartupRequiredUtils.isOutdated(startup)).isFalse
    }

    @Test
    fun isOutdatedFlagIsFalseCurrentTimeIsGreater() {
        val startupObtainTime = 666777L
        val updateInterval = 780
        val startup = mock<StartupState> {
            on { outdated } doReturn false
            on { obtainTime } doReturn startupObtainTime
            on { startupUpdateConfig } doReturn StartupUpdateConfig(updateInterval)
        }
        stubbing(timeProvider) {
            on { currentTimeSeconds() } doReturn startupObtainTime + updateInterval + 1
        }
        assertThat(StartupRequiredUtils.isOutdated(startup)).isTrue
    }

    @Test
    fun isOutdatedFlagIsFalseCurrentTimeIsTheSame() {
        val startupObtainTime = 666777L
        val updateInterval = 340
        val startup = mock<StartupState> {
            on { outdated } doReturn false
            on { obtainTime } doReturn startupObtainTime
            on { startupUpdateConfig } doReturn StartupUpdateConfig(updateInterval)
        }
        stubbing(timeProvider) {
            on { currentTimeSeconds() } doReturn startupObtainTime + updateInterval
        }
        assertThat(StartupRequiredUtils.isOutdated(startup)).isFalse
    }

    @Test
    fun isOutdatedByTimeCurrentTimeIsGreater() {
        val nextStartupTime = 666777L
        stubbing(timeProvider) {
            on { currentTimeSeconds() } doReturn nextStartupTime + 1
        }
        assertThat(StartupRequiredUtils.isOutdated(nextStartupTime)).isTrue
    }

    @Test
    fun isOutdatedByTimeOnlyCurrentTimeIsLess() {
        val nextStartupTime = 666777L
        stubbing(timeProvider) {
            on { currentTimeSeconds() } doReturn nextStartupTime - 1
        }
        assertThat(StartupRequiredUtils.isOutdated(nextStartupTime)).isFalse
    }

    @Test
    fun isOutdatedByTimeOnlyCurrentTimeIsTheSame() {
        val nextStartupTime = 666777L
        stubbing(timeProvider) {
            on { currentTimeSeconds() } doReturn nextStartupTime
        }
        assertThat(StartupRequiredUtils.isOutdated(nextStartupTime)).isFalse
    }

    @Test
    fun areMainIdentifiersEmptyAllEmpty() {
        val startup = mock<StartupState>()
        assertThat(StartupRequiredUtils.areMainIdentifiersValid(startup)).isFalse
    }

    @Test
    fun areMainIdentifiersEmptyUuidIsEmpty() {
        val startup = mock<StartupState> {
            on { uuid } doReturn ""
            on { deviceId } doReturn "device_id"
            on { deviceIdHash } doReturn "device_id_hash"
        }
        assertThat(StartupRequiredUtils.areMainIdentifiersValid(startup)).isFalse
    }

    @Test
    fun areMainIdentifiersEmptyDeviceIdIsEmpty() {
        val startup = mock<StartupState> {
            on { uuid } doReturn "uuid"
            on { deviceId } doReturn ""
            on { deviceIdHash } doReturn "device_id_hash"
        }
        assertThat(StartupRequiredUtils.areMainIdentifiersValid(startup)).isFalse
    }

    @Test
    fun areMainIdentifiersEmptyDeviceIdHashIsEmpty() {
        val startup = mock<StartupState> {
            on { uuid } doReturn "uuid"
            on { deviceId } doReturn "device_id"
            on { deviceIdHash } doReturn ""
        }
        assertThat(StartupRequiredUtils.areMainIdentifiersValid(startup)).isFalse
    }

    @Test
    fun areMainIdentifiersEmptyAllFilled() {
        val startup = mock<StartupState> {
            on { uuid } doReturn "uuid"
            on { deviceId } doReturn "device_id"
            on { deviceIdHash } doReturn "device_id_hash"
        }
        assertThat(StartupRequiredUtils.areMainIdentifiersValid(startup)).isTrue
    }

    @Test
    fun isIdentifierValidNull() {
        assertThat(StartupRequiredUtils.isIdentifierValid(null)).isFalse
    }

    @Test
    fun isIdentifierValidEmpty() {
        assertThat(StartupRequiredUtils.isIdentifierValid("")).isFalse
    }

    @Test
    fun isIdentifierValidFilled() {
        assertThat(StartupRequiredUtils.isIdentifierValid("a")).isTrue
    }
}
