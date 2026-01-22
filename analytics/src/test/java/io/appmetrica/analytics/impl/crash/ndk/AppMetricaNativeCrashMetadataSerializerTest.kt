package io.appmetrica.analytics.impl.crash.ndk

import android.util.Base64
import io.appmetrica.analytics.internal.CounterConfigurationReporterType
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RobolectricTestRunner
import kotlin.reflect.KProperty1

private typealias AppMetricaNativeCrashMetadataProperty<T> = KProperty1<AppMetricaNativeCrashMetadata, T>

@RunWith(RobolectricTestRunner::class)
internal class AppMetricaNativeCrashMetadataSerializerTest : CommonTest() {

    companion object {
        private const val DEFAULT_API_KEY = "apiKey"
        private const val DEFAULT_PACKAGE_NAME = "packageName"
        private const val DEFAULT_PROCESS_ID = 0
        private const val DEFAULT_PROCESS_SESSION_ID = "sessionID"
        private const val DEFAULT_ERROR_ENVIRONMENT = "errorEnv"
        private val DEFAULT_REPORTER_TYPE = CounterConfigurationReporterType.MAIN
    }

    @RunWith(ParameterizedRobolectricTestRunner::class)
    internal class FieldsTests(
        private val checkProperty: AppMetricaNativeCrashMetadataProperty<*>,
        private val value: Any?,
        private val propertyName: String,
    ) : CommonTest() {
        companion object {
            @JvmStatic
            @ParameterizedRobolectricTestRunner.Parameters(name = "{2} = {1}")
            fun data(): Collection<Array<Any?>> = listOf(
                entry(AppMetricaNativeCrashMetadata::apiKey, DEFAULT_API_KEY),
                entry(AppMetricaNativeCrashMetadata::apiKey, "!@#$%^"),
                entry(AppMetricaNativeCrashMetadata::apiKey, ""),

                entry(AppMetricaNativeCrashMetadata::packageName, DEFAULT_PACKAGE_NAME),
                entry(AppMetricaNativeCrashMetadata::packageName, "!@#$%"),
                entry(AppMetricaNativeCrashMetadata::packageName, ""),

                entry(AppMetricaNativeCrashMetadata::reporterType, DEFAULT_REPORTER_TYPE),
                entry(AppMetricaNativeCrashMetadata::reporterType, CounterConfigurationReporterType.CRASH),

                entry(AppMetricaNativeCrashMetadata::processID, DEFAULT_PROCESS_ID),
                entry(AppMetricaNativeCrashMetadata::processID, Int.MIN_VALUE),
                entry(AppMetricaNativeCrashMetadata::processID, Int.MAX_VALUE),

                entry(AppMetricaNativeCrashMetadata::processSessionID, DEFAULT_PROCESS_SESSION_ID),
                entry(AppMetricaNativeCrashMetadata::processSessionID, "!@@#$%"),
                entry(AppMetricaNativeCrashMetadata::processSessionID, ""),

                entry(AppMetricaNativeCrashMetadata::errorEnvironment, DEFAULT_ERROR_ENVIRONMENT),
                entry(AppMetricaNativeCrashMetadata::errorEnvironment, "{\"key\":\"value\"}"),
                entry(AppMetricaNativeCrashMetadata::errorEnvironment, "!@#$%"),
                entry(AppMetricaNativeCrashMetadata::errorEnvironment, ""),
                entry(AppMetricaNativeCrashMetadata::errorEnvironment, null),
            )

            private fun <T> entry(property: AppMetricaNativeCrashMetadataProperty<T>, value: T) =
                arrayOf(property, value, property.name)
        }

        private val serializer by setUp { AppMetricaNativeCrashMetadataSerializer() }

        @Test
        fun `serialize and deserialize`() {
            val serializedMetadata = serializer.serialize(createMetadata())
            // check if serializedMetadata is a valid base64
            Base64.decode(serializedMetadata, Base64.DEFAULT)

            val metadata = serializer.deserialize(serializedMetadata)
            assertThat(metadata).isNotNull()
            assertThat(checkProperty.get(metadata!!)).isEqualTo(value)
        }

        private fun createMetadata() = AppMetricaNativeCrashMetadata(
            getIf(AppMetricaNativeCrashMetadata::apiKey, DEFAULT_API_KEY),
            getIf(AppMetricaNativeCrashMetadata::packageName, DEFAULT_PACKAGE_NAME),
            getIf(AppMetricaNativeCrashMetadata::reporterType, DEFAULT_REPORTER_TYPE),
            getIf(AppMetricaNativeCrashMetadata::processID, DEFAULT_PROCESS_ID),
            getIf(AppMetricaNativeCrashMetadata::processSessionID, DEFAULT_PROCESS_SESSION_ID),
            getIf(AppMetricaNativeCrashMetadata::errorEnvironment, DEFAULT_ERROR_ENVIRONMENT),
        )

        private fun <T> getIf(property: AppMetricaNativeCrashMetadataProperty<T>, default: T): T {
            @Suppress("UNCHECKED_CAST")
            return if (checkProperty == property) value as T else default
        }
    }

