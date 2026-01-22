package io.appmetrica.analytics.impl.id

import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.coreapi.internal.data.Savable
import io.appmetrica.analytics.coreutils.internal.toggle.ConjunctiveCompositeThreadSafeToggle
import io.appmetrica.analytics.coreutils.internal.toggle.OuterStateToggle
import io.appmetrica.analytics.coreutils.internal.toggle.SavableToggle
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class AdvIdGetterController(
    startupState: StartupState
) {

    private val tag = "[AdvIdGetterController]"

    internal enum class State {
        ALLOWED,
        FORBIDDEN_BY_CLIENT_CONFIG,
        FORBIDDEN_BY_REMOTE_CONFIG,
        UNKNOWN
    }

    internal class CanTrackIdentifiers(
        val canTrackGaid: State,
        val canTrackHoaid: State,
        val canTrackYandexAdvId: State
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CanTrackIdentifiers

            if (canTrackGaid != other.canTrackGaid) return false
            if (canTrackHoaid != other.canTrackHoaid) return false
            if (canTrackYandexAdvId != other.canTrackYandexAdvId) return false

            return true
        }

        override fun hashCode(): Int {
            var result = canTrackGaid.hashCode()
            result = 31 * result + canTrackHoaid.hashCode()
            result = 31 * result + canTrackYandexAdvId.hashCode()
            return result
        }

        override fun toString(): String {
            return "CanTrackIdentifiers(canTrackGaid=$canTrackGaid, canTrackHoaid=$canTrackHoaid, " +
                "canTrackYandexAdvId=$canTrackYandexAdvId)"
        }
    }

    private val storage: PreferencesServiceDbStorage = GlobalServiceLocator.getInstance().servicePreferences

    private val clientApiBasedToggle = SavableToggle(
        "advIdsFromClientApi",
        object : Savable<Boolean> {
            override var value: Boolean
                get() = storage.isAdvIdentifiersTrackingStatusEnabled(false)
                set(value) {
                    storage.saveAdvIdentifiersTrackingEnabled(value)
                }
        }
    )

    private val gaidRemoteConfigToggle = OuterStateToggle(false, "GAID-remote-config")
    private val huaweiOaidRemoteConfigToggle = OuterStateToggle(false, "HOAID-remote-config")

    private val gaidToggle: Toggle = ConjunctiveCompositeThreadSafeToggle(
        listOf(clientApiBasedToggle, gaidRemoteConfigToggle),
        "GAID"
    )

    private val huaweiOaidToggle: Toggle = ConjunctiveCompositeThreadSafeToggle(
        listOf(clientApiBasedToggle, huaweiOaidRemoteConfigToggle),
        "HOAID"
    )

    private val yandexOaidToggle: Toggle = clientApiBasedToggle

    init {
        updateStartupState(startupState)
    }

    fun updateStartupState(startupState: StartupState) {
        val hadFirstStartupState = startupState.hadFirstStartup
        gaidRemoteConfigToggle.update(!hadFirstStartupState || startupState.collectingFlags.googleAid)
        huaweiOaidRemoteConfigToggle.update(!hadFirstStartupState || startupState.collectingFlags.huaweiOaid)
    }

    fun updateStateFromClientConfig(enabled: Boolean) {
        clientApiBasedToggle.update(enabled)
    }

    fun canTrackGaid(): State {
        return when {
            gaidToggle.actualState -> State.ALLOWED
            !clientApiBasedToggle.actualState -> State.FORBIDDEN_BY_CLIENT_CONFIG
            !gaidRemoteConfigToggle.actualState -> State.FORBIDDEN_BY_REMOTE_CONFIG
            else -> State.UNKNOWN
        }
    }

    fun canTrackHoaid(): State {
        return when {
            huaweiOaidToggle.actualState -> State.ALLOWED
            !clientApiBasedToggle.actualState -> State.FORBIDDEN_BY_CLIENT_CONFIG
            !huaweiOaidRemoteConfigToggle.actualState -> State.FORBIDDEN_BY_REMOTE_CONFIG
            else -> State.UNKNOWN
        }
    }

    fun canTrackYandexAdvId(): State {
        return when {
            yandexOaidToggle.actualState -> State.ALLOWED
            !clientApiBasedToggle.actualState -> State.FORBIDDEN_BY_CLIENT_CONFIG
            else -> State.UNKNOWN
        }
    }

    fun canTrackIdentifiers(): CanTrackIdentifiers = CanTrackIdentifiers(
        canTrackGaid(),
        canTrackHoaid(),
        canTrackYandexAdvId()
    ).also {
        DebugLogger.info(
            tag,
            "Can track identifiers: $it"
        )
    }
}
