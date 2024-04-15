package io.appmetrica.analytics.impl.db.constants

import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils

object TempCacheTable {
    const val TABLE_NAME = "temp_cache"

    object Column {
        const val ID = "id"
        const val SCOPE = "scope"
        const val DATA = "data"
        const val TIMESTAMP = "timestamp"
    }

    const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
        "${Column.ID} INTEGER PRIMARY KEY," +
        "${Column.SCOPE} TEXT," +
        "${Column.DATA} BLOB," +
        "${Column.TIMESTAMP} INTEGER" +
        ")"

    const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"

    @JvmField
    val COLUMNS = CollectionUtils.createSortedListWithoutRepetitions(
        Column.ID,
        Column.SCOPE,
        Column.DATA,
        Column.TIMESTAMP
    )
}
