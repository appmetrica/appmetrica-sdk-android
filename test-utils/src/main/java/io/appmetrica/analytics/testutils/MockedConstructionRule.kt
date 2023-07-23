package io.appmetrica.analytics.testutils

import org.junit.rules.ExternalResource
import org.mockito.MockedConstruction
import org.mockito.Mockito
import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.stubbing
import kotlin.reflect.KProperty

class MockedConstructionRule<T>(
    private val clazz: Class<T>,
    initializer: MockedConstruction.MockInitializer<T>? = null
) : ExternalResource() {
    constructor(clazz: Class<T>) : this(clazz, null)

    lateinit var constructionMock: MockedConstruction<T>
        private set
    val argumentInterceptor: ConstructionArgumentCaptor<T> = ConstructionArgumentCaptor(initializer)

    @Throws(Throwable::class)
    override fun before() {
        super.before()
        constructionMock = Mockito.mockConstruction(clazz, argumentInterceptor)
    }

    override fun after() {
        super.after()
        if (this::constructionMock.isInitialized) {
            constructionMock.close()
        }
    }

    operator fun <C> getValue(thisRef: C, property: KProperty<*>): T {
        return constructionMock.constructed().single()
    }
}

operator fun <C, T> ConstructionArgumentCaptor<T>.getValue(thisRef: C, property: KProperty<*>): List<Any> {
    return arguments.single()
}

inline fun <reified T> constructionRule(noinline initializer: KStubbing<T>.(T) -> Unit = {}) =
    MockedConstructionRule(T::class.java) { mock, _ -> stubbing(mock, initializer) }
