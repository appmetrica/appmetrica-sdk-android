package io.appmetrica.analytics.testutils

import org.junit.rules.ExternalResource
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing
import kotlin.reflect.KProperty

class MockedStaticRule<T>(
    private val classToMock: Class<T>,
    private val initializer: MockedStatic<T>.() -> Unit,
) : ExternalResource() {
    constructor(classToMock: Class<T>) : this(classToMock, {})

    lateinit var staticMock: MockedStatic<T>
        private set

    @Throws(Throwable::class)
    override fun before() {
        super.before()
        staticMock = Mockito.mockStatic(classToMock)
        initializer(staticMock)
    }

    override fun after() {
        super.after()
        if (this::staticMock.isInitialized) {
            staticMock.close()
        }
    }

    operator fun <C> getValue(thisRef: C, property: KProperty<*>): MockedStatic<T> {
        return staticMock
    }
}

inline fun <reified T> staticRule(noinline initializer: MockedStatic<T>.() -> Unit = {}) =
    MockedStaticRule(T::class.java, initializer)

fun <T, R> MockedStatic<T>.on(methodCall: () -> R): OngoingStubbing<R> {
    return `when` { methodCall() }
}
