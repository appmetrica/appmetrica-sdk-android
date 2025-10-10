package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class RequestStateHolderTest : CommonTest() {

    private val requestStatePrefKey = "request_state"
    private val requestStateValue = "request_state_value"
    private val firstRequestType = "first_request_type"
    private val firstRequestState: RequestState = mock {
        on { type }.thenReturn(firstRequestType)
    }

    private val secondRequestType = "second_request_type"
    private val secondRequestState: RequestState = mock {
        on { type }.thenReturn(secondRequestType)
    }

    private val thirdRequestType = "third_request_type"
    private val thirdRequestState: RequestState = mock {
        on { type }.thenReturn(thirdRequestType)
    }

    private val modelCaptor = argumentCaptor<List<RequestState>>()
    private val stateToSave = "Some state to save"

    private val preferences: ModulePreferences = mock {
        on { getString(requestStatePrefKey, null) } doReturn (requestStateValue)
    }

    @get:Rule
    val requestStateConverter = constructionRule<RequestStateConverter> {
        on { toModel(requestStateValue) } doReturn listOf(firstRequestState, secondRequestState)
        on { toModel(null) } doReturn emptyList()
        on { fromModel(modelCaptor.capture()) } doReturn stateToSave
    }

    @Test
    fun `getRequestState if initial state is null`() {
        whenever(preferences.getString(requestStatePrefKey, null)).thenReturn(null)

        val holder = RequestStateHolder(preferences)

        assertThat(holder.getRequestState(firstRequestType)).isNull()
        assertThat(holder.getRequestState(secondRequestType)).isNull()
    }

    @Test
    fun `getRequestState if initial state is not null`() {
        val holder = RequestStateHolder(preferences)

        assertThat(holder.getRequestState(firstRequestType)).isEqualTo(firstRequestState)
        assertThat(holder.getRequestState(secondRequestType)).isEqualTo(secondRequestState)
    }

    @Test
    fun `updateRequestState if initial state is empty`() {
        whenever(preferences.getString(requestStatePrefKey, null)).thenReturn(null)

        val holder = RequestStateHolder(preferences)

        holder.updateRequestState(firstRequestState)

        assertThat(holder.getRequestState(firstRequestType)).isEqualTo(firstRequestState)
        verify(preferences).putString(requestStatePrefKey, stateToSave)
        assertThat(modelCaptor.firstValue).containsExactly(firstRequestState)

        clearInvocations(preferences)

        holder.updateRequestState(secondRequestState)
        assertThat(holder.getRequestState(firstRequestType)).isEqualTo(firstRequestState)
        assertThat(holder.getRequestState(secondRequestType)).isEqualTo(secondRequestState)
        verify(preferences).putString(requestStatePrefKey, stateToSave)
        assertThat(modelCaptor.secondValue).containsExactly(firstRequestState, secondRequestState)
    }

    @Test
    fun `updateRequestState if initial state is not empty`() {
        val holder = RequestStateHolder(preferences)

        holder.updateRequestState(thirdRequestState)
        assertThat(holder.getRequestState(firstRequestType)).isEqualTo(firstRequestState)
        assertThat(holder.getRequestState(secondRequestType)).isEqualTo(secondRequestState)
        assertThat(holder.getRequestState(thirdRequestType)).isEqualTo(thirdRequestState)

        verify(preferences).putString(requestStatePrefKey, stateToSave)

        assertThat(modelCaptor.firstValue).containsExactly(
            firstRequestState,
            secondRequestState,
            thirdRequestState
        )
    }
}
