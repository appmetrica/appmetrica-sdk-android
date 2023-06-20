package io.appmetrica.analytics.impl.preloadinfo

import io.appmetrica.analytics.impl.ContentProviderDataSaver
import io.appmetrica.analytics.impl.GlobalServiceLocator

internal class PreloadInfoDataSaver : ContentProviderDataSaver<PreloadInfoState> {

    override fun invoke(data: PreloadInfoState): Boolean =
        GlobalServiceLocator.getInstance().preloadInfoStorage.updateIfNeeded(data)
}
