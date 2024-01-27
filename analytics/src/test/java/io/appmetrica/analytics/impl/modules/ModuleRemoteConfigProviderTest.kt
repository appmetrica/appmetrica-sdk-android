package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.modulesapi.internal.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.RemoteConfigMetaInfo
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class ModuleRemoteConfigProviderTest : CommonTest() {

    private val uuid = "uuid"
    private val deviceId = "deviceId"
    private val deviceIdHash = "deviceIdHash"
    private val firstStartupTime = 354321L
    private val serverTime = 4543432L
    private val firstIdentifier = "Identifier #1"
    private val secondIdentifier = "Identifier #2"
    private val firstRemoteConfig = mock<Any>()
    private val secondRemoteConfig = mock<Any>()

    private val modulesRemoteConfigs = mapOf(
        firstIdentifier to firstRemoteConfig,
        secondIdentifier to secondRemoteConfig
    )

    private val startupState = mock<StartupState> {
        on { uuid } doReturn uuid
        on { deviceId } doReturn deviceId
        on { deviceIdHash } doReturn deviceIdHash
        on { firstStartupServerTime } doReturn firstStartupTime
        on { obtainServerTime } doReturn serverTime
        on { modulesRemoteConfigs } doReturn modulesRemoteConfigs
    }

    private val identifiers = SdkIdentifiers(uuid, deviceId, deviceIdHash)
    private val remoteConfigMetaInfo = RemoteConfigMetaInfo(firstStartupTime, serverTime)

    private val moduleRemoteConfigProvider = ModuleRemoteConfigProvider(startupState)

    @Test
    fun getRemoteConfigForFirstModule() {
        assertThat(moduleRemoteConfigProvider.getRemoteConfigForModule(firstIdentifier))
            .isEqualToComparingFieldByField(
                ModuleRemoteConfig(identifiers, remoteConfigMetaInfo, firstRemoteConfig)
            )
    }

    @Test
    fun getRemoteConfigForSecondModule() {
        assertThat(moduleRemoteConfigProvider.getRemoteConfigForModule(secondIdentifier))
            .isEqualToComparingFieldByField(
                ModuleRemoteConfig(identifiers, remoteConfigMetaInfo, secondRemoteConfig)
            )
    }

    @Test
    fun getRemoteConfigForUnknownModule() {
        assertThat(moduleRemoteConfigProvider.getRemoteConfigForModule("unknown module"))
            .isEqualToComparingFieldByField(
                ModuleRemoteConfig(identifiers, remoteConfigMetaInfo, null)
            )
    }
}
