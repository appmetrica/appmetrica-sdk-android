package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ServiceComponentModuleConfigImplTest : CommonTest() {

    @Test
    fun isRevenueAutoTrackingEnabledIfTrue() {
        val reporterArguments = CommonArguments.ReporterArguments(
            /* apiKey */ null,
            /* locationTracking */ null,
            /* manualLocation */ null,
            /* firstActivationAsUpdate */ null,
            /* sessionTimeout */ null,
            /* maxReportsCount */ null,
            /* dispatchPeriod */ null,
            /* logEnabled */ null,
            /* dataSendingEnabled */ null,
            /* clidsFromClient */ null,
            /* maxReportsInDbCount */ null,
            /* nativeCrashesEnabled */ null,
            /* revenueAutoTrackingEnabled */ true,
            /* advIdentifiersTrackingEnabled */ null,
            /* autoCollectedDataSubscribers */ emptySet<String>()
        )
        val configImpl = ServiceComponentModuleConfigImpl(
            config = reporterArguments
        )
        assertThat(configImpl.isRevenueAutoTrackingEnabled()).isTrue
    }

    @Test
    fun isRevenueAutoTrackingEnabledIfFalse() {
        val reporterArguments = CommonArguments.ReporterArguments(
            /* apiKey */ null,
            /* locationTracking */ null,
            /* manualLocation */ null,
            /* firstActivationAsUpdate */ null,
            /* sessionTimeout */ null,
            /* maxReportsCount */ null,
            /* dispatchPeriod */ null,
            /* logEnabled */ null,
            /* dataSendingEnabled */ null,
            /* clidsFromClient */ null,
            /* maxReportsInDbCount */ null,
            /* nativeCrashesEnabled */ null,
            /* revenueAutoTrackingEnabled */ false,
            /* advIdentifiersTrackingEnabled */ null,
            /* autoCollectedDataSubscribers */ emptySet<String>()
        )
        val configImpl = ServiceComponentModuleConfigImpl(
            config = reporterArguments
        )
        assertThat(configImpl.isRevenueAutoTrackingEnabled()).isFalse
    }

    @Test
    fun isRevenueAutoTrackingEnabledIfDefault() {
        val reporterArguments = CommonArguments.ReporterArguments(
            /* apiKey */ null,
            /* locationTracking */ null,
            /* manualLocation */ null,
            /* firstActivationAsUpdate */ null,
            /* sessionTimeout */ null,
            /* maxReportsCount */ null,
            /* dispatchPeriod */ null,
            /* logEnabled */ null,
            /* dataSendingEnabled */ null,
            /* clidsFromClient */ null,
            /* maxReportsInDbCount */ null,
            /* nativeCrashesEnabled */ null,
            /* revenueAutoTrackingEnabled */ null,
            /* advIdentifiersTrackingEnabled */ null,
            /* autoCollectedDataSubscribers */ emptySet<String>()
        )
        val configImpl = ServiceComponentModuleConfigImpl(
            config = reporterArguments
        )
        assertThat(configImpl.isRevenueAutoTrackingEnabled()).isTrue
    }
}
