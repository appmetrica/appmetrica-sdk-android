package io.appmetrica.analytics.impl.db.preferences

import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper

internal abstract class NameSpacedPreferenceDbStorage(
    dbStorage: IKeyValueTableDbHelper,
    suffix: String?
) : PreferencesDbStorage(dbStorage, suffix), SimplePreferenceStorage {

    constructor(dbStorage: IKeyValueTableDbHelper) : this(dbStorage, null)

    override fun putString(key: String, value: String?): SimplePreferenceStorage = writeString(prepareKey(key), value)

    override fun getString(key: String, fallback: String?): String? = readString(prepareKey(key), fallback)

    override fun putInt(key: String, value: Int): SimplePreferenceStorage = writeInt(prepareKey(key), value)

    override fun getInt(key: String, fallback: Int): Int = readInt(prepareKey(key), fallback)

    override fun putLong(key: String, value: Long): SimplePreferenceStorage = writeLong(prepareKey(key), value)

    override fun getLong(key: String, fallback: Long): Long = readLong(prepareKey(key), fallback)

    override fun putBoolean(key: String, value: Boolean): SimplePreferenceStorage = writeBoolean(prepareKey(key), value)

    override fun getBoolean(key: String, fallback: Boolean): Boolean = readBoolean(prepareKey(key), fallback)

    override fun remove(key: String): SimplePreferenceStorage = removeKey(prepareKey(key))

    override fun contains(key: String): Boolean = containsKey(prepareKey(key))

    protected abstract fun prepareKey(key: String): String
}
