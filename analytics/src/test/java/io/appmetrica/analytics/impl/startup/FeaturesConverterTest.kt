package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.junit.Test

internal class FeaturesConverterTest : CommonTest() {

    private val featuresConverter = FeaturesConverter()

    @Test
    fun convertFilled() {
        val input = FeaturesInternal(false, IdentifierStatus.OK, "some error")
        val result = featuresConverter.convert(input)
        ObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .checkField("libSslEnabled", "getLibSslEnabled", false)
            .checkAll()
    }

    @Test
    fun convertDefault() {
        val input = FeaturesInternal(null, IdentifierStatus.UNKNOWN, null)
        val result = featuresConverter.convert(input)
        ObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .checkFieldIsNull("libSslEnabled", "getLibSslEnabled")
            .checkAll()
    }
}
