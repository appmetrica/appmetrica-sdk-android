package io.appmetrica.analytics.testutils.matchers

import org.mockito.ArgumentMatcher

object ArgMatchers {

    @JvmStatic
    fun greaterThat(value: Int): ArgumentMatcher<Int> = ArgumentMatcher<Int> { argument ->
        argument?.let { it > value } ?: false
    }
}
