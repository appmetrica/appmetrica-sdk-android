package io.appmetrica.analytics

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AppMetricaDefaultValuesTest : CommonTest() {

    @Test
    fun defaultReportLocationEnabled() {
        assertThat(AppMetricaDefaultValues.DEFAULT_REPORT_LOCATION_ENABLED).isFalse
    }
}
