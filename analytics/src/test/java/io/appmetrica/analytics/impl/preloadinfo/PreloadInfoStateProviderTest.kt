package io.appmetrica.analytics.impl.preloadinfo

import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.junit.Test
import org.mockito.kotlin.mock

internal class PreloadInfoStateProviderTest : CommonTest() {

    private val stateProvider = PreloadInfoStateProvider()

    @Test
    fun createState() {
        val chosen = mock<PreloadInfoState>()
        val candidates = listOf(mock<PreloadInfoData.Candidate>(), mock<PreloadInfoData.Candidate>())
        val state = stateProvider(chosen, candidates)
        ObjectPropertyAssertions(state)
            .checkField("chosenPreloadInfo", chosen)
            .checkField("candidates", candidates)
            .checkAll()
    }
}
