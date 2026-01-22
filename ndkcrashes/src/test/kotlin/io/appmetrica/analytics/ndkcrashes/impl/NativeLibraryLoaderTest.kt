package io.appmetrica.analytics.ndkcrashes.impl

import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class NativeLibraryLoaderTest : CommonTest() {
    companion object {
        private const val LIB_NAME = "lib-name"
        private const val OTHER_LIB_NAME = "other-lib"
    }

    @get:Rule
    internal val libraryLoaderMockedConstructionRule = constructionRule<NativeLibraryLoader.LibraryLoader> {
        on { load(any()) } doAnswer { check(it.getArgument<String>(0) == LIB_NAME) }
    }
    private val libraryLoader by libraryLoaderMockedConstructionRule

    @Test
    fun `first load`() {
        val loader = createLoader(LIB_NAME)
        assertThat(loader.loadIfNeeded()).isTrue()
        verify(libraryLoader).load(eq(LIB_NAME))
    }

    @Test
    fun `twice load library`() {
        val loader = createLoader(LIB_NAME)
        assertThat(loader.loadIfNeeded()).isTrue()
        verify(libraryLoader).load(eq(LIB_NAME))
        assertThat(loader.loadIfNeeded()).isTrue()
        verifyNoMoreInteractions(libraryLoader)
    }

    @Test
    fun `failed load`() {
        val loader = createLoader(OTHER_LIB_NAME)
        assertThat(loader.loadIfNeeded()).isFalse()
        verify(libraryLoader).load(eq(OTHER_LIB_NAME))
    }

    @Test
    fun `load after error`() {
        val loader = createLoader(OTHER_LIB_NAME)
        assertThat(loader.loadIfNeeded()).isFalse()
        verify(libraryLoader).load(eq(OTHER_LIB_NAME))
        assertThat(loader.loadIfNeeded()).isFalse()
        verifyNoMoreInteractions(libraryLoader)
    }

    @Test
    fun `load appmetrica-native`() {
        AppMetricaNativeLibraryLoader().loadIfNeeded()
        verify(libraryLoader).load(eq("appmetrica-native"))
    }

    @Test
    fun `load appmetrica-service-native`() {
        AppMetricaServiceNativeLibraryLoader().loadIfNeeded()
        verify(libraryLoader).load(eq("appmetrica-service-native"))
    }

    private fun createLoader(libName: String): NativeLibraryLoader =
        object : NativeLibraryLoader(libName) {}
}
