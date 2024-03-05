package io.appmetrica.analytics.impl.startup.uuid

import android.os.Build
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.KITKAT, Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.Q,
    Build.VERSION_CODES.TIRAMISU])
class UuidValidatorTest(
    private val input: String?,
    private val expected: Boolean
) : CommonTest() {

    private val uuidValidator by setUp { UuidValidator() }

    @Test
    fun isValid() {
        assertThat(uuidValidator.isValid(input)).isEqualTo(expected)
    }

    companion object {

        private val VALID_UUID = UUID.randomUUID().toString().replace("-", "").lowercase()
        private val LESS_THAN_VALID_UUID = UUID.randomUUID().toString().replace("-", "").lowercase().substring(0, 23)
        private val LONGER_THAN_VALID_UUID = UUID.randomUUID().toString().replace("-", "").lowercase().plus("a")
        private val STRING_WITH_NULLS = String(CharArray(24) { Character.MIN_VALUE })
        private val STRING_WITH_FFFF = String(CharArray(24) { Character.MAX_VALUE })

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "`{0}` -> `{1}`")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(null, false),
            arrayOf("", false),
            arrayOf(VALID_UUID, true),
            arrayOf(LESS_THAN_VALID_UUID, false),
            arrayOf(LONGER_THAN_VALID_UUID, false),
            arrayOf(STRING_WITH_NULLS, false),
            arrayOf(STRING_WITH_FFFF, false)
        )
    }
}
