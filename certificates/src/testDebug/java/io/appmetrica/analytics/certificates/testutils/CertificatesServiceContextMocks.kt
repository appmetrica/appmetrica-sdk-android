package io.appmetrica.analytics.certificates.testutils

import android.content.Context
import io.appmetrica.analytics.modulesapi.internal.common.ExecutorProvider
import io.appmetrica.analytics.modulesapi.internal.network.SimpleNetworkApi
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.modulesapi.internal.service.ServiceNetworkContext
import io.appmetrica.analytics.modulesapi.internal.service.ServiceStorageProvider
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal object CertificatesServiceContextMocks {

    fun serviceContext(): ServiceContext {
        val context: Context = mock()
        val networkApi: SimpleNetworkApi = mock()
        val networkContext: ServiceNetworkContext = mock {
            on { this.networkApi } doReturn networkApi
        }
        val executorProvider: ExecutorProvider = mock {
            on { getSupportIOExecutor() } doReturn mock()
        }
        val storageProvider: ServiceStorageProvider = mock()
        return mock {
            on { this.context } doReturn context
            on { this.networkContext } doReturn networkContext
            on { this.executorProvider } doReturn executorProvider
            on { this.serviceStorageProvider } doReturn storageProvider
        }
    }
}
