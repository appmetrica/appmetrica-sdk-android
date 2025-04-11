package io.appmetrica.analytics.coreutils.internal

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

class ReferenceHolderTest : CommonTest() {

    private val referenceHolder by setUp { ReferenceHolder() }

    @Test
    fun storeAndRemove() {
        val storedReference = mock<Any>()
        val removedReference = mock<Any>()
        referenceHolder.storeReference(removedReference)
        referenceHolder.storeReference(storedReference)
        referenceHolder.storeReference(storedReference)
        referenceHolder.removeReference(removedReference)
        assertThat(referenceHolder.peekReferences()).containsOnly(storedReference)
    }
}
