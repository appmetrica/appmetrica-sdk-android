package io.appmetrica.analytics.impl.db.storage

internal class OldDatabaseRelativePathFormer : RelativePathFormer {

    override fun preparePath(simpleName: String): String = simpleName
}
