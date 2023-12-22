package io.appmetrica.analytics.impl.attribution

import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution.ClientExternalAttribution
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExternalAttributionTypeConverterTest : CommonTest() {

    @Test
    fun fromModel() {
        assertThat(ExternalAttributionTypeConverter.fromModel(ExternalAttributionType.UNKNOWN))
            .isEqualTo(ClientExternalAttribution.UNKNOWN)
        assertThat(ExternalAttributionTypeConverter.fromModel(ExternalAttributionType.APPSFLYER))
            .isEqualTo(ClientExternalAttribution.APPSFLYER)
        assertThat(ExternalAttributionTypeConverter.fromModel(ExternalAttributionType.ADJUST))
            .isEqualTo(ClientExternalAttribution.ADJUST)
        assertThat(ExternalAttributionTypeConverter.fromModel(ExternalAttributionType.KOCHAVA))
            .isEqualTo(ClientExternalAttribution.KOCHAVA)
        assertThat(ExternalAttributionTypeConverter.fromModel(ExternalAttributionType.TENJIN))
            .isEqualTo(ClientExternalAttribution.TENJIN)
        assertThat(ExternalAttributionTypeConverter.fromModel(ExternalAttributionType.AIRBRIDGE))
            .isEqualTo(ClientExternalAttribution.AIRBRIDGE)
    }

    @Test
    fun toStringTest() {
        assertThat(ExternalAttributionTypeConverter.toString(ClientExternalAttribution.UNKNOWN))
            .isEqualTo("UNKNOWN")
        assertThat(ExternalAttributionTypeConverter.toString(ClientExternalAttribution.APPSFLYER))
            .isEqualTo("APPSFLYER")
        assertThat(ExternalAttributionTypeConverter.toString(ClientExternalAttribution.ADJUST))
            .isEqualTo("ADJUST")
        assertThat(ExternalAttributionTypeConverter.toString(ClientExternalAttribution.KOCHAVA))
            .isEqualTo("KOCHAVA")
        assertThat(ExternalAttributionTypeConverter.toString(ClientExternalAttribution.TENJIN))
            .isEqualTo("TENJIN")
        assertThat(ExternalAttributionTypeConverter.toString(ClientExternalAttribution.AIRBRIDGE))
            .isEqualTo("AIRBRIDGE")
    }
}
