package io.appmetrica.analytics.impl.id

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.data.Savable
import io.appmetrica.analytics.coreutils.internal.toggle.ConjunctiveCompositeThreadSafeToggle
import io.appmetrica.analytics.coreutils.internal.toggle.OuterStateToggle
import io.appmetrica.analytics.coreutils.internal.toggle.SavableToggle
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.impl.startup.CollectingFlags
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class AdvIdGetterControllerTest : CommonTest() {

    private val startupState: StartupState =
        StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build()).build()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val savableToggleMockedConstructionRule = constructionRule<SavableToggle>()
    private val clientApiBasedToggle: SavableToggle by savableToggleMockedConstructionRule

    @get:Rule
    val outerStateToggleMockedConstructionRule = constructionRule<OuterStateToggle>()

    private val storage: PreferencesServiceDbStorage by setUp { GlobalServiceLocator.getInstance().servicePreferences }

    private val controller: AdvIdGetterController by setUp { AdvIdGetterController(startupState) }

    private val gaidRemoteConfigToggle: OuterStateToggle by setUp {
        outerStateToggleMockedConstructionRule.constructionMock.constructed().first()
    }

    private val hoaidRemoteConfigToggle: OuterStateToggle by setUp {
        outerStateToggleMockedConstructionRule.constructionMock.constructed()[1]
    }

    @get:Rule
    val conjunctiveCompositeThreadSafeToggleMockedConstructionRule =
        constructionRule<ConjunctiveCompositeThreadSafeToggle>()

    private val gaidToggle: Toggle by setUp {
        conjunctiveCompositeThreadSafeToggleMockedConstructionRule.constructionMock.constructed().first()
    }

    private val hoaidToggle: Toggle by setUp {
        conjunctiveCompositeThreadSafeToggleMockedConstructionRule.constructionMock.constructed()[1]
    }

    @Test
    fun `construction - clientApiBasedToggle`() {
        assertThat(savableToggleMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        val arguments = savableToggleMockedConstructionRule.argumentInterceptor.arguments.first()
        assertThat(arguments).hasSize(2)
        assertThat(arguments[0]).isEqualTo("advIdsFromClientApi")
        val savable = arguments[1] as Savable<Boolean>

        whenever(storage.isAdvIdentifiersTrackingStatusEnabled(any())).thenReturn(true)
        assertThat(savable.value).isTrue()
        savable.value = false
        verify(storage).saveAdvIdentifiersTrackingEnabled(false)
    }

    @Test
    fun `construction - remote config toggles`() {
        assertThat(outerStateToggleMockedConstructionRule.constructionMock.constructed()).hasSize(2)
        assertThat(outerStateToggleMockedConstructionRule.argumentInterceptor.arguments)
            .containsExactly(
                listOf(false, "GAID-remote-config"),
                listOf(false, "HOAID-remote-config")
            )
    }

    @Test
    fun `construction - adv identifier toggles`() {
        assertThat(conjunctiveCompositeThreadSafeToggleMockedConstructionRule.constructionMock.constructed()).hasSize(2)
        assertThat(conjunctiveCompositeThreadSafeToggleMockedConstructionRule.argumentInterceptor.arguments)
            .containsExactly(
                listOf(listOf(clientApiBasedToggle, gaidRemoteConfigToggle), "GAID"),
                listOf(listOf(clientApiBasedToggle, hoaidRemoteConfigToggle), "HOAID")
            )
    }

    @Test
    fun `construction - update startup state`() {
        verify(gaidRemoteConfigToggle).update(true)
        verify(hoaidRemoteConfigToggle).update(true)
    }

    @Test
    fun `updateStartupState if startup had not startup been yet`() {
        clearInvocations(gaidRemoteConfigToggle, hoaidRemoteConfigToggle)
        controller.updateStartupState(
            StartupState.Builder(
                CollectingFlags.CollectingFlagsBuilder().build()
            )
                .withHadFirstStartup(false)
                .build()
        )
        verify(gaidRemoteConfigToggle).update(true)
        verify(hoaidRemoteConfigToggle).update(true)
    }

    @Test
    fun `updateStartupState if had first startup without features`() {
        clearInvocations(gaidToggle, hoaidRemoteConfigToggle)
        controller.updateStartupState(
            StartupState.Builder(
                CollectingFlags.CollectingFlagsBuilder().build()
            )
                .withHadFirstStartup(true)
                .build()
        )
        verify(gaidRemoteConfigToggle).update(false)
        verify(hoaidRemoteConfigToggle).update(false)
    }

    @Test
    fun `updateStartupState with features`() {
        clearInvocations(gaidRemoteConfigToggle, hoaidRemoteConfigToggle)
        controller.updateStartupState(
            StartupState.Builder(
                CollectingFlags.CollectingFlagsBuilder()
                    .withGoogleAid(true)
                    .withHuaweiOaid(true)
                    .build()
            )
                .withHadFirstStartup(true)
                .build()
        )
    }

    @Test
    fun updateStateFromClient() {
        clearInvocations(clientApiBasedToggle)
        controller.updateStateFromClientConfig(true)
        clientApiBasedToggle.update(true)
    }

    @Test
    fun `canTrackGaid - allowed`() {
        whenever(gaidToggle.actualState).thenReturn(true)
        assertThat(controller.canTrackGaid()).isEqualTo(AdvIdGetterController.State.ALLOWED)
    }

    @Test
    fun `canTrackGaid - forbidden by client config`() {
        whenever(gaidToggle.actualState).thenReturn(false)
        whenever(clientApiBasedToggle.actualState).thenReturn(false)
        assertThat(controller.canTrackGaid()).isEqualTo(AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG)
    }

    @Test
    fun `canTrackGaid - forbidden by remote config`() {
        whenever(gaidToggle.actualState).thenReturn(false)
        whenever(clientApiBasedToggle.actualState).thenReturn(true)
        whenever(gaidRemoteConfigToggle.actualState).thenReturn(false)
        assertThat(controller.canTrackGaid()).isEqualTo(AdvIdGetterController.State.FORBIDDEN_BY_REMOTE_CONFIG)
    }

    @Test
    fun `canTrackGaid - unknown state`() {
        whenever(gaidToggle.actualState).thenReturn(false)
        whenever(clientApiBasedToggle.actualState).thenReturn(true)
        whenever(gaidRemoteConfigToggle.actualState).thenReturn(true)
        assertThat(controller.canTrackGaid()).isEqualTo(AdvIdGetterController.State.UNKNOWN)
    }

    @Test
    fun `canTrackHoaid - allowed`() {
        whenever(hoaidToggle.actualState).thenReturn(true)
        assertThat(controller.canTrackHoaid()).isEqualTo(AdvIdGetterController.State.ALLOWED)
    }

    @Test
    fun `canTrackHoaid - forbidden by client config`() {
        whenever(hoaidToggle.actualState).thenReturn(false)
        whenever(clientApiBasedToggle.actualState).thenReturn(false)
        assertThat(controller.canTrackHoaid()).isEqualTo(AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG)
    }

    @Test
    fun `canTrackHoaid - forbidden by remote config`() {
        whenever(hoaidToggle.actualState).thenReturn(false)
        whenever(clientApiBasedToggle.actualState).thenReturn(true)
        whenever(hoaidRemoteConfigToggle.actualState).thenReturn(false)
        assertThat(controller.canTrackHoaid()).isEqualTo(AdvIdGetterController.State.FORBIDDEN_BY_REMOTE_CONFIG)
    }

    @Test
    fun `canTrackHoaid - unknown`() {
        whenever(hoaidToggle.actualState).thenReturn(false)
        whenever(clientApiBasedToggle.actualState).thenReturn(true)
        whenever(hoaidRemoteConfigToggle.actualState).thenReturn(true)
        assertThat(controller.canTrackHoaid()).isEqualTo(AdvIdGetterController.State.UNKNOWN)
    }

    @Test
    fun `canTrackYandexAdvId - allowed`() {
        whenever(clientApiBasedToggle.actualState).thenReturn(true)
        assertThat(controller.canTrackYandexAdvId()).isEqualTo(AdvIdGetterController.State.ALLOWED)
    }

    @Test
    fun `canTrackYandexAdvId - unknown`() {
        whenever(clientApiBasedToggle.actualState).thenReturn(false)
        assertThat(controller.canTrackYandexAdvId()).isEqualTo(AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG)
    }

    @Test
    fun canTrackIdentifiers() {
        whenever(clientApiBasedToggle.actualState).thenReturn(true)
        whenever(gaidToggle.actualState).thenReturn(true)
        whenever(hoaidToggle.actualState).thenReturn(true)
        ObjectPropertyAssertions(controller.canTrackIdentifiers())
            .checkField("canTrackGaid", AdvIdGetterController.State.ALLOWED)
            .checkField("canTrackHoaid", AdvIdGetterController.State.ALLOWED)
            .checkField("canTrackYandexAdvId", AdvIdGetterController.State.ALLOWED)
            .checkAll()
    }
}
