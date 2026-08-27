package io.appmetrica.analytics.impl.proxy

import android.content.Context
import io.appmetrica.analytics.impl.AppMetricaFacade
import io.appmetrica.analytics.impl.DeeplinkConsumer
import io.appmetrica.analytics.impl.MainReporter
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider
import io.appmetrica.analytics.impl.proxy.validation.Barrier
import io.appmetrica.gradle.testutils.CommonTest
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.lang.reflect.Modifier

internal abstract class BaseAppMetricaProxyBarrierTests(
    protected val name: String,
    protected val ifNoArgs: Boolean,
    protected val args: Array<Class<*>>
) : CommonTest() {

    private val mainReporter: MainReporter = mock()
    private val mainReporterApiConsumerProvider: MainReporterApiConsumerProvider = mock {
        on { mainReporter } doReturn mainReporter
        on { deeplinkConsumer } doReturn mock<DeeplinkConsumer>()
    }
    private val impl: AppMetricaFacade = mock {
        on { mainReporterApiConsumerProvider } doReturn mainReporterApiConsumerProvider
    }
    protected val provider: AppMetricaFacadeProvider = mock {
        on { peekInitializedImpl() } doReturn impl
        on { getInitializedImpl(any<Context>()) } doReturn impl
    }
    protected abstract val proxy: BaseAppMetricaProxy
    protected abstract val barrier: Barrier

    fun verifyBarrierCall() {
        if (ifNoArgs) {
            CallProxyVerifier.verifyNoArgsForMock(proxy, barrier, name, args)
        } else {
            CallProxyVerifier.verify(proxy, barrier, name, args)
        }
    }

    companion object {

        fun data(
            methodsNotToCheck: List<String>,
            methodsWithNoArguments: List<String>,
            proxyClass: Class<*>
        ): Collection<Array<Any>> = proxyClass.declaredMethods
            .asSequence()
            .filter { Modifier.isPublic(it.modifiers) && !it.isSynthetic && !it.isBridge }
            .filterNot { it.name == "\$jacocoInit" }
            .filterNot { it.name in methodsNotToCheck }
            .map { method ->
                arrayOf<Any>(method.name, method.name in methodsWithNoArguments, method.parameterTypes)
            }
            .toList()
    }
}