    @RunWith(ParameterizedRobolectricTestRunner::class)
    internal class BrokenFieldsTests(
        private val checkProperty: AppMetricaNativeCrashMetadataProperty<*>,
        private val value: Any?,
        private val broken: Boolean,
        private val actualValue: Any?,
        private val propertyName: String,
    ) : CommonTest() {
        companion object {
            @JvmStatic
            @ParameterizedRobolectricTestRunner.Parameters(name = "{4} = {1}")
            fun data(): Collection<Array<Any?>> = listOf(
                entry(AppMetricaNativeCrashMetadata::apiKey, null),
                entry(AppMetricaNativeCrashMetadata::packageName, null),
                entry(AppMetricaNativeCrashMetadata::reporterType, null),
                entry(
                    AppMetricaNativeCrashMetadata::reporterType,
                    "broken reporter type",
                    broken = false,
                    actualValue = CounterConfigurationReporterType.MAIN
                ),
                entry(AppMetricaNativeCrashMetadata::processID, null),
                entry(AppMetricaNativeCrashMetadata::processID, "some string"),
                entry(AppMetricaNativeCrashMetadata::processID, false),
                entry(AppMetricaNativeCrashMetadata::processSessionID, null),
            )

            private fun <T> entry(
                property: AppMetricaNativeCrashMetadataProperty<T>,
                value: Any?,
                broken: Boolean = true,
                actualValue: Any? = null
            ) = arrayOf(property, value, broken, actualValue, property.name)
        }

        private val serializer by setUp { AppMetricaNativeCrashMetadataSerializer() }

        @Test
        fun deserialize() {
            val metadata = serializer.deserialize(createMetadataString())
            if (broken) {
                assertThat(metadata).isNull()
            } else {
                assertThat(metadata).isNotNull()
                assertThat(checkProperty.get(metadata!!)).isEqualTo(actualValue)
            }
        }

        private fun createMetadataString(): String {
            val metadata = AppMetricaNativeCrashMetadata(
                DEFAULT_API_KEY,
                DEFAULT_PACKAGE_NAME,
                DEFAULT_REPORTER_TYPE,
                DEFAULT_PROCESS_ID,
                DEFAULT_PROCESS_SESSION_ID,
                DEFAULT_ERROR_ENVIRONMENT,
            )
            val json = JSONObject(String(Base64.decode(serializer.serialize(metadata), Base64.DEFAULT)))
            json.put(checkProperty.name, value)
            return Base64.encodeToString(json.toString().toByteArray(), Base64.DEFAULT)
        }
    }

    private val serializer by setUp { AppMetricaNativeCrashMetadataSerializer() }

    @Test
    fun `deserialize broken metadata`() {
        assertThat(serializer.deserialize("broken metadata")).isNull()
    }

    @Test
    fun `deserialize empty metadata`() {
        assertThat(serializer.deserialize("")).isNull()
    }

    @Test
    fun `deserialize valid base64 but broken json`() {
        assertThat(serializer.deserialize(Base64.encodeToString("metadata".toByteArray(), Base64.DEFAULT))).isNull()
    }
}
