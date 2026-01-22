package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock

internal class ThreadUncaughtExceptionHandlerInstallerTest : CommonTest() {

    private val handler = mock<Thread.UncaughtExceptionHandler>()

    @get:Rule
    val handlersCompositeConstructionRule = constructionRule<ThreadUncaughtExceptionHandlerComposite>()
    private val handlersComposite by handlersCompositeConstructionRule

    private val installer by setUp { ThreadUncaughtExceptionHandlerInstaller(handler) }

    @Test
    fun install() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        installer.install()
        assertThat(Thread.getDefaultUncaughtExceptionHandler()).isEqualTo(handlersComposite)
        inOrder(handlersComposite) {
            verify(handlersComposite).register(handler)
            verify(handlersComposite).register(defaultHandler)
        }
    }
}
