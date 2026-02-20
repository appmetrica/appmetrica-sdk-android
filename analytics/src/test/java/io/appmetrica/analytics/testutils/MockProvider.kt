package io.appmetrica.analytics.testutils

import android.content.ContentValues
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import org.mockito.stubbing.Answer
import java.util.concurrent.Callable
import java.util.concurrent.Future

object MockProvider {

    val blockingRunnableAnswer = Answer {
        (it.arguments.first() as Runnable).run()
    }

    val callableAnswer = Answer<Future<*>> {
        val callable = it.arguments.first() as Callable<*>
        val result = callable.call()
        mock<Future<Any?>> {
            on { get() } doReturn result
            on { isDone } doReturn true
        }
    }

    val mockedLooper: Looper = mock()
    val mockedBlockingHandler = mockedBlockingHandler()

    @JvmStatic
    fun mockedBlockingExecutorMock() = mock<IHandlerExecutor> {
        on { handler } doReturn mockedBlockingHandler
        on { looper } doReturn mockedLooper
        on { execute(any()) } doAnswer blockingRunnableAnswer
        on { executeDelayed(any(), any()) } doAnswer blockingRunnableAnswer
        on { submit(any<Callable<*>>()) } doAnswer callableAnswer
    }

    @JvmStatic
    fun mockedBlockingHandler() = mock<Handler> {
        on { post(any()) } doAnswer blockingRunnableAnswer
        on { postDelayed(any(), any()) } doAnswer blockingRunnableAnswer
    }

    @JvmStatic
    fun mockedLocation(lat: Double, lon: Double) = mock<Location> {
        on { latitude } doReturn lat
        on { longitude } doReturn lon
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun stubContentValues(contentValues: ContentValues, dataMap: MutableMap<String, Any?>) {
        // Use Java helper to avoid Kotlin overload resolution ambiguity with put()
        ContentValuesStubHelper.stubPutMethod(contentValues, dataMap as MutableMap<String, Any>)

        stubbing(contentValues) {
            on { getAsString(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap[key] as? String
            }
            on { getAsInteger(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap[key] as? Int
            }
            on { getAsLong(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap[key] as? Long
            }
            on { getAsBoolean(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap[key] as? Boolean
            }
            on { getAsDouble(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap[key] as? Double
            }
            on { getAsFloat(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap[key] as? Float
            }
            on { getAsByteArray(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap[key] as? ByteArray
            }
            on { containsKey(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap.containsKey(key)
            }
            on { size() } doAnswer {
                dataMap.size
            }
            on { keySet() } doAnswer {
                dataMap.keys
            }
            on { remove(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap.remove(key)
                null
            }
            on { clear() } doAnswer {
                dataMap.clear()
                null
            }
        }
    }

    @JvmStatic
    fun mockedContentValues(): ContentValues {
        val dataMap = mutableMapOf<String, Any?>()
        val contentValues = mock<ContentValues>()
        stubContentValues(contentValues, dataMap)
        return contentValues
    }

    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    @JvmStatic
    fun stubBundle(bundle: Bundle, dataMap: MutableMap<String, Any?>) {
        stubbing(bundle) {
            // put methods
            on { putString(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val value = it.arguments[1] as String?
                dataMap[key] = value
                null
            }
            on { putInt(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val value = it.arguments[1] as Int
                dataMap[key] = value
                null
            }
            on { putLong(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val value = it.arguments[1] as Long
                dataMap[key] = value
                null
            }
            on { putBoolean(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val value = it.arguments[1] as Boolean
                dataMap[key] = value
                null
            }
            on { putDouble(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val value = it.arguments[1] as Double
                dataMap[key] = value
                null
            }
            on { putFloat(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val value = it.arguments[1] as Float
                dataMap[key] = value
                null
            }
            on { putParcelable(any(), anyOrNull()) } doAnswer {
                val key = it.arguments[0] as String
                val value = it.arguments[1] as Parcelable?
                dataMap[key] = value
                null
            }
            on { putSerializable(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val value = it.arguments[1]
                dataMap[key] = value
                null
            }
            on { putBundle(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val value = it.arguments[1] as Bundle?
                dataMap[key] = value
                null
            }
            on { putStringArrayList(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val value = it.arguments[1] as ArrayList<String>?
                dataMap[key] = value
                null
            }

            // get methods
            on { getString(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap[key] as? String
            }
            on { getString(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val defaultValue = it.arguments[1] as String?
                (dataMap[key] as? String) ?: defaultValue
            }
            on { getInt(any()) } doAnswer {
                val key = it.arguments[0] as String
                (dataMap[key] as? Int) ?: 0
            }
            on { getInt(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val defaultValue = it.arguments[1] as Int
                (dataMap[key] as? Int) ?: defaultValue
            }
            on { getLong(any()) } doAnswer {
                val key = it.arguments[0] as String
                (dataMap[key] as? Long) ?: 0L
            }
            on { getLong(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val defaultValue = it.arguments[1] as Long
                (dataMap[key] as? Long) ?: defaultValue
            }
            on { getBoolean(any()) } doAnswer {
                val key = it.arguments[0] as String
                (dataMap[key] as? Boolean) ?: false
            }
            on { getBoolean(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val defaultValue = it.arguments[1] as Boolean
                (dataMap[key] as? Boolean) ?: defaultValue
            }
            on { getDouble(any()) } doAnswer {
                val key = it.arguments[0] as String
                (dataMap[key] as? Double) ?: 0.0
            }
            on { getDouble(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val defaultValue = it.arguments[1] as Double
                (dataMap[key] as? Double) ?: defaultValue
            }
            on { getFloat(any()) } doAnswer {
                val key = it.arguments[0] as String
                (dataMap[key] as? Float) ?: 0f
            }
            on { getFloat(any(), any()) } doAnswer {
                val key = it.arguments[0] as String
                val defaultValue = it.arguments[1] as Float
                (dataMap[key] as? Float) ?: defaultValue
            }
            on { getParcelable<Parcelable>(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap[key] as? Parcelable
            }
            on { getSerializable(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap[key] as? java.io.Serializable
            }
            on { getBundle(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap[key] as? Bundle
            }
            on { getStringArrayList(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap[key] as? ArrayList<String>
            }

            // utility methods
            on { containsKey(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap.containsKey(key)
            }
            on { keySet() } doAnswer {
                dataMap.keys
            }
            on { size() } doAnswer {
                dataMap.size
            }
            on { remove(any()) } doAnswer {
                val key = it.arguments[0] as String
                dataMap.remove(key)
                null
            }
            on { clear() } doAnswer {
                dataMap.clear()
                null
            }
        }
    }

    @JvmStatic
    fun mockedBundle(): Bundle {
        val dataMap = mutableMapOf<String, Any?>()
        val bundle = mock<Bundle>()
        stubBundle(bundle, dataMap)
        return bundle
    }
}
