package io.appmetrica.analytics.location.impl.gpl

import android.content.Context
import android.location.LocationListener
import android.os.Looper
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.gpllibrary.internal.GplLibraryWrapper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

class GplWrapperFactoryTest : CommonTest() {

    private val context = mock<Context>()
    private val brokenContext = mock<Context>()
    private val locationListener = mock<LocationListener>()
    private val looper = mock<Looper>()
    private val executor = mock<IHandlerExecutor> {
        on { looper } doReturn looper
    }

    private val className = "com.google.android.gms.location.LocationRequest"

    @get:Rule
    val reflectionUtilsMockedRule = MockedStaticRule(ReflectionUtils::class.java)

    @get:Rule
    val gplLibraryWrapperMockedConstructionRule =
        MockedConstructionRule(GplLibraryWrapper::class.java) { mock, mockedContext ->
            if (mockedContext.arguments().first() === brokenContext) {
                throw RuntimeException()
            }
        }

    private lateinit var gplWrapperFactory: GplWrapperFactory

    @Before
    fun setUp() {
        gplWrapperFactory = GplWrapperFactory()
        whenever(ReflectionUtils.detectClassExists(className)).thenReturn(true)
    }

    @Test
    fun create() {
        val result = gplWrapperFactory.create(context, locationListener, executor)
        assertThat(result).isEqualTo(singleGplWrapper())
    }

    @Test
    fun `create() if does not detect class`() {
        whenever(ReflectionUtils.detectClassExists(className)).thenReturn(false)
        assertThat(gplWrapperFactory.create(context, locationListener, executor))
            .isInstanceOf(DummyGplLibraryWrapper::class.java)
    }

    @Test
    fun `create() if throw exception`() {
        assertThat(gplWrapperFactory.create(brokenContext, locationListener, executor))
            .isInstanceOf(DummyGplLibraryWrapper::class.java)
    }

    private fun singleGplWrapper(): GplLibraryWrapper {
        assertThat(gplLibraryWrapperMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(gplLibraryWrapperMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, locationListener, looper, executor, TimeUnit.SECONDS.toMillis(1))
        return gplLibraryWrapperMockedConstructionRule.constructionMock.constructed().first()
    }
}
