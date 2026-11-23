package io.appmetrica.analytics.billingv8.impl.library

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class BillingLibraryConnectionHolderTest : CommonTest() {

    private val results = mutableListOf<Boolean>()
    private val holder = BillingLibraryConnectionHolder { results.add(it) }

    @Test
    fun listeners() {
        val firstListener = Any()
        val secondListener = Any()
        holder.addListener(firstListener)
        holder.addListener(secondListener)
        assertThat(holder.isWaitingFor(firstListener)).isTrue()

        holder.removeListener(firstListener)
        holder.removeListener(firstListener)
        assertThat(results).isEmpty()
        assertThat(holder.isWaitingFor(firstListener)).isFalse()

        holder.removeListener(secondListener)
        holder.removeListener(secondListener)
        assertThat(results).containsExactly(true)
    }

    @Test
    fun `failed round waits for its remaining listener`() {
        val listener = Any()
        holder.addListener(listener)
        holder.markFailed()
        assertThat(results).isEmpty()

        holder.removeListener(listener)
        assertThat(results).containsExactly(false)
    }

    @Test
    fun `cancel discards pending callbacks without finishing the round twice`() {
        val listener = Any()
        holder.addListener(listener)
        holder.cancel()
        holder.removeListener(listener)
        assertThat(holder.isWaitingFor(listener)).isFalse()
        assertThat(results).isEmpty()
    }
}
