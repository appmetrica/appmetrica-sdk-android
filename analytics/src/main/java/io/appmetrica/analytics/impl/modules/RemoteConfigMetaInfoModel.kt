package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigMetaInfo

internal data class RemoteConfigMetaInfoModel(
    override val firstSendTime: Long,
    override val lastUpdateTime: Long
) : RemoteConfigMetaInfo
