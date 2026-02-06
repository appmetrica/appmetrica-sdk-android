package io.appmetrica.analytics.impl.utils.encryption

import android.content.Context
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.junit.Before
import org.junit.Rule
import org.mockito.kotlin.whenever

internal open class EventEncrypterProviderBaseTest : CommonTest() {

    private val packageName = "com.yandex.test.package.name"

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    protected lateinit var context: Context
    protected lateinit var dummyEventEncrypter: EventEncrypter
    protected lateinit var aesRsaWithDecryptionOnBackendEncrypter: EventEncrypter
    protected lateinit var aesEncrypter: EventEncrypter
    protected lateinit var eventEncrypterProvider: EventEncrypterProvider

    @Before
    open fun setUp() {
        context = globalServiceLocatorRule.context
        whenever(context.packageName).thenReturn(packageName)
        dummyEventEncrypter = DummyEventEncrypter()
        aesRsaWithDecryptionOnBackendEncrypter = ExternallyEncryptedEventCrypter()
        aesEncrypter = AESEventEncrypter()

        eventEncrypterProvider = EventEncrypterProvider(
            dummyEventEncrypter,
            aesRsaWithDecryptionOnBackendEncrypter,
            aesEncrypter
        )
    }
}
