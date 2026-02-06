package io.appmetrica.analytics.impl.utils.encryption

import android.content.Context
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.junit.Before
import org.junit.Rule
import org.mockito.kotlin.whenever

internal open class AESEventEncrypterTestBase : CommonTest() {

    protected lateinit var aesEventEncrypter: AESEventEncrypter

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()
    private lateinit var context: Context

    @Before
    open fun setUp() {
        context = globalServiceLocatorRule.context
        whenever(context.packageName).thenReturn("com.yandex.test.package.name")
    }
}
