package io.appmetrica.analytics.ndkcrashes.impl.utils

import android.os.Build
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.robolectric.util.ReflectionHelpers
import kotlin.reflect.KFunction

@RunWith(Parameterized::class)
class AndroidUtilsTest(
    private val testId: String,
    private val func: () -> Boolean,
    private val needApi: Int,
    private val curApi: Int,
) : CommonTest() {
    companion object {
        private val APIS = 10..50

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            AndroidUtils::isAndroidMAchieved to 23,
            AndroidUtils::isAndroidNAchieved to 24,
            AndroidUtils::isAndroidQAchieved to 29,
        ).flatMap { (func, needApi) ->
            APIS.map { api ->
                entry(func, needApi, api)
            }
        }

        private fun entry(func: KFunction<Boolean>, needApi: Int, curApi: Int) =
            arrayOf("${func.name} with $curApi", func, needApi, curApi)
    }

    @Test
    fun checkApi() {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", curApi)
        assertThat(func()).isEqualTo(curApi >= needApi)
    }
}
