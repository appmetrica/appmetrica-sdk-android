package io.appmetrica.analytics.testutils

import org.junit.Before
import org.junit.Rule
import org.junit.rules.DisableOnDebug
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit

open class CommonTest {

    @Rule
    @JvmField
    val testTimeoutRule = DisableOnDebug(Timeout(30, TimeUnit.SECONDS))

    @Rule
    @JvmField
    val printExecutorRule = PrintExecutorRule()

    private val setUpDelegates = mutableListOf<SetUpDelegate<*>>()

    fun <T : Any> setUp(block: () -> T): SetUpDelegate<T> = SetUpDelegate(block).also(setUpDelegates::add)

    @Before
    fun initSetUpDelegates() {
        for (delegate in setUpDelegates) {
            delegate.setUp()
        }
    }
}
