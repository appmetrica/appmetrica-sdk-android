package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.impl.db.constants.Constants

internal class PreLollipopRelativePathFormer : RelativePathFormer {
    override fun preparePath(simpleName: String): String = "${Constants.PRE_LOLLIPOP_DATABASE_PREFIX}_$simpleName"
}
