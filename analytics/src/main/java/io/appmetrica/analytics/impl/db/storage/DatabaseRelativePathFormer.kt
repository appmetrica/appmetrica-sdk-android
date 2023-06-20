package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.impl.db.constants.Constants

internal class DatabaseRelativePathFormer : RelativePathFormer {

    override fun preparePath(simpleName: String): String = "${Constants.DATABASES_RELATIVE_PATH}/$simpleName"
}
