package io.appmetrica.analytics.coreutils.internal.toggle

import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.control.ToggleObserver
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.LogRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class ConjunctiveCompositeThreadSafeToggleTest : CommonTest() {

    // Every third attempt to acquire lock will succeed and two other will fail
    private val defaultMockAcquireAttempts = 3

    private var actualMockInvocationCount = 0

    private val lockWaitingMillis = 100L

    @get:Rule
    val logRule = LogRule()

    private val lock = mock<ReentrantLock> {
        on { tryLock(lockWaitingMillis, TimeUnit.MILLISECONDS) }.then {
            actualMockInvocationCount++
            actualMockInvocationCount % defaultMockAcquireAttempts == 0
        }
    }

    private val firstToggle = mock<Toggle>()
    private val secondToggle = mock<Toggle>()

    private val firstObserver = mock<ToggleObserver>()
    private val secondObserver = mock<ToggleObserver>()

    private val toggleObserverCaptor = argumentCaptor<ToggleObserver>()

    private lateinit var compositeToggle: ConjunctiveCompositeThreadSafeToggle

    @Before
    fun setUp() {
        compositeToggle = ConjunctiveCompositeThreadSafeToggle(listOf(firstToggle, secondToggle), "subtag", lock)
    }

    @Test
    fun constructor() {
        inOrder(lock, firstToggle, secondToggle) {
            verify(lock, times(defaultMockAcquireAttempts)).tryLock(lockWaitingMillis, TimeUnit.MILLISECONDS)
            verifyToggleObserversRegistration()
            verify(lock).unlock()
        }
    }

    @Test
    fun `initialState for true and true`() {
        initialState(firstToggleInitialState = true, secondToggleInitialState = true, expectedResultState = true)
    }

    @Test
    fun `initialState for true and false`() {
        initialState(firstToggleInitialState = true, secondToggleInitialState = false, expectedResultState = false)
    }

    @Test
    fun `initialState for false and false`() {
        initialState(firstToggleInitialState = false, secondToggleInitialState = false, expectedResultState = false)
    }

    private fun initialState(
        firstToggleInitialState: Boolean,
        secondToggleInitialState: Boolean,
        expectedResultState: Boolean
    ) {
        whenever(firstToggle.actualState).thenReturn(firstToggleInitialState)
        whenever(secondToggle.actualState).thenReturn(secondToggleInitialState)
        compositeToggle = ConjunctiveCompositeThreadSafeToggle(listOf(firstToggle, secondToggle), "some tag", lock)
        assertThat(compositeToggle.actualState).isEqualTo(expectedResultState)
    }

    @Test
    fun `registerObserver with sticky`() {
        clearInvocations(lock, firstObserver)
        compositeToggle.registerObserver(firstObserver, sticky = true)
        inOrder(lock, firstObserver) {
            verify(lock, times(defaultMockAcquireAttempts)).tryLock(lockWaitingMillis, TimeUnit.MILLISECONDS)
            verify(firstObserver).onStateChanged(compositeToggle.actualState)
            verify(lock).unlock()
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `registerObserver without sticky`() {
        clearInvocations(lock, firstObserver)
        compositeToggle.registerObserver(firstObserver, sticky = false)
        inOrder(lock, firstObserver) {
            verify(lock, times(defaultMockAcquireAttempts)).tryLock(lockWaitingMillis, TimeUnit.MILLISECONDS)
            verify(firstObserver, never()).onStateChanged(compositeToggle.actualState)
            verify(lock).unlock()
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `registerObserver with default sticky`() {
        clearInvocations(lock, firstObserver)
        compositeToggle.registerObserver(firstObserver, true)
        inOrder(lock, firstObserver) {
            verify(lock, times(defaultMockAcquireAttempts)).tryLock(lockWaitingMillis, TimeUnit.MILLISECONDS)
            verify(firstObserver).onStateChanged(compositeToggle.actualState)
            verify(lock).unlock()
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun updateState() {
        compositeToggle.registerObserver(firstObserver, true)
        compositeToggle.registerObserver(secondObserver, true)
        clearInvocations(lock, firstObserver, secondObserver)

        verifyToggleObserversRegistration()
        assertThat(toggleObserverCaptor.allValues).hasSize(2)

        toggleObserverCaptor.firstValue.onStateChanged(true)
        inOrder(lock, firstObserver, secondObserver) {
            verify(lock, times(defaultMockAcquireAttempts)).tryLock(lockWaitingMillis, TimeUnit.MILLISECONDS)
            verify(lock).unlock()
            verifyNoMoreInteractions()
        }

        clearInvocations(lock, firstObserver, secondObserver)

        toggleObserverCaptor.secondValue.onStateChanged(true)
        inOrder(lock, firstObserver, secondObserver) {
            verify(lock, times(defaultMockAcquireAttempts)).tryLock(lockWaitingMillis, TimeUnit.MILLISECONDS)
            verify(firstObserver).onStateChanged(true)
            verify(secondObserver).onStateChanged(true)
            verify(lock).unlock()
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun removeObserver() {
        compositeToggle.registerObserver(firstObserver, true)
        compositeToggle.registerObserver(secondObserver, true)
        clearInvocations(firstObserver, secondObserver)

        verifyToggleObserversRegistration()
        toggleObserverCaptor.firstValue.onStateChanged(true)
        clearInvocations(lock, firstObserver, secondObserver)
        compositeToggle.removeObserver(secondObserver)

        inOrder(lock, firstObserver, secondObserver) {
            verify(lock, times(defaultMockAcquireAttempts)).tryLock(lockWaitingMillis, TimeUnit.MILLISECONDS)
            verify(lock).unlock()
            verifyNoMoreInteractions()
        }

        toggleObserverCaptor.secondValue.onStateChanged(true)
        verify(firstObserver).onStateChanged(true)
        verifyNoMoreInteractions(secondObserver)
    }

    private fun verifyToggleObserversRegistration() {
        verify(firstToggle).registerObserver(toggleObserverCaptor.capture(), eq(false))
        verify(secondToggle).registerObserver(toggleObserverCaptor.capture(), eq(false))
    }
}
