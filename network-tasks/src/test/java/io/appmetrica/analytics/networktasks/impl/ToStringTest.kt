package io.appmetrica.analytics.networktasks.impl

import io.appmetrica.analytics.networktasks.internal.DefaultResponseParser
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig
import io.appmetrica.analytics.testutils.BaseToStringTest
import io.appmetrica.analytics.testutils.BaseToStringTest.Companion.toTestCase
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.lang.reflect.Modifier

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
            RetryPolicyConfig(66, 77).toTestCase(Modifier.PRIVATE or Modifier.FINAL),
            DefaultResponseParser.Response("my status").toTestCase(Modifier.PUBLIC or Modifier.FINAL)
        )
    }
}
