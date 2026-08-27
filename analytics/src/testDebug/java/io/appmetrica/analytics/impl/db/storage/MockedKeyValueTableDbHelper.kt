package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.impl.db.DatabaseStorage
import io.appmetrica.analytics.impl.db.connectors.SimpleDBConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer

internal open class MockedKeyValueTableDbHelper @JvmOverloads constructor(
    dbStorage: DatabaseStorage? = null,
    executor: IHandlerExecutor = mock(IHandlerExecutor::class.java).also { mockExecutor ->
        // Configure executor to run tasks synchronously
        doAnswer(
            Answer { invocation: InvocationOnMock ->
                val runnable = invocation.getArgument<Runnable>(0)
                runnable.run()
                null
            }
        ).whenever(mockExecutor).execute(any(Runnable::class.java))

        doAnswer(
            Answer { invocation: InvocationOnMock ->
                val runnable = invocation.getArgument<Runnable>(0)
                runnable.run()
                null
            }
        ).whenever(mockExecutor).executeDelayed(any(Runnable::class.java), any(Long::class.java))
    }
) : KeyValueTableDbHelper(
    tableName = "test_table",
    dbConnector = SimpleDBConnector(dbStorage ?: mock(DatabaseStorage::class.java)),
    executor = executor
)
