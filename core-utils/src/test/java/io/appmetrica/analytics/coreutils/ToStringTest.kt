package io.appmetrica.analytics.coreutils

import io.appmetrica.analytics.coreutils.internal.cache.CachedDataProvider
import io.appmetrica.analytics.testutils.BaseToStringTest
import io.appmetrica.analytics.testutils.BaseToStringTest.Companion.toTestCase
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class ToStringTest(
    actualValue: Any?,
    modifierPreconditions: Int,
    excludedFields: Set<String>?,
    additionalDescription: String?
) : BaseToStringTest(
    actualValue,
    modifierPreconditions,
    excludedFields,
    additionalDescription
) {

    companion object {

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            CachedDataProvider.CachedData<Any>(10L, 20L, "some description")
                .toTestCase(excludedFields = setOf("timeProvider"))
        )
    }
}
