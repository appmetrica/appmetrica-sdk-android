package io.appmetrica.analytics.impl.db.storage

internal interface RelativePathFormer {

    fun preparePath(simpleName: String): String
}
